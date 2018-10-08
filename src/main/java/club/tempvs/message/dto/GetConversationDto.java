package club.tempvs.message.dto;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;

import java.util.*;

import static java.util.stream.Collectors.*;

public class GetConversationDto {

    private Long id;
    private String type;
    private Long admin;
    private Set<Long> participants = new HashSet<>();
    private MessageDtoBean lastMessage;
    private List<MessageDtoBean> messages = new ArrayList<>();

    public GetConversationDto() {

    }

    public GetConversationDto(Conversation conversation, List<Message> messages) {
        Collections.reverse(messages);

        this.id = conversation.getId();
        this.type = conversation.getType().toString();
        this.admin = conversation.getAdmin().getId();
        this.participants = conversation.getParticipants().stream().map(Participant::getId).collect(toSet());
        this.lastMessage = new MessageDtoBean(conversation.getLastMessage());
        this.messages = messages.stream().map(MessageDtoBean::new).collect(toList());
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

    public Long getAdmin() {
        return admin;
    }

    public void setAdmin(Long admin) {
        this.admin = admin;
    }

    public Set<Long> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<Long> participants) {
        this.participants = participants;
    }

    public MessageDtoBean getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(MessageDtoBean lastMessage) {
        this.lastMessage = lastMessage;
    }

    public List<MessageDtoBean> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDtoBean> messages) {
        this.messages = messages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GetConversationDto that = (GetConversationDto) o;
        return Objects.equals(id, that.id) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }
}
