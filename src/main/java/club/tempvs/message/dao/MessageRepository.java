package club.tempvs.message.dao;

import club.tempvs.message.Message;
import org.springframework.data.repository.CrudRepository;

public interface MessageRepository extends CrudRepository<Message, Long> {
}
