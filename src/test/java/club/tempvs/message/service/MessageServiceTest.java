package club.tempvs.message.service;

import club.tempvs.message.dao.MessageRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.service.impl.MessageServiceImpl;
import club.tempvs.message.util.ObjectFactory;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTest {

    private MessageService messageService;

    @Mock
    private Message message;
    @Mock
    private Participant author;
    @Mock
    private Participant receiver1;
    @Mock
    private Participant receiver2;
    @Mock
    private Participant subject;
    @Mock
    private Conversation conversation;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private MessageSource messageSource;

    @Before
    public void setup() {
        this.messageService = new MessageServiceImpl(objectFactory, messageRepository, messageSource);
    }

    @Test
    public void testCreateMessage() {
        String text = "text";
        Set<Participant> receivers = new HashSet<>();
        receivers.add(receiver1);
        receivers.add(receiver2);

        when(objectFactory.getInstance(Message.class)).thenReturn(message);

        Message result = messageService.createMessage(author, receivers, text, false, subject);

        verify(objectFactory).getInstance(Message.class);
        verify(message).setAuthor(author);
        verify(message).setNewFor(receivers);
        verify(message).setText(text);
        verify(message).setSystem(false);
        verify(message).setSubject(subject);
        verifyNoMoreInteractions(objectFactory);
        verifyNoMoreInteractions(message);

        assertEquals("An instance of Message is returned as a result", result, message);
    }

    @Test
    public void testGetMessagesFromConversation() {
        int page = 0;
        int size = 40;
        String text = "text";
        String translatedText = "translated text";
        Locale locale = Locale.ENGLISH;
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdDate");
        List<Message> messages = Arrays.asList(message, message, message);

        when(messageRepository.findByConversation(conversation, pageable)).thenReturn(messages);
        when(message.getText()).thenReturn(text);
        when(message.getSystem()).thenReturn(true);
        when(messageSource.getMessage(text, null, locale)).thenReturn(translatedText);

        List<Message> result = messageService.getMessagesFromConversation(conversation, locale, page, size);

        verify(messageRepository).findByConversation(conversation, pageable);
        verify(message, times(3)).getText();
        verify(message, times(3)).getSystem();
        verify(messageSource, times(3)).getMessage(text, null, locale);
        verify(message, times(3)).setText(translatedText);
        verifyNoMoreInteractions(message, messageSource, messageRepository);

        assertEquals("A list of messages is returned", messages, result);
    }

    @Test
    public void testGetMessagesFromConversationWithDefaultParams() {
        int page = 0;
        int size = 40;
        String text = "text";
        String translatedText = "translated text";
        Locale locale = Locale.ENGLISH;
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdDate");
        List<Message> messages = Arrays.asList(message, message, message);

        when(messageRepository.findByConversation(conversation, pageable)).thenReturn(messages);
        when(message.getText()).thenReturn(text);
        when(message.getSystem()).thenReturn(true);
        when(messageSource.getMessage(text, null, locale)).thenReturn(translatedText);

        List<Message> result = messageService.getMessagesFromConversation(conversation, locale);

        verify(messageRepository).findByConversation(conversation, pageable);
        verify(message, times(3)).getText();
        verify(message, times(3)).getSystem();
        verify(messageSource, times(3)).getMessage(text, null, locale);
        verify(message, times(3)).setText(translatedText);
        verifyNoMoreInteractions(message, messageSource, messageRepository);

        assertEquals("A list of messages is returned", messages, result);
    }
}
