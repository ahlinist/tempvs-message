package club.tempvs.message;

import java.util.Set;

public interface ConversationService {
    Conversation createConversation(Participant sender, Set<Participant> receivers, String text, String name);
    Conversation getConversation(Long id);
    Conversation addParticipants(Conversation conversation, Set<Participant> participantsToAdd);
    Conversation addMessage(Conversation conversation, Participant sender, Set<Participant> receivers, String text);
    Conversation removeParticipant(Conversation conversation, Participant participant);
}
