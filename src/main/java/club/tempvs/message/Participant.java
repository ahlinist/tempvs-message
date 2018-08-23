package club.tempvs.message;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Participant {

    @Id
    @GeneratedValue
    private Long id;
    @ManyToMany(cascade = CascadeType.MERGE)
    private List<Conversation> conversations = new ArrayList<>();
    @OneToMany
    private List<Message> receivedMessages = new ArrayList<>();
    @OneToMany
    private List<Message> sentMessages = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Conversation> getConversations() {
        return conversations;
    }

    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    public List<Message> getReceivedMessages() {
        return receivedMessages;
    }

    public void setReceivedMessages(List<Message> receivedMessages) {
        this.receivedMessages = receivedMessages;
    }

    public List<Message> getSentMessages() {
        return sentMessages;
    }

    public void setSentMessages(List<Message> sentMessages) {
        this.sentMessages = sentMessages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(conversations, that.conversations) &&
                Objects.equals(receivedMessages, that.receivedMessages) &&
                Objects.equals(sentMessages, that.sentMessages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, conversations, receivedMessages, sentMessages);
    }
}
