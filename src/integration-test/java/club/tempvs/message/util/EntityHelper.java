package club.tempvs.message.util;

import club.tempvs.message.dao.ConversationRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.service.MessageService;
import club.tempvs.message.service.ParticipantService;
import club.tempvs.message.service.impl.ConversationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class EntityHelper {

    private ParticipantService participantService;
    private MessageService messageService;
    private ConversationServiceImpl conversationService;
    private ConversationRepository conversationRepository;

    @Autowired
    public EntityHelper(ParticipantService participantService, MessageService messageService,
                        ConversationServiceImpl conversationService, ConversationRepository conversationRepository) {
        this.participantService = participantService;
        this.conversationService = conversationService;
        this.conversationRepository = conversationRepository;
        this.messageService = messageService;
    }

    public Conversation createConversation(Participant author, Set<Participant> receivers, String text, String name) {
        Message message = messageService.createMessage(author, receivers, text, false, null, null);
        Conversation conversation = conversationService.buildConversation(author, receivers, name, message);
        return conversationRepository.save(conversation);
    }

    public Participant createParticipant(Long id, String name, String type, String period) {
        return participantService.createParticipant(id, name, type, period);
    }
}
