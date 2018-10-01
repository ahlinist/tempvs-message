package club.tempvs.message.service;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;

import java.util.List;
import java.util.Set;

public interface MessageService {
    Message createMessage(Participant sender, Set<Participant> receivers, String text, Boolean isSystem);
    Message createMessage(Conversation conversation, Participant sender, Set<Participant> receivers, String text, Boolean isSystem);
    List<Message> getMessagesFromConversation(Conversation conversation, int page, int size);
}
