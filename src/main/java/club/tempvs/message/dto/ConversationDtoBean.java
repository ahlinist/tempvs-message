package club.tempvs.message.dto;

import club.tempvs.message.domain.Conversation;

import java.util.Objects;

public class ConversationDtoBean {

    private Long id;
    private String name;
    private MessageDtoBean lastMessage;

    public ConversationDtoBean() {

    }

    public ConversationDtoBean(Conversation conversation) {
        this.id = conversation.getId();
        this.name = conversation.getName();
        this.lastMessage = new MessageDtoBean(conversation.getLastMessage());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConversationDtoBean that = (ConversationDtoBean) o;
        return id.equals(that.id) &&
                name.equals(that.name) &&
                lastMessage.equals(that.lastMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, lastMessage);
    }
}
