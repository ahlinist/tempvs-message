package club.tempvs.message.util;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.service.MessageService;
import club.tempvs.message.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class EntityHelper {

    private ParticipantService participantService;
    private ConversationService conversationService;
    private MessageService messageService;

    @Autowired
    public EntityHelper(ParticipantService participantService,
                        ConversationService conversationService, MessageService messageService) {
        this.participantService = participantService;
        this.conversationService = conversationService;
        this.messageService = messageService;
    }

    public Conversation createConversation(Participant author, Set<Participant> receivers, String text, String name) {
        Message message = messageService.createMessage(author, receivers, text, false, null, null);
        return conversationService.createConversation(author, receivers, name, message);
    }

    public Participant createParticipant(Long id, String name, String type, String period) {
        return participantService.createParticipant(id, name, type, period);
    }
}
