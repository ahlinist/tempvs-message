package club.tempvs.message.dto;

import club.tempvs.message.api.BadRequestException;

import java.util.ArrayList;
import java.util.List;

public class AddMessageDto implements Validateable {

    private Long author;
    private String text;
    private Boolean isSystem = Boolean.FALSE;

    public AddMessageDto() {

    }

    public Long getAuthor() {
        return author;
    }

    public void setAuthor(Long author) {
        this.author = author;
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

        if (this.author == null) {
            errors.add("Author id is missing.");
        }

        if (this.text == null || this.text.isEmpty()) {
            errors.add("Text is missing.");
        }

        if (!errors.isEmpty()) {
            throw new BadRequestException(String.join("\n", errors));
        }
    }
}
