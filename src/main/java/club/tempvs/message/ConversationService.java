package club.tempvs.message;

import java.util.Set;

public interface ConversationService {
    Conversation createConversation(Participant sender, Set<Participant> receivers, String text, String name);
    Conversation getConversation(Long id);
}
