package club.tempvs.message.service.impl;

import club.tempvs.message.dao.MessageRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.service.MessageService;
import club.tempvs.message.util.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class MessageServiceImpl implements MessageService {

    private final ObjectFactory objectFactory;
    private final MessageRepository messageRepository;

    @Autowired
    public MessageServiceImpl(ObjectFactory objectFactory, MessageRepository messageRepository) {
        this.objectFactory = objectFactory;
        this.messageRepository = messageRepository;
    }

    public Message createMessage(
            Conversation conversation, Participant author, Set<Participant> receivers, String text, Boolean isSystem) {
        Message message = objectFactory.getInstance(Message.class);
        message.setConversation(conversation);
        message.setAuthor(author);
        message.setNewFor(receivers);
        message.setText(text);
        message.setSystem(isSystem);
        return message;
    }

    public Message createMessage(Participant author, Set<Participant> receivers, String text, Boolean isSystem) {
        return createMessage(null, author, receivers, text, isSystem);
    }

    public List<Message> getMessagesFromConversation(Conversation conversation, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdDate");
        return messageRepository.findByConversation(conversation, pageable);
    }
}
