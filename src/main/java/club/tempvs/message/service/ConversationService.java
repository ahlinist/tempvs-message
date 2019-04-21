package club.tempvs.message.service;

import club.tempvs.message.dto.GetConversationDto;
import club.tempvs.message.dto.GetConversationsDto;

import java.util.List;
import java.util.Set;

public interface ConversationService {

    GetConversationDto createConversation(Set<Long> receiverIds, String name, String text);

    GetConversationDto getConversation(Long id, int page, int size);

    GetConversationsDto getConversationsAttended(int page, int size);

    GetConversationDto addMessage(Long conversationId, String text);

    GetConversationDto removeParticipant(Long conversationId, Long removedId);

    GetConversationDto addParticipants(Long conversationId, Set<Long> subjectIds);

    long countUpdatedConversationsPerParticipant();

    GetConversationDto rename(Long conversationId, String name);

    void markMessagesAsRead(Long conversationId, List<Long> messageIds);
}
