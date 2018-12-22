package club.tempvs.message.service;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;

import java.util.List;
import java.util.Set;

public interface MessageService {
    Message createMessage(Participant author, Set<Participant> receivers, String text);
    Message createMessage(Participant author, Set<Participant> receivers, String text, Boolean isSystem, String systemArgs);
    Message createMessage(Participant author, Set<Participant> receivers, String text, Boolean isSystem, String systemArgs, Participant subject);
    List<Message> getMessagesFromConversation(Conversation conversation, int page, int size);
    List<Message> markAsRead(Conversation conversation, Participant participant, List<Message> messages);
    List<Message> findMessagesByIds(List<Long> ids);
}
