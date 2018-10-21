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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ObjectFactory objectFactory;
    private final MessageRepository messageRepository;

    @Autowired
    public MessageServiceImpl(ObjectFactory objectFactory, MessageRepository messageRepository) {
        this.objectFactory = objectFactory;
        this.messageRepository = messageRepository;
    }

    public Message createMessage(Participant author,
                                 Set<Participant> receivers, String text, Boolean isSystem, Participant subject) {
        Message message = objectFactory.getInstance(Message.class);
        message.setAuthor(author);
        message.setNewFor(receivers);
        message.setText(text);
        message.setSystem(isSystem);
        message.setSubject(subject);
        return message;
    }

    public Message createMessage(Participant author, Set<Participant> receivers, String text) {
        return createMessage(author, receivers, text, Boolean.FALSE, null);
    }

    public Message createMessage(Participant author, Set<Participant> receivers, String text, Boolean isSystem) {
        return createMessage(author, receivers, text, isSystem, null);
    }

    public List<Message> getMessagesFromConversation(Conversation conversation, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdDate");
        return messageRepository.findByConversation(conversation, pageable);
    }

    public List<Message> getMessagesFromConversation(Conversation conversation) {
        return getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
    }
}
