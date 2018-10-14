package club.tempvs.message.dto;

public class ParticipantDto {

    Long id;
    String name;

    public ParticipantDto() {

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
