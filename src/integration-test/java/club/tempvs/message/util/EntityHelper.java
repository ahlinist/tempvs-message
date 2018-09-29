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

    @Autowired
    private ParticipantService participantService;
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private MessageService messageService;

    public Conversation createConversation(Long senderId, Set<Long> receiverIds, String text, String name) {
        Participant sender = participantService.createParticipant(senderId);
        Set<Participant> receivers = receiverIds.stream().map(participantService::createParticipant).collect(toSet());
        Message message = messageService.createMessage(sender, receivers, text, false);
        return conversationService.createConversation(sender, receivers, name, message);
    }
}
