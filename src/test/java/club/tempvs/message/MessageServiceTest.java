package club.tempvs.message;

import club.tempvs.message.dao.MessageRepository;
import club.tempvs.message.service.MessageServiceImpl;
import club.tempvs.message.util.ObjectFactory;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTest {

    private MessageService messageService;

    @Mock
    private Message message;
    @Mock
    private Participant sender;
    @Mock
    private Participant receiver1;
    @Mock
    private Participant receiver2;
    @Mock
    private Conversation conversation;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private MessageRepository messageRepository;

    @Before
    public void setup() {
        this.messageService = new MessageServiceImpl(objectFactory, messageRepository);
    }

    @Test
    public void testCreateMessage() {
        String text = "text";
        List<Participant> receivers = Arrays.asList(receiver1, receiver2);

        when(objectFactory.getInstance(Message.class)).thenReturn(message);

        Message result = messageService.createMessage(conversation, sender, receivers, text);

        verify(objectFactory).getInstance(Message.class);
        verify(message).setConversation(conversation);
        verify(message).setSender(sender);
        verify(message).setNewFor(receivers);
        verify(message).setText(text);
        verifyNoMoreInteractions(objectFactory);
        verifyNoMoreInteractions(message);
        verifyNoMoreInteractions(messageRepository);

        assertEquals("An instance of Message is returned as a result", result, message);
    }

    @Test
    public void testPersistMessage() {
        String text = "text";
        List<Participant> receivers = Arrays.asList(receiver1, receiver2);

        when(objectFactory.getInstance(Message.class)).thenReturn(message);
        when(messageRepository.save(message)).thenReturn(message);

        Message result = messageService.persistMessage(conversation, sender, receivers, text);

        verify(objectFactory).getInstance(Message.class);
        verify(message).setConversation(conversation);
        verify(message).setSender(sender);
        verify(message).setNewFor(receivers);
        verify(message).setText(text);
        verify(messageRepository).save(message);
        verifyNoMoreInteractions(objectFactory);
        verifyNoMoreInteractions(message);
        verifyNoMoreInteractions(messageRepository);

        assertEquals("An instance of Message is returned as a result", result, message);
    }
}
