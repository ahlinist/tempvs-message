package club.tempvs.message.dao;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Participant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("SELECT c from Conversation c JOIN c.participants p WHERE :participant IN p")
    List<Conversation> findByParticipantsIn(Participant participant, Pageable pageable);
    @Query("SELECT c, count(n) from Conversation c " +
            "JOIN c.messages m " +
            "JOIN m.newFor n " +
            "WHERE :participant IN n and c IN :conversations " +
            "GROUP BY c")
    List<Object[]> countUnreadMessages(List<Conversation> conversations, Participant participant);
    Conversation findOneByTypeAndParticipantsContainsAndParticipantsContains(
            Conversation.Type type, Set<Participant> authorSet, Set<Participant> receiverSet);
    @Query("select count(distinct m.conversation) from Message m join m.newFor n where n = :participant")
    long countByNewMessagesPerParticipant(@Param("participant") Participant participant);
}
