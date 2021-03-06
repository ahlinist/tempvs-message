package club.tempvs.message.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.*;

@Data
@Entity
@EqualsAndHashCode(of = {"id"})
@EntityListeners(AuditingEntityListener.class)
public class Conversation {

    @Id
    @GeneratedValue
    private Long id;
    private String name;

    @NotNull
    private Type type;

    @NotEmpty
    @OrderColumn
    @OneToMany(cascade = ALL)
    private List<Message> messages = new ArrayList<>();

    @OneToOne
    private Participant admin;

    private String lastMessageText;
    private String lastMessageAuthorName;
    private String lastMessageSubjectName;
    private Instant lastMessageCreatedDate;
    private Boolean lastMessageSystem;
    private String lastMessageSystemArgs;

    @Size(min = 2, max = 20)
    @ManyToMany(fetch = EAGER)
    private Set<Participant> participants = new LinkedHashSet<>();

    //TODO: replace with separate table + separate query
    @ElementCollection
    private Map<Participant, Instant> lastReadOn = new HashMap<>();

    @CreatedDate
    private Instant createdDate;

    private transient Long unreadMessagesCount;

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void addParticipant(Participant participant) {
        this.participants.add(participant);
    }

    public void removeParticipant(Participant participant) {
        this.participants.remove(participant);
    }

    public enum Type {
        DIALOGUE, CONFERENCE
    }
}
