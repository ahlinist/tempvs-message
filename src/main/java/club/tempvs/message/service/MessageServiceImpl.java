package club.tempvs.message.service;

import club.tempvs.message.*;
import club.tempvs.message.dao.MessageRepository;
import club.tempvs.message.util.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Message createMessage(Conversation conversation, Participant sender, Set<Participant> receivers, String text) {
        Message message = objectFactory.getInstance(Message.class);
        message.setConversation(conversation);
        message.setSender(sender);
        message.setNewFor(receivers);
        message.setText(text);

        return message;
    }

    public Message persistMessage(Conversation conversation, Participant sender, Set<Participant> receivers, String text) {
        Message message = createMessage(conversation, sender, receivers, text);
        return messageRepository.save(message);
    }
}
