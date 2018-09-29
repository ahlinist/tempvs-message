package club.tempvs.message.dto;

import java.util.ArrayList;
import java.util.List;

public class AddMessageDto implements ValidateableDto {

    private Long conversation;
    private Long sender;
    private String text;
    private Boolean isSystem = Boolean.FALSE;

    public AddMessageDto() {

    }

    public Long getConversation() {
        return conversation;
    }

    public void setConversation(Long conversation) {
        this.conversation = conversation;
    }

    public Long getSender() {
        return sender;
    }

    public void setSender(Long sender) {
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getSystem() {
        return isSystem;
    }

    public void setSystem(Boolean system) {
        isSystem = system;
    }

    public void validate() {
        List<String> errors = new ArrayList<>();

        if (this.conversation == null) {
            errors.add("Conversation id is missing.");
        }

        if (this.sender == null) {
            errors.add("Sender id is missing.");
        }

        if (this.text == null || this.text.isEmpty()) {
            errors.add("Text is missing.");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }
    }
}
