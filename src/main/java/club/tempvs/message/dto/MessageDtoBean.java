package club.tempvs.message.dto;

import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;

import java.time.Instant;

public class MessageDtoBean {

    private Long id;
    private String text;
    private Long author;
    private Long subject;
    private Instant createdDate;
    private Boolean isUnread;
    private Boolean isSystem;

    public MessageDtoBean() {

    }

    public MessageDtoBean(Message message, Participant self) {
        Participant subject = message.getSubject();

        this.id = message.getId();
        this.text = message.getText();
        this.author = message.getAuthor().getId();
        this.subject = subject != null ? subject.getId() : null;
        this.createdDate = message.getCreatedDate();
        this.isUnread = message.getNewFor().contains(self);
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

    public Long getSubject() {
        return subject;
    }

    public void setSubject(Long subject) {
        this.subject = subject;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getUnread() {
        return isUnread;
    }

    public void setUnread(Boolean unread) {
        isUnread = unread;
    }

    public Boolean getSystem() {
        return isSystem;
    }

    public void setSystem(Boolean system) {
        isSystem = system;
    }
}
