package club.tempvs.message.dto;

import java.util.Set;

public class AddParticipantsDto {

    private ParticipantDto initiator;
    private Set<ParticipantDto> subjects;

    public AddParticipantsDto() {

    }

    public ParticipantDto getInitiator() {
        return initiator;
    }

    public void setInitiator(ParticipantDto initiator) {
        this.initiator = initiator;
    }

    public Set<ParticipantDto> getSubjects() {
        return subjects;
    }

    public void setSubjects(Set<ParticipantDto> subjects) {
        this.subjects = subjects;
    }
}
