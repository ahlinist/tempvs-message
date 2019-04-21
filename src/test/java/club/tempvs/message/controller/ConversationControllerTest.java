package club.tempvs.message.controller;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.*;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.service.MessageService;
import club.tempvs.message.service.ParticipantService;
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
    private CreateConversationDto createConversationDto;
    @Mock
    private Message message;
    @Mock
    private Participant participant;
    @Mock
    private Conversation conversation;
    @Mock
    private GetConversationDto getConversationDto;
    @Mock
    private AddMessageDto addMessageDto;
    @Mock
    private AddParticipantsDto addParticipantsDto;
    @Mock
    private UpdateConversationNameDto updateConversationNameDto;
    @Mock
    private ReadMessagesDto readMessagesDto;
    @Mock
    private UserInfoDto userInfoDto;
    @Mock
    private GetConversationsDto getConversationsDto;

    @Before
    public void setup() {
        conversationController = new ConversationController(conversationService, participantService, messageService);
    }

    @Test
    public void testCreateConversation() {
        Long receiverId = 2L;
        Long participantId = 3L;
        String text = "text";
        String name = "name";
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(receiverId, participantId));

        when(createConversationDto.getReceivers()).thenReturn(receiverIds);
        when(createConversationDto.getText()).thenReturn(text);
        when(createConversationDto.getName()).thenReturn(name);
        when(conversationService.createConversation(receiverIds, name, text)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.createConversation(createConversationDto);

        verify(conversationService).createConversation(receiverIds, name, text);
        verifyNoMoreInteractions(participantService, messageService, conversationService);

        assertEquals("GetConversationDto is returned", result,getConversationDto);
    }

    @Test
    public void testGetConversation() {
        long id = 1L;
        int page = 0;
        int size = 40;

        when(conversationService.getConversation(id, page, size)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.getConversation(id, page, size);

        verify(conversationService).getConversation(id, page, size);
        verifyNoMoreInteractions(conversationService);

        assertTrue("Result is a conversation", result instanceof GetConversationDto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetConversationForWrongPaging() {
        long id = 1L;
        int page = 0;
        int size = 41;

        conversationController.getConversation(id, page, size);
    }

    @Test
    public void testGetConversationsByParticipant() {
        int page = 0;
        int size = 40;

        when(conversationService.getConversationsAttended(page, size)).thenReturn(getConversationsDto);

        ResponseEntity result = conversationController.getConversationsByParticipant(page, size);

        verify(conversationService).getConversationsAttended(page, size);
        verifyNoMoreInteractions(conversationService);

        assertTrue("GetConversationsDto object is returned as a body", result.getBody() instanceof GetConversationsDto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetConversationsByParticipantForLargeAmountOfDataBeingRetrieved() {
        int page = 0;
        int size = 200;

        conversationController.getConversationsByParticipant(page, size);
    }

    @Test
    public void testAddMessage() {
        Long conversationId = 2L;
        String text = "new message text";

        when(addMessageDto.getText()).thenReturn(text);
        when(conversationService.addMessage(conversationId, text)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.addMessage(conversationId, addMessageDto);

        verify(conversationService).addMessage(conversationId, text);
        verifyNoMoreInteractions(conversationService);

        assertEquals("GetConversationDto is returned", getConversationDto, result);
    }

    @Test
    public void testAddParticipant() {
        Long conversationId = 1L;
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(3L));

        when(addParticipantsDto.getParticipants()).thenReturn(receiverIds);
        when(conversationService.addParticipants(conversationId, receiverIds)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.addParticipants(conversationId, addParticipantsDto);

        verify(conversationService).addParticipants(conversationId, receiverIds);
        verifyNoMoreInteractions(conversationService);

        assertEquals("GetConversationDto is returned as a result", getConversationDto,result);
    }

    @Test
    public void testRemoveParticipant() {
        Long conversationId = 1L;
        Long subjectId = 3L;

        when(conversationService.removeParticipant(conversationId, subjectId)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.removeParticipant(conversationId, subjectId);

        verify(conversationService).removeParticipant(conversationId, subjectId);
        verifyNoMoreInteractions(messageService, conversationService, participantService);

        assertTrue("GetConversationDto is returned as a result", result instanceof GetConversationDto);
    }

    @Test
    public void testCountConversations() {
        long conversationsCount = 3L;

        when(conversationService.countUpdatedConversationsPerParticipant()).thenReturn(conversationsCount);

        ResponseEntity result = conversationController.countConversations();

        verify(conversationService).countUpdatedConversationsPerParticipant();
        verifyNoMoreInteractions(participantService);

        assertTrue("3L returned as a response as a new conversations count", result.getStatusCodeValue() == 200);
    }

    @Test
    public void testUpdateConversationName() {
        Long conversationId = 1L;
        String conversationName = "name";

        when(updateConversationNameDto.getName()).thenReturn(conversationName);
        when(conversationService.rename(conversationId, conversationName)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.renameConversation(conversationId, updateConversationNameDto);

        verify(conversationService).rename(conversationId, conversationName);
        verifyNoMoreInteractions(conversationService);

        assertTrue("GetConversationDto is returned as a result", result instanceof GetConversationDto);
    }

    @Test
    public void testReadMessages() {
        Long conversationId = 1L;
        Long participantId = 4L;
        List<Long> messageIds = Arrays.asList(2L, 3L);
        List<Message> messages = Arrays.asList(message, message);

        when(conversationService.findOne(conversationId)).thenReturn(conversation);
        when(readMessagesDto.getMessages()).thenReturn(messageIds);
        when(messageService.findMessagesByIds(messageIds)).thenReturn(messages);
        when(userInfoDto.getProfileId()).thenReturn(participantId);
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(messageService.markAsRead(conversation, participant, messages)).thenReturn(messages);

        conversationController.readMessages(userInfoDto, conversationId, readMessagesDto);

        verify(conversationService).findOne(conversationId);
        verify(readMessagesDto).getMessages();
        verify(messageService).findMessagesByIds(messageIds);
        verify(userInfoDto).getProfileId();
        verify(participantService).getParticipant(participantId);
        verify(messageService).markAsRead(conversation, participant, messages);
        verifyNoMoreInteractions(conversationService, readMessagesDto, messageService,
                userInfoDto, participantService, conversation, message, participant);
    }

    @Test(expected = IllegalStateException.class)
    public void testReadMessagesForMissingMessages() {
        Long conversationId = 1L;
        Long participantId = 4L;
        List<Long> messageIds = Arrays.asList(2L, 3L);
        List<Message> messages = Arrays.asList(message, null);

        when(conversationService.findOne(conversationId)).thenReturn(conversation);
        when(userInfoDto.getProfileId()).thenReturn(participantId);
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(readMessagesDto.getMessages()).thenReturn(messageIds);
        when(messageService.findMessagesByIds(messageIds)).thenReturn(messages);

        conversationController.readMessages(userInfoDto, conversationId, readMessagesDto);
    }
}
