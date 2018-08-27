package club.tempvs.message;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@Entity
public class Message {

    @Id
    @GeneratedValue
    private Long id;
    @NotNull
    @ManyToOne(cascade = CascadeType.ALL)
    private Conversation conversation;
    @NotNull
    @OneToOne
    private Participant sender;
    @ManyToMany
    private Set<Participant> newFor = new LinkedHashSet<>();
    @NotNull
    private String text;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public Participant getSender() {
        return sender;
    }

    public void setSender(Participant sender) {
        this.sender = sender;
    }

    public Set<Participant> getNewFor() {
        return newFor;
    }

    public void setNewFor(Set<Participant> newFor) {
        this.newFor = newFor;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Message message = (Message) o;
        return Objects.equals(id, message.id) &&
                Objects.equals(conversation, message.conversation) &&
                Objects.equals(sender, message.sender) &&
                Objects.equals(text, message.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, conversation, sender, text);
    }

    @Override
    public String toString() {
        return text;
    }
}
