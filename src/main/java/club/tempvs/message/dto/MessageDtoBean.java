package club.tempvs.message.dto;

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

    public MessageDtoBean(Message message, Participant self, String zoneId) {
        Participant subject = message.getSubject();

        this.id = message.getId();
        this.text = message.getText();
        this.author = new ParticipantDto(message.getAuthor());
        this.subject = subject != null ? new ParticipantDto(subject) : null;
        this.createdDate = parseDate(message.getCreatedDate(), zoneId);
        this.unread = message.getNewFor().contains(self);
        this.system = message.getSystem();
    }

    private String parseDate(Instant instant, String zoneId) {
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of(zoneId));
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(LocaleContextHolder.getLocale());
        return zonedDateTime.format(formatter);
    }
}
