package club.tempvs.message.service;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;

import java.util.Set;

public interface ConversationService {
    Conversation createConversation(Participant sender, Set<Participant> receivers, String name, Message message);
    Conversation getConversation(Long id);
    Conversation addMessage(Conversation conversation, Message message);
}
