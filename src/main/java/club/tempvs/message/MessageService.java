package club.tempvs.message;

import java.util.Set;

public interface MessageService {
    Message createMessage(Conversation conversation, Participant sender, Set<Participant> receivers, String text);
    Message persistMessage(Conversation conversation, Participant sender, Set<Participant> receivers, String text);
}
