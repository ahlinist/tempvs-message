package club.tempvs.message.dto;

import club.tempvs.message.domain.Conversation;

import java.util.Objects;

public class ConversationDto {

    private Long id;
    private String name;
    private MessageDto lastMessage;

    public ConversationDto() {

    }

    public ConversationDto(Conversation conversation) {
        this.id = conversation.getId();
        this.name = conversation.getName();
        this.lastMessage = new MessageDto(conversation.getLastMessage());
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

    public MessageDto getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(MessageDto lastMessage) {
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

        ConversationDto that = (ConversationDto) o;
        return id.equals(that.id) &&
                name.equals(that.name) &&
                lastMessage.equals(that.lastMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, lastMessage);
    }
}
