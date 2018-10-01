package club.tempvs.message.dao;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MessageRepository extends PagingAndSortingRepository<Message, Long> {
    List<Message> findByConversation(Conversation conversation, Pageable pageable);
}
