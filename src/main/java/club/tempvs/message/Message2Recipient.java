package club.tempvs.message;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.Objects;

@Entity
public class Message2Recipient {

    @Id
    @GeneratedValue
    private Long id;
    @OneToOne
    private Participant recipient;
    @OneToOne
    private Message message;
    private Boolean seen = Boolean.FALSE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Participant getRecipient() {
        return recipient;
    }

    public void setRecipient(Participant recipient) {
        this.recipient = recipient;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message2Recipient that = (Message2Recipient) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(recipient, that.recipient) &&
                Objects.equals(message, that.message) &&
                Objects.equals(seen, that.seen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, recipient, message, seen);
    }
}
