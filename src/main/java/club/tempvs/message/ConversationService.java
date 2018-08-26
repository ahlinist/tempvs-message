package club.tempvs.message;

import java.util.List;

public interface ConversationService {
    Conversation createConversation(
            Participant sender, List<Participant> receivers, String messageText, String conversationName);
    Conversation getConversation(Long id);
}
