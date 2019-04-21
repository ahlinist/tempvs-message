package club.tempvs.message.dto;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Participant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "type"})
public class ConversationDtoBean {

    private Long id;
    private String type;
    private String name;
    private MessageDtoBean lastMessage;
    private String conversant;
    private Long unreadMessagesCount;

    public ConversationDtoBean(Conversation conversation, Participant self, String zoneId) {
        this.id = conversation.getId();
        this.name = conversation.getName();
        this.type = conversation.getType().toString();
        this.lastMessage = new MessageDtoBean(conversation.getLastMessage(), self, zoneId);
        this.conversant = conversation.getParticipants().stream().filter(participant -> !participant.equals(self))
                .map(Participant::getName).collect(Collectors.joining(", "));
        this.unreadMessagesCount = conversation.getUnreadMessagesCount();
    }
}
