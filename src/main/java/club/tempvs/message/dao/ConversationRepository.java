package club.tempvs.message.dao;

import club.tempvs.message.domain.Conversation;
import org.springframework.data.repository.CrudRepository;

public interface ConversationRepository extends CrudRepository<Conversation, Long> {
}
