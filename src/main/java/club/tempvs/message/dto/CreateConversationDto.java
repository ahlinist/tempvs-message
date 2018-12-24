package club.tempvs.message.dto;

import java.util.*;

public class CreateConversationDto {

    private Set<Long> receivers = new LinkedHashSet<>();
    private String text;
    private String name;

    public CreateConversationDto() {

    }

    public Set<Long> getReceivers() {
        return receivers;
    }

    public void setReceivers(Set<Long> receivers) {
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
