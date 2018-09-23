package club.tempvs.message.service.impl;

import club.tempvs.message.dao.ConversationRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.util.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            Participant sender, Set<Participant> receivers, String name, Message message) {
        Conversation conversation = objectFactory.getInstance(Conversation.class);
        conversation.setParticipants(receivers);
        conversation.addParticipant(sender);
        conversation.setName(name);
        conversation.addMessage(message);
        message.setConversation(conversation);

        if (conversation.getParticipants().size() > 2) {
            conversation.setAdmin(sender);
        }

        return conversationRepository.saveAndFlush(conversation);
    }

    public Conversation getConversation(Long id) {
        return conversationRepository.findById(id).get();
    }

    public Conversation addMessage(Conversation conversation, Message message) {
        conversation.addMessage(message);
        message.setConversation(conversation);
        return conversationRepository.save(conversation);
    }

    public Conversation removeParticipant(Conversation conversation, Participant participant) {
        Set<Participant> participants = conversation.getParticipants();

        if (participants.size() > 2) {
            participants.remove(participant);
            return conversationRepository.save(conversation);
        }

        return conversation;
    }
}
