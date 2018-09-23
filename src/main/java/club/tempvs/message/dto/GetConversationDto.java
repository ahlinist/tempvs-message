package club.tempvs.message.dto;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Participant;

import java.util.*;

import static java.util.stream.Collectors.*;

public class GetConversationDto {

    private Long id;
    private Long admin;
    private Set<Long> participants = new HashSet<>();
    private MessageDto lastMessage;
    private List<MessageDto> messages = new ArrayList<>();

    public GetConversationDto() {

    }

    public GetConversationDto(Conversation conversation) {
        this.id = conversation.getId();
        this.admin = conversation.getAdmin().getId();
        this.participants = conversation.getParticipants().stream().map(Participant::getId).collect(toSet());
        this.lastMessage = new MessageDto(conversation.getLastMessage());
        this.messages = conversation.getMessages().stream().map(MessageDto::new).collect(toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public MessageDto getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(MessageDto lastMessage) {
        this.lastMessage = lastMessage;
    }

    public List<MessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDto> messages) {
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
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
