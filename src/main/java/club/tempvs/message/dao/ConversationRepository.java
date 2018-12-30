package club.tempvs.message.dao;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Participant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("SELECT c FROM Conversation c WHERE :participant MEMBER OF c.participants")
    List<Conversation> findByParticipantsIn(Participant participant, Pageable pageable);

    @Query("SELECT c, count(n) FROM Conversation c " +
            "JOIN c.messages m " +
            "JOIN m.newFor n " +
            "WHERE :participant IN n and c IN :conversations " +
            "GROUP BY c")
    //TODO: simplify the query. Consider 'member of' clause.
    List<Object[]> countUnreadMessages(List<Conversation> conversations, Participant participant);

    @Query("SELECT c FROM Conversation c " +
            "WHERE :author MEMBER OF c.participants AND :receiver MEMBER OF c.participants AND c.type = :type")
    Conversation findDialogue(Conversation.Type type, Participant author, Participant receiver);

    @Query("SELECT COUNT(distinct m.conversation) FROM Message m " +
            "WHERE :participant MEMBER OF m.newFor AND :participant MEMBER OF m.conversation.participants")
    long countByNewMessagesPerParticipant(@Param("participant") Participant participant);
}
