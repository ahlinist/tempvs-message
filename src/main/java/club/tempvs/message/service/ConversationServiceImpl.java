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
            Participant sender, Set<Participant> receivers, String messageText, String conversationName) {
        Conversation conversation = objectFactory.getInstance(Conversation.class);
        conversation.setParticipants(receivers);
        conversation.addParticipant(sender);
        conversation.setName(conversationName);

        Message message = messageService.createMessage(conversation, sender, receivers, messageText);
        conversation.addMessage(message);
        return conversationRepository.save(conversation);
    }

    public Conversation getConversation(Long id) {
        throw new RuntimeException("not implemented yet");
    }
}
