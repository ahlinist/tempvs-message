package club.tempvs.message.service;

import club.tempvs.message.api.ForbiddenException;
import club.tempvs.message.dao.MessageRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.service.impl.MessageServiceImpl;
import club.tempvs.message.util.LocaleHelper;
import club.tempvs.message.util.ObjectFactory;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTest {

    private MessageService messageService;

    @Mock
    private Message message, message1, message2;
    @Mock
    private Participant participant, author, receiver1, receiver2, subject;
    @Mock
    private Conversation conversation, conversation1, conversation2;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private LocaleHelper localeHelper;

    @Before
    public void setup() {
        this.messageService = new MessageServiceImpl(objectFactory, messageRepository, localeHelper);
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
        verify(message1).setIsSystem(false);
        verify(message1).setSystemArgs(null);
        verify(message1).setSubject(subject);
        verifyNoMoreInteractions(message1, receiver1, receiver2, objectFactory);

        assertEquals("An instance of Message is returned as a result", result, message1);
    }

    @Test
    public void testAddMessage() {
        Conversation result = messageService.addMessage(conversation, message);

        verify(conversation).setLastMessage(message);
        verify(conversation).addMessage(message);
        verify(message).setConversation(conversation);
        verifyNoMoreInteractions(message, conversation);

        assertEquals("Conversation object is returned", conversation, result);
    }

    @Test
    public void testGetMessagesFromConversation() {
        int page = 0;
        int size = 40;
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdDate");
        List<Message> originalMessages = Arrays.asList(message1, message1, message1);
        List<Message> translatedMessages = Arrays.asList(message2, message2, message2);

        when(messageRepository.findByConversation(conversation1, pageable)).thenReturn(originalMessages);
        when(localeHelper.translateMessageIfSystem(message1)).thenReturn(message2);

        List<Message> result = messageService.getMessagesFromConversation(conversation1, page, size);

        verify(messageRepository).findByConversation(conversation1, pageable);
        verify(localeHelper, times(3)).translateMessageIfSystem(message1);
        verifyNoMoreInteractions(message1, localeHelper, messageRepository);

        assertEquals("A list of messages is returned", translatedMessages, result);
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

    @Test(expected = ForbiddenException.class)
    public void testMarkAsReadForInvalidConversations() {
        List<Message> messages = Arrays.asList(message1, message2);

        when(message1.getConversation()).thenReturn(conversation1);
        when(message2.getConversation()).thenReturn(conversation2);

        messageService.markAsRead(conversation1, participant, messages);

        verify(message1).getConversation();
        verify(message2).getConversation();
        verifyNoMoreInteractions(message1, messageRepository, participant, author, receiver1, conversation1, conversation2);
    }

    @Test(expected = ForbiddenException.class)
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

    @Test(expected = IllegalStateException.class)
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
