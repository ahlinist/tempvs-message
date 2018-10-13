package club.tempvs.message.dao;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Participant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByParticipantsIn(Set<Participant> participants, Pageable pageable);
    Conversation findOneByTypeAndParticipantsContainsAndParticipantsContains(
            Conversation.Type type, Set<Participant> authorSet, Set<Participant> receiverSet);
}
