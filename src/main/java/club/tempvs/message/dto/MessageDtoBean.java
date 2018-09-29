package club.tempvs.message.dto;

import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;

import java.time.Instant;
import java.util.*;
import static java.util.stream.Collectors.toSet;

public class MessageDtoBean {

    private Long id;
    private String text;
    private Long author;
    private Instant createdDate;
    private Set<Long> newFor = new HashSet<>();
    private Boolean isSystem;

    public MessageDtoBean() {

    }

    public MessageDtoBean(Message message) {
        this.id = message.getId();
        this.text = message.getText();
        this.author = message.getSender().getId();
        this.createdDate = message.getCreatedDate();
        this.newFor = message.getNewFor().stream().map(Participant::getId).collect(toSet());
        this.isSystem = message.getSystem();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Set<Long> getNewFor() {
        return newFor;
    }

    public void setNewFor(Set<Long> newFor) {
        this.newFor = newFor;
    }

    public Boolean getSystem() {
        return isSystem;
    }

    public void setSystem(Boolean system) {
        isSystem = system;
    }
}
