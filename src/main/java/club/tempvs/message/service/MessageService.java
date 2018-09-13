package club.tempvs.message.service;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;

import java.util.Set;

public interface MessageService {
    Message createMessage(Conversation conversation, Participant sender, Set<Participant> receivers, String text);
}