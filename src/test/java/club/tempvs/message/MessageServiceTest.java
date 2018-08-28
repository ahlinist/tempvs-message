package club.tempvs.message;

import club.tempvs.message.service.MessageServiceImpl;
import club.tempvs.message.util.ObjectFactory;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

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

    @Before
    public void setup() {
        this.messageService = new MessageServiceImpl(objectFactory);
    }

    @Test
    public void testCreateMessage() {
        String text = "text";
        Set<Participant> receivers = new LinkedHashSet<>();
        receivers.add(receiver1);
        receivers.add(receiver2);

        when(objectFactory.getInstance(Message.class)).thenReturn(message);

        Message result = messageService.createMessage(conversation, sender, receivers, text);

        verify(objectFactory).getInstance(Message.class);
        verify(message).setConversation(conversation);
        verify(message).setSender(sender);
        verify(message).setNewFor(receivers);
        verify(message).setText(text);
        verifyNoMoreInteractions(objectFactory);
        verifyNoMoreInteractions(message);

        assertEquals("An instance of Message is returned as a result", result, message);
    }
}
