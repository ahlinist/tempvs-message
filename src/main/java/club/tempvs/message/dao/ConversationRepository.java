package club.tempvs.message.dao;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Participant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    //TODO: replace :participant with :participantId
    @Query("SELECT c, (SELECT COUNT(m) FROM Message m WHERE (m.conversation = c) AND (c.lastReadOn[:participant] < m.createdDate)) " +
            "FROM Conversation c " +
            "WHERE :participant MEMBER OF c.participants " +
            "GROUP BY c, c.lastMessageCreatedDate " +
            "ORDER BY c.lastMessageCreatedDate DESC")
    List<Object[]> findConversationsPerParticipant(@Param("participant") Participant participant, Pageable pageable);

    @Query("SELECT c FROM Conversation c " +
            "WHERE :author MEMBER OF c.participants AND :receiver MEMBER OF c.participants AND c.type = :type")
    Conversation findDialogue(@Param("type") Conversation.Type type,
                              @Param("author") Participant author,
                              @Param("receiver") Participant receiver);

    @Query("SELECT COUNT(distinct m.conversation) FROM Message m " +
            "WHERE (m.conversation.lastReadOn[:participant] < m.createdDate) " +
            "AND (:participant MEMBER OF m.conversation.participants)")
    long countByNewMessagesPerParticipant(@Param("participant") Participant participant);
}
