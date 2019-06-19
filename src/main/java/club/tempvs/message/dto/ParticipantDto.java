package club.tempvs.message.dto;

import club.tempvs.message.domain.Participant;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ParticipantDto {

    Long id;
    String name;
    String type;
    String period;

    public ParticipantDto(Participant participant) {
        this.id = participant.getId();
        this.name = participant.getName();
        this.type = participant.getType();
        this.period = participant.getPeriod();
    }

    public ParticipantDto(String name) {
        this.name = name;
    }
}
