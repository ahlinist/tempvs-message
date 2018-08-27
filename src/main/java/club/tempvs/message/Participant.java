package club.tempvs.message;

import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Participant {

    @Id
    private Long id;
    @ManyToMany(mappedBy = "participants")
    @BatchSize(size = 20)
    private List<Conversation> conversations = new ArrayList<>();

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

    public void addConversation(Conversation conversation) {
        this.conversations.add(conversation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(conversations, that.conversations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, conversations);
    }
}
