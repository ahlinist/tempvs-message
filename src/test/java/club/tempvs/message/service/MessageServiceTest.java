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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTest {

    private MessageService messageService;

    @Mock
    private Message message1;
    @Mock
    private Message message2;
    @Mock
    private Participant participant;
    @Mock
    private Participant author;
    @Mock
    private Participant receiver1;
    @Mock
    private Participant receiver2;
    @Mock
    private Participant subject;
    @Mock
    private Conversation conversation1;
    @Mock
    private Conversation conversation2;
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

        when(objectFactory.getInstance(Message.class)).thenReturn(message1);

        Message result = messageService.createMessage(author, receivers, text, false, null, subject);

        verify(objectFactory).getInstance(Message.class);
        verify(message1).setAuthor(author);
        verify(message1).setNewFor(receivers);
        verify(message1).setText(text);
        verify(message1).setSystem(false);
        verify(message1).setSystemArgs(null);
        verify(message1).setSubject(subject);
        verifyNoMoreInteractions(message1, receiver1, receiver2, objectFactory);

        assertEquals("An instance of Message is returned as a result", result, message1);
    }

    @Test
    public void testGetMessagesFromConversation() {
        int page = 0;
        int size = 40;
        String text = "text";
        String translatedText = "translated text";
        Locale locale = LocaleContextHolder.getLocale();
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdDate");
        List<Message> messages = Arrays.asList(message1, message1, message1);
        String[] args = new String[0];

        when(messageRepository.findByConversation(conversation1, pageable)).thenReturn(messages);
        when(message1.getText()).thenReturn(text);
        when(message1.getSystem()).thenReturn(true);
        when(messageSource.getMessage(text, args, text, locale)).thenReturn(translatedText);

        List<Message> result = messageService.getMessagesFromConversation(conversation1, page, size);

        verify(messageRepository).findByConversation(conversation1, pageable);
        verify(message1, times(3)).getText();
        verify(message1, times(3)).getSystem();
        verify(message1, times(3)).getSystemArgs();
        verify(messageSource, times(3)).getMessage(text, args, text, locale);
        verify(message1, times(3)).setText(translatedText);
        verifyNoMoreInteractions(message1, messageSource, messageRepository);

        assertEquals("A list of messages is returned", messages, result);
    }

    @Test
    public void testMarkAsRead() {
        List<Message> messages = Arrays.asList(message1, message1);
        Set<Participant> newFor = new HashSet<>(Arrays.asList(author, receiver1, participant));
        Set<Participant> participants = new LinkedHashSet<>(Arrays.asList(receiver1, participant));

        when(message1.getConversation()).thenReturn(conversation1);
        when(conversation1.getParticipants()).thenReturn(participants);
        when(message1.getNewFor()).thenReturn(newFor);
        when(messageRepository.saveAll(messages)).thenReturn(messages);

        List<Message> result = messageService.markAsRead(conversation1, participant, messages);

        verify(message1, times(2)).getConversation();
        verify(message1, times(2)).getNewFor();
        verify(conversation1).getParticipants();
        verify(messageRepository).saveAll(messages);
        verifyNoMoreInteractions(message1, messageRepository, participant, author, receiver1, conversation1, conversation2);

        assertEquals("2 messages returned", 2, result.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMarkAsReadForInvalidConversations() {
        List<Message> messages = Arrays.asList(message1, message2);

        when(message1.getConversation()).thenReturn(conversation1);
        when(message2.getConversation()).thenReturn(conversation2);

        messageService.markAsRead(conversation1, participant, messages);

        verify(message1).getConversation();
        verify(message2).getConversation();
        verifyNoMoreInteractions(message1, messageRepository, participant, author, receiver1, conversation1, conversation2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMarkAsReadForInvalidParticipant() {
        List<Message> messages = Arrays.asList(message1, message1);
        Set<Participant> participants = new LinkedHashSet<>(Arrays.asList(author, receiver1));

        when(message1.getConversation()).thenReturn(conversation1);
        when(conversation1.getParticipants()).thenReturn(participants);

        messageService.markAsRead(conversation1, participant, messages);

        verify(message1).getConversation();
        verify(conversation1).getParticipants();
        verifyNoMoreInteractions(message1, messageRepository, participant, author, receiver1, conversation1, conversation2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMarkAsReadForEmptyMessagesList() {
        List<Message> messages = new ArrayList<>();

        messageService.markAsRead(conversation1, participant, messages);

        verifyNoMoreInteractions(message1, messageRepository, participant, author, receiver1, conversation1, conversation2);
    }

    @Test
    public void testFindMessagesByIds() {
        List<Long> ids = Arrays.asList(1L, 2L);
        List<Message> messages = Arrays.asList(message1, message2);

        when(messageRepository.findAllById(ids)).thenReturn(messages);

        List<Message> result = messageService.findMessagesByIds(ids);

        verify(messageRepository).findAllById(ids);
        verifyNoMoreInteractions(messageRepository, message1, message2);

        assertEquals("A list of messages is returned.", result, messages);
    }
}
