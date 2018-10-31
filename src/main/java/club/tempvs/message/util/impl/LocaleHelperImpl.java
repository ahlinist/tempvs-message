package club.tempvs.message.util.impl;

import club.tempvs.message.util.LocaleHelper;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class LocaleHelperImpl implements LocaleHelper {

    public Locale getLocale(String lang) {
        Locale locale;

        try {
            locale = new Locale(lang);
        } catch (Exception e) {
            locale = Locale.ENGLISH;
        }

        return locale;
    }
}
