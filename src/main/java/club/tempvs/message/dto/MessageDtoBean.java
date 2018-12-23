package club.tempvs.message.dto;

import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class MessageDtoBean {

    private Long id;
    private String text;
    private ParticipantDto author;
    private ParticipantDto subject;
    private String createdDate;
    private Boolean isUnread;
    private Boolean isSystem;

    public MessageDtoBean() {

    }

    public MessageDtoBean(Message message, Participant self, String zoneId) {
        Participant subject = message.getSubject();

        this.id = message.getId();
        this.text = message.getText();
        this.author = new ParticipantDto(message.getAuthor());
        this.subject = subject != null ? new ParticipantDto(subject) : null;
        this.createdDate = parseDate(message.getCreatedDate(), zoneId);
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

    public ParticipantDto getAuthor() {
        return author;
    }

    public void setAuthor(ParticipantDto author) {
        this.author = author;
    }

    public ParticipantDto getSubject() {
        return subject;
    }

    public void setSubject(ParticipantDto subject) {
        this.subject = subject;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
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

    private String parseDate(Instant instant, String zoneId) {
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of(zoneId));
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(LocaleContextHolder.getLocale());
        return zonedDateTime.format(formatter);
    }
}
