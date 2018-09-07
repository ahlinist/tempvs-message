package club.tempvs.message.dto;

import club.tempvs.message.Conversation;
import club.tempvs.message.Participant;

import java.util.*;

import static java.util.stream.Collectors.*;

public class GetConversationDto {

    private Long id;
    private Set<Long> participants = new HashSet<>();
    private List<MessageDto> messages = new ArrayList<>();

    public GetConversationDto() {

    }

    public GetConversationDto(Conversation conversation) {
        this.id = conversation.getId();
        this.participants = conversation.getParticipants().stream().map(Participant::getId).collect(toSet());
        this.messages = conversation.getMessages().stream().map(MessageDto::new).collect(toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Long> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<Long> participants) {
        this.participants = participants;
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
        return Objects.equals(id, that.id) &&
                Objects.equals(participants, that.participants) &&
                Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, participants, messages);
    }
}
