package club.tempvs.message.dto;

import club.tempvs.message.api.BadRequestException;

import java.util.ArrayList;
import java.util.List;

public class AddParticipantsDto implements Validateable {

    private ParticipantDto initiator;
    private List<ParticipantDto> subjects;

    public AddParticipantsDto() {

    }

    public ParticipantDto getInitiator() {
        return initiator;
    }

    public void setInitiator(ParticipantDto initiator) {
        this.initiator = initiator;
    }

    public List<ParticipantDto> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<ParticipantDto> subjects) {
        this.subjects = subjects;
    }

    public void validate() {
        List<String> errors = new ArrayList<>();

        if (this.initiator == null) {
            errors.add("Initiator id is missing.");
        }

        if (this.subjects == null || this.subjects.isEmpty()) {
            errors.add("Subjects are missing.");
        }

        if (!errors.isEmpty()) {
            throw new BadRequestException(String.join("\n", errors));
        }
    }
}
