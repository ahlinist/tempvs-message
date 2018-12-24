package club.tempvs.message.dto;

import java.util.Set;

public class AddParticipantsDto {

    private Set<Long> participants;

    public AddParticipantsDto() {

    }

    public Set<Long> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<Long> participants) {
        this.participants = participants;
    }
}
