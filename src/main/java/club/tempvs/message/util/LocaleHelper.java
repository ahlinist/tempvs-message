package club.tempvs.message.util;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;

public interface LocaleHelper {

    Message translateMessageIfSystem(Message message);

    String translateMessageIfSystem(Conversation conversation);
}
