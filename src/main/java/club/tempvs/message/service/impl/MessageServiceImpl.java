package club.tempvs.message.service.impl;

import club.tempvs.message.dao.MessageRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.service.MessageService;
import club.tempvs.message.util.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import static java.util.stream.Collectors.*;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = 40;

    private final ObjectFactory objectFactory;
    private final MessageRepository messageRepository;
    private final MessageSource messageSource;

    @Autowired
    public MessageServiceImpl(ObjectFactory objectFactory, MessageRepository messageRepository, MessageSource messageSource) {
        this.objectFactory = objectFactory;
        this.messageRepository = messageRepository;
        this.messageSource = messageSource;
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

    public List<Message> getMessagesFromConversation(Conversation conversation, Locale locale, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdDate");
        List<Message> messages = messageRepository.findByConversation(conversation, pageable);
        return messages.stream().map(message -> {
            if (message.getSystem()) {
                message.setText(messageSource.getMessage(message.getText(), null, locale));
            }

            return message;
        }).collect(toList());
    }
}
