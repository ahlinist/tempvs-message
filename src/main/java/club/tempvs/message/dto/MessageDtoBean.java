package club.tempvs.message.dto;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import lombok.Data;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Data
public class MessageDtoBean {

    private Long id;
    private String text;
    private ParticipantDto author;
    private ParticipantDto subject;
    private String createdDate;
    private Boolean unread;
    private Boolean system;

    public MessageDtoBean(Message message, Participant participant, Conversation conversation, String zoneId) {
        Participant subject = message.getSubject();
        Instant lastReadOn = conversation.getLastReadOn().getOrDefault(participant, Instant.MIN);

        this.id = message.getId();
        this.text = message.getText();
        this.author = new ParticipantDto(message.getAuthor());
        this.subject = subject != null ? new ParticipantDto(subject) : null;
        this.createdDate = parseDate(message.getCreatedDate(), zoneId);
        this.unread = message.getCreatedDate().isAfter(lastReadOn);
        this.system = message.getIsSystem();
    }

    public MessageDtoBean(Participant participant, Conversation conversation, String zoneId) {
        String subjectName = conversation.getLastMessageSubjectName();
        Instant lastReadOn = conversation.getLastReadOn().getOrDefault(participant, Instant.MIN);

        this.text = conversation.getLastMessageText();
        this.author = new ParticipantDto(conversation.getLastMessageAuthorName());
        this.subject = isNotBlank(subjectName) ? new ParticipantDto(subjectName) : null;
        this.createdDate = parseDate(conversation.getLastMessageCreatedDate(), zoneId);
        this.unread = conversation.getLastMessageCreatedDate().isAfter(lastReadOn);
        this.system = conversation.getLastMessageSystem();
    }

    private String parseDate(Instant instant, String zoneId) {
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of(zoneId));
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(LocaleContextHolder.getLocale());
        return zonedDateTime.format(formatter);
    }
}
