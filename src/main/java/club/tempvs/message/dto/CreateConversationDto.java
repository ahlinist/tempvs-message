package club.tempvs.message.dto;

import club.tempvs.message.api.BadRequestException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreateConversationDto implements Validateable {

    private ParticipantDto author;
    private Set<ParticipantDto> receivers = new HashSet<>();
    private String text;
    private String name;

    public CreateConversationDto() {

    }

    public ParticipantDto getAuthor() {
        return author;
    }

    public void setAuthor(ParticipantDto author) {
        this.author = author;
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

    public void validate() {
        List<String> errors = new ArrayList<>();

        if (this.author == null) {
            errors.add("Author id is missing.");
        } else {
            try {
                author.validate();
            } catch (Exception e) {
                errors.add(e.getMessage());
            }
        }

        if (this.receivers == null || this.receivers.isEmpty()) {
            errors.add("Receivers list is empty.");
        } else {
            for (ParticipantDto receiver : receivers) {
                try {
                    receiver.validate();
                } catch (Exception e) {
                    errors.add(e.getMessage());
                }
            }
        }

        if (this.text == null || this.text.isEmpty()) {
            errors.add("Text is missing.");
        }

        if (!errors.isEmpty()) {
            throw new BadRequestException(String.join("\n", errors));
        }
    }
}
