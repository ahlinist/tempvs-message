package club.tempvs.message.service.impl;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.service.MessageService;
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

    public Message createMessage(
            Conversation conversation, Participant sender, Set<Participant> receivers, String text, Boolean isSystem) {
        Message message = objectFactory.getInstance(Message.class);
        message.setConversation(conversation);
        message.setSender(sender);
        message.setNewFor(receivers);
        message.setText(text);
        message.setSystem(isSystem);
        return message;
    }

    public Message createMessage(Participant sender, Set<Participant> receivers, String text, Boolean isSystem) {
        return createMessage(null, sender, receivers, text, isSystem);
    }
}
