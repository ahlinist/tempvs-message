package club.tempvs.message.util;

import club.tempvs.message.domain.Message;
import club.tempvs.message.util.impl.LocaleHelperImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LocaleHelperTest {

    private LocaleHelper localeHelper;

    @Mock
    private Message originalMessage;

    @Mock
    private Message translatedMessage;

    @Mock
    private MessageSource messageSource;

    @Mock
    private ObjectFactory objectFactory;

    @Before
    public void setup() {
        localeHelper = new LocaleHelperImpl(messageSource, objectFactory);
    }

    @Test
    public void testTranslateSystemMessageForRegularMessage() {
        when(originalMessage.getSystem()).thenReturn(false);

        Message result = localeHelper.translateMessageIfSystem(originalMessage);

        verify(originalMessage).getSystem();
        verifyNoMoreInteractions(originalMessage, messageSource);

        Assert.assertEquals("The original message is returned", originalMessage, result);
    }


    @Test
    public void testTranslateSystemMessageForSystemMessage() {
        String code = "message.code";
        String systemArgsString = "arg1,arg2";
        String[] args = new String[]{"arg1", "arg2"};
        Locale locale = LocaleContextHolder.getLocale();
        String translatedMessageString = "translated message";

        when(originalMessage.getSystem()).thenReturn(true);
        when(originalMessage.getText()).thenReturn(code);
        when(originalMessage.getSystemArgs()).thenReturn(systemArgsString);
        when(objectFactory.getInstance(Message.class, originalMessage)).thenReturn(translatedMessage);
        when(messageSource.getMessage(code, args, code, locale)).thenReturn(translatedMessageString);

        Message result = localeHelper.translateMessageIfSystem(originalMessage);

        verify(originalMessage).getSystem();
        verify(originalMessage).getText();
        verify(originalMessage).getSystemArgs();
        verify(objectFactory).getInstance(Message.class, originalMessage);
        verify(messageSource).getMessage(code, args, code, locale);
        verify(translatedMessage).setText(translatedMessageString);
        verifyNoMoreInteractions(translatedMessage, messageSource);

        Assert.assertEquals("The translated message is returned", translatedMessage, result);
    }
}
