package club.tempvs.message.service;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;

import java.util.List;
import java.util.Set;

public interface ConversationService {

    Conversation createConversation(Participant author, Set<Participant> receivers, String name, Message message);

    Conversation getConversation(Long id);

    List<Conversation> getConversationsByParticipant(Participant participant, int page, int size);

    Conversation addMessage(Conversation conversation, Message message);

    Conversation removeParticipant(Conversation conversation, Participant remover, Participant removed);

    Conversation addParticipants(Conversation conversation, Participant adder, Set<Participant> added);

    Conversation findDialogue(Participant author, Participant receiver);

    long countUpdatedConversationsPerParticipant(Participant participant);

    Conversation rename(Conversation conversation, Participant initiator, String name);
}
