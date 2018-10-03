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
import org.springframework.http.ResponseEntity;

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
    @Mock
    private AddMessageDto addMessageDto;

    @Before
    public void setup() {
        conversationController = new ConversationController(objectFactory, conversationService, participantService, messageService);
    }

    @Test
    public void testGetPong() {
        assertEquals("getPong() method returns 'pong!' string", "pong!", conversationController.getPong());
    }

    @Test
    public void testCreateConversation() {
        String text = "text";
        String name = "name";
        Set<Long> receiverIds = new HashSet<>();
        receiverIds.add(2L);
        Set<Participant> receivers = new HashSet<>();
        receivers.add(receiver);
        List<Message> messages = Arrays.asList(message, message, message);

        when(createConversationDto.getSender()).thenReturn(1L);
        when(participantService.getParticipant(1L)).thenReturn(sender);
        when(participantService.getParticipant(2L)).thenReturn(receiver);
        when(createConversationDto.getReceivers()).thenReturn(receiverIds);
        when(createConversationDto.getText()).thenReturn(text);
        when(createConversationDto.getName()).thenReturn(name);
        when(messageService.createMessage(sender, receivers, text,false)).thenReturn(message);
        when(conversationService.createConversation(sender, receivers, name, message)).thenReturn(conversation);
        when(conversation.getMessages()).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.createConversation(createConversationDto);

        verify(createConversationDto).validate();
        verify(createConversationDto).getSender();
        verify(participantService).getParticipant(1L);
        verify(participantService).getParticipant(2L);
        verify(createConversationDto).getReceivers();
        verify(createConversationDto).getText();
        verify(createConversationDto).getName();
        verify(messageService).createMessage(sender, receivers, text, false);
        verify(conversationService).createConversation(sender, receivers, name, message);
        verify(conversation).getMessages();
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages);
        verifyNoMoreInteractions(message, createConversationDto, participantService, messageService, conversationService, objectFactory);

        assertEquals("Result is a conversation", result, getConversationDto);
    }

    @Test
    public void testGetConversation() {
        long id = 1L;
        int page = 0;
        int size = 20;
        List<Message> messages = Arrays.asList(message, message, message);

        when(conversationService.getConversation(id)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, page, size)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.getConversation(id, page, size);

        verify(conversationService).getConversation(id);
        verify(messageService).getMessagesFromConversation(conversation, page, size);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages);
        verifyNoMoreInteractions(message, conversationService, messageService, objectFactory, getConversationDto);

        assertEquals("Result is a conversation", result, getConversationDto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetConversationForLargeAmountOfDataPerRequest() {
        long id = 1L;
        int page = 0;
        int size = 21;

        GetConversationDto result = conversationController.getConversation(id, page, size);

        verifyNoMoreInteractions(message, conversationService, messageService, objectFactory, getConversationDto);
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

    @Test
    public void testAddMessage() {
        Long senderId = 1L;
        Long conversationId = 2L;
        Set<Participant> participants = new HashSet<>();
        participants.add(receiver);
        String text = "new message text";
        Boolean isSystem = Boolean.FALSE;

        when(addMessageDto.getSender()).thenReturn(senderId);
        when(addMessageDto.getText()).thenReturn(text);
        when(addMessageDto.getSystem()).thenReturn(isSystem);
        when(participantService.getParticipant(senderId)).thenReturn(sender);
        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);
        when(messageService.createMessage(conversation, sender, participants, text, isSystem)).thenReturn(message);
        when(conversationService.addMessage(conversation, message)).thenReturn(conversation);

        ResponseEntity result = conversationController.addMessage(conversationId, addMessageDto);

        verify(addMessageDto).validate();
        verify(addMessageDto).getSender();
        verify(addMessageDto).getText();
        verify(addMessageDto).getSystem();
        verify(participantService).getParticipant(senderId);
        verify(conversationService).getConversation(conversationId);
        verify(conversation).getParticipants();
        verify(messageService).createMessage(conversation, sender, participants, text, isSystem);
        verify(conversationService).addMessage(conversation, message);
        verifyNoMoreInteractions(
                addMessageDto, participantService, conversationService, conversation, messageService, objectFactory);

        assertTrue("Status code 200 is returned", result.getStatusCodeValue() == 200);
    }

    @Test
    public void testAddMessageForMissingConversation() {
        Long senderId = 1L;
        Long conversationId = 2L;
        Set<Participant> participants = new HashSet<>();
        participants.add(receiver);
        String text = "new message text";
        Boolean isSystem = Boolean.FALSE;

        when(addMessageDto.getSender()).thenReturn(senderId);
        when(addMessageDto.getText()).thenReturn(text);
        when(addMessageDto.getSystem()).thenReturn(isSystem);
        when(participantService.getParticipant(senderId)).thenReturn(sender);
        when(conversationService.getConversation(conversationId)).thenReturn(null);

        ResponseEntity result = conversationController.addMessage(conversationId, addMessageDto);

        verify(addMessageDto).validate();
        verify(addMessageDto).getSender();
        verify(addMessageDto).getText();
        verify(addMessageDto).getSystem();
        verify(participantService).getParticipant(senderId);
        verify(conversationService).getConversation(conversationId);
        verifyNoMoreInteractions(
                addMessageDto, participantService, conversationService, conversation, messageService, objectFactory);

        assertTrue("Status code 404 is returned", result.getStatusCodeValue() == 404);
    }
}
