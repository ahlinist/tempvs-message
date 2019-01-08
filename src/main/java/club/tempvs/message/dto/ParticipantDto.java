package club.tempvs.message.dto;

import club.tempvs.message.domain.Participant;
import lombok.Data;

@Data
public class ParticipantDto {

    Long id;
    String name;
    String type;
    String period;

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
}
