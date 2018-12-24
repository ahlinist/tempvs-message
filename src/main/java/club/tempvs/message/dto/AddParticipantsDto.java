package club.tempvs.message.dto;

import java.util.Set;

public class AddParticipantsDto {

    private Set<ParticipantDto> subjects;

    public AddParticipantsDto() {

    }

    public Set<ParticipantDto> getSubjects() {
        return subjects;
    }

    public void setSubjects(Set<ParticipantDto> subjects) {
        this.subjects = subjects;
    }
}
