package club.tempvs.message.dto;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Participant;

import java.util.*;
import java.util.stream.Collectors;

public class ConversationDtoBean {

    private Long id;
    private String type;
    private String name;
    private MessageDtoBean lastMessage;
    private String conversant;
    private Long unreadMessagesCount;

    public ConversationDtoBean() {

    }

    public ConversationDtoBean(Conversation conversation, Participant self, String zoneId, Locale locale) {
        this.id = conversation.getId();
        this.name = conversation.getName();
        this.type = conversation.getType().toString();
        this.lastMessage = new MessageDtoBean(conversation.getLastMessage(), self, zoneId);
        this.conversant = conversation.getParticipants().stream().filter(participant -> !participant.equals(self))
                .map(Participant::getName).collect(Collectors.joining(", "));
        this.unreadMessagesCount = conversation.getUnreadMessagesCount();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MessageDtoBean getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(MessageDtoBean lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getConversant() {
        return conversant;
    }

    public void setConversant(String conversant) {
        this.conversant = conversant;
    }

    public Long getUnreadMessagesCount() {
        return unreadMessagesCount;
    }

    public void setUnreadMessagesCount(Long unreadMessagesCount) {
        this.unreadMessagesCount = unreadMessagesCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConversationDtoBean that = (ConversationDtoBean) o;
        return id.equals(that.id) && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }
}
