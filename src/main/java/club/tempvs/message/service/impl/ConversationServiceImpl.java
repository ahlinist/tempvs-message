package club.tempvs.message.service.impl;

import club.tempvs.message.api.BadRequestException;
import club.tempvs.message.dao.ConversationRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.service.MessageService;
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

    private static final String PARTICIPANT_ADDED_MESSAGE = "conversation.add.participant";
    private static final String PARTICIPANT_REMOVED_MESSAGE = "conversation.remove.participant";
    private static final String PARTICIPANT_SELFREMOVED_MESSAGE = "conversation.selfremove.participant";

    private final ObjectFactory objectFactory;
    private final ConversationRepository conversationRepository;
    private final MessageService messageService;

    @Autowired
    public ConversationServiceImpl(
            ObjectFactory objectFactory, MessageService messageService, ConversationRepository conversationRepository) {
        this.objectFactory = objectFactory;
        this.messageService = messageService;
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

    public Conversation addParticipant(Conversation conversation, Participant adder, Participant added) {
        Set<Participant> participants = conversation.getParticipants();

        if (participants.size() == 20) {
            throw new BadRequestException("Conversation may have only 20 participants max.");
        }

        Participant admin = conversation.getAdmin();

        if ((admin == null || !admin.equals(adder))) {
            throw new BadRequestException("Participants can be added only by admin.");
        }

        conversation.addParticipant(added);
        Boolean isSystem = Boolean.TRUE;
        Message message = messageService.createMessage(
                conversation, adder, participants, PARTICIPANT_ADDED_MESSAGE, isSystem, added);
        return addMessage(conversation, message);
    }

    public Conversation removeParticipant(Conversation conversation, Participant remover, Participant removed) {
        Set<Participant> participants = conversation.getParticipants();

        if (participants.size() <= 2) {
            throw new BadRequestException("Conversation has only 2 participants. One can't delete one of them.");
        }

        Participant admin = conversation.getAdmin();
        boolean isSelfRemoval = removed.equals(remover);

        if ((admin == null || !admin.equals(remover)) && !isSelfRemoval) {
            throw new BadRequestException("Participants can be removed only by admin or by themselves.");
        }

        conversation.removeParticipant(removed);
        Boolean isSystem = Boolean.TRUE;

        Message message;

        if (isSelfRemoval) {
            message = messageService.createMessage(
                    conversation, remover, participants, PARTICIPANT_SELFREMOVED_MESSAGE, isSystem);

        } else {
            message = messageService.createMessage(
                    conversation, remover, participants, PARTICIPANT_REMOVED_MESSAGE, isSystem, removed);
        }

        return addMessage(conversation, message);
    }
}
