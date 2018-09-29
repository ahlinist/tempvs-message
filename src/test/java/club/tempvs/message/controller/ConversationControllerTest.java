package club.tempvs.message.controller;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.*;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.service.MessageService;
import club.tempvs.message.service.ParticipantService;
import club.tempvs.message.util.ObjectFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class ConversationControllerTest {

    private ConversationController conversationController;

    @Mock
    private ConversationService conversationService;
    @Mock
    private ParticipantService participantService;
    @Mock
    private MessageService messageService;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private CreateConversationDto createConversationDto;
    @Mock
    private Message message;
    @Mock
    private Participant sender;
    @Mock
    private Participant receiver;
    @Mock
    private Participant participant;
    @Mock
    private Conversation conversation;
    @Mock
    private GetConversationDto getConversationDto;
    @Mock
    private GetConversationsDto getConversationsDto;

    @Before
    public void setup() {
        conversationController = new ConversationController(objectFactory, conversationService, participantService, messageService);
    }

    @Test
    public void testCreateConversation() {
        String text = "text";
        String name = "name";
        Set<Long> receiverIds = new HashSet<>();
        receiverIds.add(2L);
        Set<Participant> receivers = new HashSet<>();
        receivers.add(receiver);

        when(createConversationDto.getSender()).thenReturn(1L);
        when(participantService.getParticipant(1L)).thenReturn(sender);
        when(participantService.getParticipant(2L)).thenReturn(receiver);
        when(createConversationDto.getReceivers()).thenReturn(receiverIds);
        when(createConversationDto.getText()).thenReturn(text);
        when(createConversationDto.getName()).thenReturn(name);
        when(messageService.createMessage(sender, receivers, text,false)).thenReturn(message);
        when(conversationService.createConversation(sender, receivers, name, message)).thenReturn(conversation);
        when(objectFactory.getInstance(GetConversationDto.class, conversation)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.createConversation(createConversationDto);

        verify(createConversationDto).getSender();
        verify(participantService).getParticipant(1L);
        verify(participantService).getParticipant(2L);
        verify(createConversationDto).getReceivers();
        verify(createConversationDto).getText();
        verify(createConversationDto).getName();
        verify(messageService).createMessage(sender, receivers, text, false);
        verify(conversationService).createConversation(sender, receivers, name, message);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation);
        verifyNoMoreInteractions(createConversationDto, participantService, messageService, conversationService, objectFactory);

        assertEquals("Result is a conversation", result, getConversationDto);
    }

    @Test
    public void testGetConversation() {
        long id = 1L;

        when(conversationService.getConversation(id)).thenReturn(conversation);
        when(objectFactory.getInstance(GetConversationDto.class, conversation)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.getConversation(id);

        verify(conversationService).getConversation(id);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation);
        verifyNoMoreInteractions(conversationService, objectFactory);

        assertEquals("Result is a conversation", result, getConversationDto);
    }

    @Test
    public void testGetConversationsByParticipant() {
        Long participantId = 1L;
        int page = 0;
        int size = 20;
        List<Conversation> conversations = new ArrayList<>();
        conversations.add(conversation);

        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(conversationService.getConversationsByParticipant(participant, page, size)).thenReturn(conversations);
        when(objectFactory.getInstance(GetConversationsDto.class, conversations)).thenReturn(getConversationsDto);

        GetConversationsDto result = conversationController.getConversationsByParticipant(participantId, page, size);

        verify(participantService).getParticipant(participantId);
        verify(conversationService).getConversationsByParticipant(participant, page, size);
        verify(objectFactory).getInstance(GetConversationsDto.class, conversations);
        verifyNoMoreInteractions(participantService, conversationService, objectFactory);

        assertEquals("GetCoversationsDto is returned as a result", getConversationsDto, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetConversationsByParticipantForLargeAmountOfDataBeingRetrieved() {
        Long participantId = 1L;
        int page = 0;
        int size = 200;

        conversationController.getConversationsByParticipant(participantId, page, size);

        verifyNoMoreInteractions(participantService, conversationService, objectFactory);
    }
}
