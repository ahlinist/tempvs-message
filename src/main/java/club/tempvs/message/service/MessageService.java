package club.tempvs.message.service;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public interface MessageService {
    Message createMessage(Participant author, Set<Participant> receivers, String text);
    Message createMessage(Participant author, Set<Participant> receivers, String text, Boolean isSystem);
    Message createMessage(Participant author, Set<Participant> receivers, String text, Boolean isSystem, Participant subject);
    List<Message> getMessagesFromConversation(Conversation conversation, Locale locale, int page, int size);
    List<Message> getMessagesFromConversation(Conversation conversation, Locale locale);
}
