package club.tempvs.message.dto;

import club.tempvs.message.api.BadRequestException;
import club.tempvs.message.domain.Participant;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ParticipantDto implements Validateable {

    Long id;
    String name;
    String type;
    String period;

    public ParticipantDto() {

    }

    public ParticipantDto(Long id, String name, String type, String period) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.period = period;
    }

    public ParticipantDto(Participant participant) {
        this.id = participant.getId();
        this.name = participant.getName();
        this.type = participant.getType();
        this.period = participant.getPeriod();
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipantDto that = (ParticipantDto) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
