package club.tempvs.message.service;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.GetConversationDto;
import club.tempvs.message.dto.GetConversationsDto;

import java.util.Set;

public interface ConversationService {

    GetConversationDto createConversation(Set<Long> receiverIds, String name, String text);

    Conversation buildConversation(Participant author, Set<Participant> receivers, String name, Message message);

    Conversation getConversation(Long id);

    GetConversationsDto getConversationsAttended(int page, int size);

    Conversation addMessage(Conversation conversation, Message message);

    Conversation removeParticipant(Conversation conversation, Participant remover, Participant removed);

    Conversation addParticipants(Conversation conversation, Participant adder, Set<Participant> added);

    Conversation findDialogue(Participant author, Participant receiver);

    long countUpdatedConversationsPerParticipant();

    GetConversationDto rename(Long conversationId, String name);
}
