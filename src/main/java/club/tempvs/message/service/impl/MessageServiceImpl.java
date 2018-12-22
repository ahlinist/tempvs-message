package club.tempvs.message.service.impl;

import club.tempvs.message.dao.MessageRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.service.MessageService;
import club.tempvs.message.util.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.*;

@Service
public class MessageServiceImpl implements MessageService {

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
                                 Set<Participant> receivers, String text, Boolean isSystem, String systemArgs, Participant subject) {
        Message message = objectFactory.getInstance(Message.class);
        message.setAuthor(author);
        message.setNewFor(receivers);
        message.setText(text);
        message.setSystem(isSystem);
        message.setSystemArgs(systemArgs);
        message.setSubject(subject);
        return message;
    }

    public Message createMessage(Participant author, Set<Participant> receivers, String text) {
        return createMessage(author, receivers, text, Boolean.FALSE, null, null);
    }

    public Message createMessage(Participant author, Set<Participant> receivers, String text, Boolean isSystem, String systemArgs) {
        return createMessage(author, receivers, text, isSystem, systemArgs, null);
    }

    public List<Message> getMessagesFromConversation(Conversation conversation, int page, int size) {
        Locale locale = LocaleContextHolder.getLocale();
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdDate");
        List<Message> messages = messageRepository.findByConversation(conversation, pageable);

        return messages.stream().map(message -> {
            if (message.getSystem()) {
                String code = message.getText();
                String[] args = new String[0];
                String argsString = message.getSystemArgs();

                if (argsString != null) {
                    args = argsString.split(",");
                }

                message.setText(messageSource.getMessage(code, args, code, locale));
            }

            return message;
        }).collect(toList());
    }

    public List<Message> markAsRead(Conversation conversation, Participant participant, List<Message> messages) {
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("Empty messages list.");
        }

        if (!messages.stream().map(Message::getConversation).allMatch(conversation::equals)) {
            throw new IllegalArgumentException("Messages belong to different conversations.");
        }

        if (!conversation.getParticipants().contains(participant)) {
            throw new IllegalArgumentException("The conversation should contain the given participant.");
        }

        messages.stream().forEach(message -> message.getNewFor().remove(participant));
        return messageRepository.saveAll(messages);
    }

    public List<Message> findMessagesByIds(List<Long> ids) {
        return messageRepository.findAllById(ids);
    }
}
