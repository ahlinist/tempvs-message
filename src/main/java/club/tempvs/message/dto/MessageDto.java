package club.tempvs.message.dto;

import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;

import java.time.LocalDateTime;
import java.util.*;
import static java.util.stream.Collectors.toSet;

public class MessageDto {

    private String text;
    private Long author;
    private LocalDateTime createdDate;
    private Set<Long> newFor = new HashSet<>();

    public MessageDto() {

    }

    public MessageDto(Message message) {
        this.text = message.getText();
        this.author = message.getSender().getId();
        this.createdDate = message.getCreatedDate();
        this.newFor = message.getNewFor().stream().map(Participant::getId).collect(toSet());
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getAuthor() {
        return author;
    }

    public void setAuthor(Long author) {
        this.author = author;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Set<Long> getNewFor() {
        return newFor;
    }

    public void setNewFor(Set<Long> newFor) {
        this.newFor = newFor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MessageDto that = (MessageDto) o;
        return Objects.equals(text, that.text) &&
                Objects.equals(author, that.author) &&
                Objects.equals(createdDate, that.createdDate) &&
                Objects.equals(newFor, that.newFor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, author, createdDate, newFor);
    }
}
