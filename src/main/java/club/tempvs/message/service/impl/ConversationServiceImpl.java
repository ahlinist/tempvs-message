package club.tempvs.message.service.impl;

import club.tempvs.message.dao.ConversationRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.util.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ConversationServiceImpl implements ConversationService {

    private final ObjectFactory objectFactory;
    private final ConversationRepository conversationRepository;

    @Autowired
    public ConversationServiceImpl(ObjectFactory objectFactory, ConversationRepository conversationRepository) {
        this.objectFactory = objectFactory;
        this.conversationRepository = conversationRepository;
    }

    public Conversation createConversation(
            Participant author, Set<Participant> receivers, String name, Message message) {
        Conversation conversation = objectFactory.getInstance(Conversation.class);
        conversation.setParticipants(receivers);
        conversation.addParticipant(author);
        conversation.setName(name);
        conversation.addMessage(message);
        conversation.setLastMessage(message);
        message.setConversation(conversation);

        if (conversation.getParticipants().size() > 2) {
            conversation.setAdmin(author);
        }

        return conversationRepository.saveAndFlush(conversation);
    }

    public Conversation getConversation(Long id) {
        return conversationRepository.findById(id).orElse(null);
    }

    public Conversation addMessage(Conversation conversation, Message message) {
        conversation.addMessage(message);
        conversation.setLastMessage(message);
        message.setConversation(conversation);
        return conversationRepository.save(conversation);
    }

    public List<Conversation> getConversationsByParticipant(Participant participant, int page, int size) {
        Set<Participant> participants = new HashSet<>();
        participants.add(participant);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "lastMessage.createdDate");
        return conversationRepository.findByParticipantsIn(participants, pageable);
    }

    public Conversation removeParticipant(Conversation conversation, Participant remover, Participant removed) {
        Set<Participant> participants = conversation.getParticipants();

        if (participants.size() <= 2) {
            throw new IllegalArgumentException("Conversation has only 2 participants. One can't delete one of them.");
        }

        Participant admin = conversation.getAdmin();
        boolean isSelfRemoval = removed.equals(remover);

        if ((admin == null || !admin.equals(remover)) && !isSelfRemoval) {
            throw new IllegalArgumentException("Participants can be removed only by admin or by themselves.");
        }

        conversation.removeParticipant(removed);
        return conversationRepository.saveAndFlush(conversation);
    }

    public Conversation addParticipant(Conversation conversation, Participant adder, Participant added) {
        Set<Participant> participants = conversation.getParticipants();

        if (participants.size() == 20) {
            throw new IllegalArgumentException("Conversation may have only 20 participants max.");
        }

        Participant admin = conversation.getAdmin();

        if ((admin == null || !admin.equals(adder))) {
            throw new IllegalArgumentException("Participants can be added only by admin.");
        }

        conversation.addParticipant(added);
        return conversationRepository.saveAndFlush(conversation);
    }
}
