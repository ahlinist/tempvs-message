package club.tempvs.message.dto;

import club.tempvs.message.domain.Participant;

public class ParticipantDto {

    Long id;
    String name;

    public ParticipantDto() {

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
}
