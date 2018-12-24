package club.tempvs.message.dto;

import java.util.*;

public class CreateConversationDto {

    private Set<ParticipantDto> receivers = new LinkedHashSet<>();
    private String text;
    private String name;

    public CreateConversationDto() {

    }

    public Set<ParticipantDto> getReceivers() {
        return receivers;
    }

    public void setReceivers(Set<ParticipantDto> receivers) {
        this.receivers = receivers;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
