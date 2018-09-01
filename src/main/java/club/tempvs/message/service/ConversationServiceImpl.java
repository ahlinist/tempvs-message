package club.tempvs.message.service;

import club.tempvs.message.*;
import club.tempvs.message.dao.ConversationRepository;
import club.tempvs.message.util.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ConversationServiceImpl implements ConversationService {

    private final ObjectFactory objectFactory;
    private final ConversationRepository conversationRepository;
    private final MessageService messageService;

    @Autowired
    public ConversationServiceImpl(
            ObjectFactory objectFactory, ConversationRepository conversationRepository, MessageService messageService) {
        this.objectFactory = objectFactory;
        this.conversationRepository = conversationRepository;
        this.messageService = messageService;
    }

    public Conversation createConversation(
            Participant sender, Set<Participant> receivers, String text, String name) {
        Conversation conversation = objectFactory.getInstance(Conversation.class);
        conversation.setParticipants(receivers);
        conversation.addParticipant(sender);
        conversation.setName(name);

        Message message = messageService.createMessage(conversation, sender, receivers, text);
        conversation.addMessage(message);
        return conversationRepository.save(conversation);
    }

    public Conversation getConversation(Long id) {
        return conversationRepository.findById(id).orElse(null);
    }

    public Conversation addParticipants(Conversation conversation, Set<Participant> participantsToAdd) {
        Set<Participant> participants = conversation.getParticipants();
        participants.addAll(participantsToAdd);

        for (Participant addedParticipant : participantsToAdd) {
            addedParticipant.addConversation(conversation);
        }

        return conversationRepository.save(conversation);
    }

    public Conversation addMessage(Conversation conversation, Participant sender, Set<Participant> receivers, String text) {
        Message message = messageService.createMessage(conversation, sender, receivers, text);
        conversation.addMessage(message);
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
