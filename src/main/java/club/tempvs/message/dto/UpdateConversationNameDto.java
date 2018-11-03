package club.tempvs.message.dto;

import club.tempvs.message.api.BadRequestException;

import java.util.ArrayList;
import java.util.List;

public class UpdateConversationNameDto implements Validateable {

    private String name;
    private ParticipantDto initiator;

    public UpdateConversationNameDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ParticipantDto getInitiator() {
        return initiator;
    }

    public void setInitiator(ParticipantDto initiator) {
        this.initiator = initiator;
    }

    public void validate() {
        List<String> errors = new ArrayList<>();

        if (this.initiator == null) {
            errors.add("Initiator is missing.");
        } else {
            try {
                initiator.validate();
            } catch (Exception e) {
                errors.add(e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new BadRequestException(String.join("\n", errors));
        }
    }
}
