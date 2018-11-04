package club.tempvs.message.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.*;

@Entity
@EntityListeners(AuditingEntityListener.class)
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

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Participant> newFor = new HashSet<>();

    @NotBlank
    private String text;

    @CreatedDate
    private Instant createdDate;

    public Message() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getSystem() {
        return isSystem;
    }

    public void setSystem(Boolean system) {
        isSystem = system;
    }

    public String getSystemArgs() {
        return systemArgs;
    }

    public void setSystemArgs(String systemArgs) {
        this.systemArgs = systemArgs;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public Participant getAuthor() {
        return author;
    }

    public void setAuthor(Participant author) {
        this.author = author;
    }

    public Participant getSubject() {
        return subject;
    }

    public void setSubject(Participant subject) {
        this.subject = subject;
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

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
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
                Objects.equals(conversation, message.conversation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, conversation);
    }

    @Override
    public String toString() {
        return text;
    }
}
