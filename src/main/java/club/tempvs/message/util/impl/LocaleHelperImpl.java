package club.tempvs.message.util.impl;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.util.LocaleHelper;
import club.tempvs.message.util.ObjectFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocaleHelperImpl implements LocaleHelper {

    private final MessageSource messageSource;
    private final ObjectFactory objectFactory;

    public Message translateMessageIfSystem(Message originalMessage) {
        if (originalMessage.getIsSystem()) {
            String code = originalMessage.getText();
            String[] args = new String[0];
            String argsString = originalMessage.getSystemArgs();

            if (argsString != null) {
                args = argsString.split(",");
            }

            Message translatedMessage = objectFactory.getInstance(Message.class, originalMessage);
            String translatedMessageString = messageSource.getMessage(code, args, code, LocaleContextHolder.getLocale());
            translatedMessage.setText(translatedMessageString);
            return translatedMessage;
        } else {
            return originalMessage;
        }
    }

    public String translateMessageIfSystem(Conversation conversation) {
        String originalMessage = conversation.getLastMessageText();

        if (conversation.getLastMessageSystem()) {
            String code = originalMessage;
            String[] args = new String[0];
            String argsString = conversation.getLastMessageSystemArgs();

            if (argsString != null) {
                args = argsString.split(",");
            }

            return messageSource.getMessage(code, args, code, LocaleContextHolder.getLocale());
        } else {
            return originalMessage;
        }
    }
}
