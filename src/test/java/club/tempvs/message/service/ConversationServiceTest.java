package club.tempvs.message.service;

import club.tempvs.message.dao.ConversationRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.service.impl.ConversationServiceImpl;
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
    private Participant participant;
    @Mock
    private Participant participantToAdd;
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
        Set<Participant> receivers = new LinkedHashSet<>();
        receivers.add(receiver);

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

    @Test
    public void testGetConversation() {
        long conversationId = 1L;
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        Conversation result = conversationService.getConversation(conversationId);

        verify(conversationRepository).findById(conversationId);
        verifyNoMoreInteractions(conversationRepository);

        assertEquals("A conversation with given id is retrieved", result, conversation);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetConversationNotFound() {
        long conversationId = 1L;

        Conversation result = conversationService.getConversation(conversationId);

        verify(conversationRepository).findById(conversationId);
        verifyNoMoreInteractions(conversationRepository);

        assertEquals("A conversation with given id is retrieved", result, null);
    }

    @Test
    public void testAddParticipants() {
        Set<Participant> participants = new LinkedHashSet<>();
        participants.add(participant);
        Set<Participant> participantsToAdd = new LinkedHashSet<>();
        participantsToAdd.add(participantToAdd);
        int participantsInitSize = participants.size();
        when(conversation.getParticipants()).thenReturn(participants);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.addParticipants(conversation, participantsToAdd);

        verify(conversation).getParticipants();
        verify(participantToAdd).addConversation(conversation);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(conversation);
        verifyNoMoreInteractions(participantToAdd);
        verifyNoMoreInteractions(conversationRepository);

        assertTrue("Participants collection size increased by 1", participants.size() == (participantsInitSize + 1));
        assertEquals("Updated conversation is returned as a successful result", result, conversation);
    }

    @Test
    public void testAddMessage() {
        String text = "new message text";
        Set<Participant> receivers = new LinkedHashSet<>();
        receivers.add(receiver);
        when(messageService.createMessage(conversation, sender, receivers, text)).thenReturn(message);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.addMessage(conversation, sender, receivers, text);

        verify(messageService).createMessage(conversation, sender, receivers, text);
        verify(conversation).addMessage(message);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(conversation);
        verifyNoMoreInteractions(messageService);
        verifyNoMoreInteractions(conversationRepository);
        verifyNoMoreInteractions(sender);
        verifyNoMoreInteractions(receiver);

        assertEquals("Updated conversation is returned as a successful result", result, conversation);
    }

    @Test
    public void testLeaveConversation() {
        conversation = new Conversation();
        conversation.addParticipant(sender);
        conversation.addParticipant(receiver);
        conversation.addParticipant(participant);

        int initialPartisipantsAmount = conversation.getParticipants().size();

        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.removeParticipant(conversation, participant);

        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(conversationRepository);

        assertEquals("A conversation object is returned", result, conversation);
        assertEquals("Participants size decrease by 1", initialPartisipantsAmount - 1, conversation.getParticipants().size());
    }

    @Test
    public void testLeaveConversationOfTwoParticipants() {
        conversation = new Conversation();
        conversation.addParticipant(sender);
        conversation.addParticipant(participant);

        int initialPartisipantsAmount = conversation.getParticipants().size();

        Conversation result = conversationService.removeParticipant(conversation, participant);

        assertEquals("A conversation object is returned", result, conversation);
        assertEquals("Participants size remains the same",
                initialPartisipantsAmount, conversation.getParticipants().size());
    }
}
