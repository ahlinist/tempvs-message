package club.tempvs.message.util;

import club.tempvs.message.domain.Message;

import java.util.Locale;

public interface LocaleHelper {

    Locale getLocale(String lang);

    Message translateMessageIfSystem(Message message);
}
