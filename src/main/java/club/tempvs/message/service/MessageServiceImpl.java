package club.tempvs.message.service;

import club.tempvs.message.*;
import club.tempvs.message.util.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class MessageServiceImpl implements MessageService {

    private final ObjectFactory objectFactory;

    @Autowired
    public MessageServiceImpl(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public Message createMessage(Conversation conversation, Participant sender, Set<Participant> receivers, String text) {
        Message message = objectFactory.getInstance(Message.class);
        message.setConversation(conversation);
        message.setSender(sender);
        message.setNewFor(receivers);
        message.setText(text);

        return message;
    }
}
