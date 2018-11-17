package club.tempvs.message.dto;

import club.tempvs.message.api.BadRequestException;

import java.util.ArrayList;
import java.util.List;

public class UpdateParticipantsDto implements Validateable {

    private ParticipantDto initiator;
    private ParticipantDto subject;

    public UpdateParticipantsDto() {

    }

    public ParticipantDto getInitiator() {
        return initiator;
    }

    public void setInitiator(ParticipantDto initiator) {
        this.initiator = initiator;
    }

    public ParticipantDto getSubject() {
        return subject;
    }

    public void setSubject(ParticipantDto subject) {
        this.subject = subject;
    }

    public void validate() {
        List<String> errors = new ArrayList<>();

        if (this.initiator == null) {
            errors.add("Initiator id is missing.");
        }

        if (this.subject == null) {
            errors.add("Subject id is missing.");
        }

        if (!errors.isEmpty()) {
            throw new BadRequestException(String.join("\n", errors));
        }
    }
}
