package club.tempvs.message.dto;

import java.util.HashSet;
import java.util.Set;

public class CreateConversationDto {

    private Long sender;
    private Set<Long> receivers = new HashSet<>();
    private String text;
    private String name;

    public CreateConversationDto() {

    }

    public CreateConversationDto(Long sender, Set<Long> receivers, String text, String name) {
        this.sender = sender;
        this.receivers = receivers;
        this.text = text;
        this.name = name;
    }

    public Long getSender() {
        return sender;
    }

    public void setSender(Long sender) {
        this.sender = sender;
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