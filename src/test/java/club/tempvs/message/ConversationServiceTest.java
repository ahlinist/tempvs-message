package club.tempvs.message;

import club.tempvs.message.dao.ConversationRepository;
import club.tempvs.message.service.ConversationServiceImpl;
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
public class ConversationServiceTest {

    private ConversationService conversationService;

    @Mock
    private Message message;
    @Mock
    private Conversation conversation;
    @Mock
    private Participant sender;
    @Mock
    private Participant receiver;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private MessageService messageService;

    @Before
    public void setup() {
        this.conversationService = new ConversationServiceImpl(objectFactory, conversationRepository, messageService);
    }

    @Test
    public void testCreateConversation() {
        String messageText = "text";
        String conversationName = "name";
        List<Participant> receivers = Arrays.asList(receiver);

        when(objectFactory.getInstance(Conversation.class)).thenReturn(conversation);
        when(messageService.createMessage(conversation, sender, receivers, messageText)).thenReturn(message);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.createConversation(sender, receivers, messageText, conversationName);

        verify(conversation).setParticipants(receivers);
        verify(conversation).addParticipant(sender);
        verify(conversation).setName(conversationName);
        verify(conversation).addMessage(message);
        verify(messageService).createMessage(conversation, sender, receivers, messageText);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(conversation);
        verifyNoMoreInteractions(messageService);
        verifyNoMoreInteractions(conversationRepository);
        verifyNoMoreInteractions(sender);
        verifyNoMoreInteractions(receiver);

        assertEquals("Service returns a conversation instance", result, conversation);
    }
}
