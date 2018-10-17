package club.tempvs.message.dto;

import club.tempvs.message.api.BadRequestException;
import club.tempvs.message.domain.Participant;

import java.util.ArrayList;
import java.util.List;

public class ParticipantDto implements Validateable {

    Long id;
    String name;

    public ParticipantDto() {

    }

    public ParticipantDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public ParticipantDto(Participant participant) {
        this.id = participant.getId();
        this.name = participant.getName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void validate() {
        List<String> errors = new ArrayList<>();

        if (this.id == null) {
            errors.add("Participant id is missing.");
        }

        if (this.name == null || this.name.isEmpty()) {
            errors.add("Participant name is missing.");
        }

        if (!errors.isEmpty()) {
            throw new BadRequestException(String.join("\n", errors));
        }
    }
}
