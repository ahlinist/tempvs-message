package club.tempvs.message.dto;

import java.util.Set;

public class AddParticipantDto {

    private Long conversation;
    private Set<Long> participants;

    public AddParticipantDto() {

    }

    public Long getConversation() {
        return conversation;
    }

    public void setConversation(Long conversation) {
        this.conversation = conversation;
    }

    public Set<Long> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<Long> participants) {
        this.participants = participants;
    }
}
