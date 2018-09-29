package club.tempvs.message.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreateConversationDto implements ValidateableDto {

    private Long sender;
    private Set<Long> receivers = new HashSet<>();
    private String text;
    private String name;

    public CreateConversationDto() {

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

    public void validate() {
        List<String> errors = new ArrayList<>();

        if (this.sender == null) {
            errors.add("Sender id is missing.");
        }

        if (this.receivers == null || this.receivers.isEmpty()) {
            errors.add("Receivers list is empty.");
        }

        if (this.text == null || this.text.isEmpty()) {
            errors.add("Text is missing.");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }
    }
}
