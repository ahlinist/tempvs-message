package club.tempvs.message.dto;

import club.tempvs.message.api.BadRequestException;

import java.util.ArrayList;
import java.util.List;

public class UpdateParticipantsDto implements Validateable {

    private Long initiator;
    private Long subject;
    private Action action;

    public UpdateParticipantsDto() {

    }

    public Long getInitiator() {
        return initiator;
    }

    public void setInitiator(Long initiator) {
        this.initiator = initiator;
    }

    public Long getSubject() {
        return subject;
    }

    public void setSubject(Long subject) {
        this.subject = subject;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public enum Action {
        ADD, REMOVE
    }

    public void validate() {
        List<String> errors = new ArrayList<>();

        if (this.initiator == null) {
            errors.add("Initiator id is missing.");
        }

        if (this.subject == null) {
            errors.add("Subject id is missing.");
        }

        if (this.action == null) {
            errors.add("Action is not specified.");
        }

        if (!errors.isEmpty()) {
            throw new BadRequestException(String.join("\n", errors));
        }
    }
}
