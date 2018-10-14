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

import static java.util.stream.Collectors.toSet;

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

    public Conversation createConversation(Long authorId, Set<Long> receiverIds, String text, String name) {
        Participant author = participantService.createParticipant(authorId, "");
        Set<Participant> receivers = receiverIds.stream().map(id -> participantService.createParticipant(id, "")).collect(toSet());
        Message message = messageService.createMessage(author, receivers, text, false);
        return conversationService.createConversation(author, receivers, name, message);
    }

    public Participant createParticipant(Long id, String name) {
        return participantService.createParticipant(id, name);
    }
}
