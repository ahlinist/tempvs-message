package club.tempvs.message;

import java.util.List;

public interface MessageService {
    Message createMessage(Conversation conversation, Participant sender, List<Participant> receivers, String text);
    Message persistMessage(Conversation conversation, Participant sender, List<Participant> receivers, String text);
}
