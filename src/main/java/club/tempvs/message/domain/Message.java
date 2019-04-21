package club.tempvs.message.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
@Entity
@EqualsAndHashCode(of = {"id", "conversation"})
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue
    private Long id;
    private Boolean isSystem = false;
    private String systemArgs;

    @NotNull
    @ManyToOne
    private Conversation conversation;

    @NotNull
    @OneToOne
    private Participant author;

    @OneToOne
    private Participant subject;

    @NotBlank
    private String text;

    private Instant createdDate = Instant.now();

    public Message(Message message) {
        this.id = message.getId();
        this.isSystem = message.getIsSystem();
        this.systemArgs = message.getSystemArgs();
        this.conversation = message.getConversation();
        this.author = message.getAuthor();
        this.subject = message.getSubject();
        this.text = message.getText();
        this.createdDate = message.getCreatedDate();
    }

    @Override
    public String toString() {
        return text;
    }
}
