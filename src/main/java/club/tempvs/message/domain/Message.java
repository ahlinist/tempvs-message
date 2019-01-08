package club.tempvs.message.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.*;

@Data
@Entity
@EqualsAndHashCode(of = {"id", "conversation"})
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Message {

    @Id
    @GeneratedValue
    private Long id;
    private Boolean system = false;
    private String systemArgs;

    @NotNull
    @ManyToOne
    private Conversation conversation;

    @NotNull
    @OneToOne
    private Participant author;

    @OneToOne
    private Participant subject;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Participant> newFor = new HashSet<>();

    @NotBlank
    private String text;

    @CreatedDate
    private Instant createdDate;

    public Message(Message message) {
        this.id = message.getId();
        this.system = message.getSystem();
        this.systemArgs = message.getSystemArgs();
        this.conversation = message.getConversation();
        this.author = message.getAuthor();
        this.subject = message.getSubject();
        this.newFor = message.getNewFor();
        this.text = message.getText();
        this.createdDate = message.getCreatedDate();
    }

    @Override
    public String toString() {
        return text;
    }
}
