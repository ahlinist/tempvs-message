package club.tempvs.message.service.impl;

import static java.util.Objects.nonNull;

import club.tempvs.message.dao.MessageRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.service.MessageService;
import club.tempvs.message.util.LocaleHelper;
import club.tempvs.message.util.ObjectFactory;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final ObjectFactory objectFactory;
    private final MessageRepository messageRepository;
    private final LocaleHelper localeHelper;

    public Message createMessage(Participant author,
                                 Set<Participant> receivers, String text, Boolean isSystem, String systemArgs, Participant subject) {
        Message message = objectFactory.getInstance(Message.class);
        message.setAuthor(author);
        message.setText(text);
        message.setIsSystem(isSystem);
        message.setSystemArgs(systemArgs);
        message.setSubject(subject);
        return message;
    }

    public Conversation addMessage(Conversation conversation, Message message, Participant author) {
        Instant createdDate = message.getCreatedDate();
        Participant subject = message.getSubject();

        if (nonNull(subject)) {
            conversation.setLastMessageSubjectName(subject.getName());
        }

        conversation.addMessage(message);
        conversation.setLastMessageText(message.getText());
        conversation.setLastMessageAuthorName(message.getAuthor().getName());
        conversation.setLastMessageCreatedDate(createdDate);
        conversation.setLastMessageSystem(message.getIsSystem());
        conversation.setLastMessageSystemArgs(message.getSystemArgs());
        message.setConversation(conversation);
        conversation.getLastReadOn()
                .put(author, createdDate);
        return conversation;
    }

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    public List<Message> getMessagesFromConversation(Conversation conversation, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdDate");
        return messageRepository.findByConversation(conversation, pageable).stream()
                .map(localeHelper::translateMessageIfSystem)
                .collect(toList());
    }

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    public List<Message> findMessagesByIds(List<Long> ids) {
        return messageRepository.findAllById(ids);
    }
}
