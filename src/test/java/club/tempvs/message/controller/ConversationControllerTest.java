package club.tempvs.message.controller;

import club.tempvs.message.dto.*;
import club.tempvs.message.service.ConversationService;
import com.google.common.collect.ImmutableSet;
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
    private CreateConversationDto createConversationDto;
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
    private GetConversationsDto getConversationsDto;

    @Before
    public void setup() {
        conversationController = new ConversationController(conversationService);
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
        verifyNoMoreInteractions(conversationService);

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

        assertEquals("Result is a conversation", getConversationDto, result);
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

        GetConversationsDto result = conversationController.getConversationsByParticipant(page, size);

        verify(conversationService).getConversationsAttended(page, size);
        verifyNoMoreInteractions(conversationService);

        assertEquals("GetConversationsDto object is returned as a body", getConversationsDto, result);
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
        Set<Long> receiverIds = ImmutableSet.of(3L);

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
        verifyNoMoreInteractions(conversationService);

        assertEquals("GetConversationDto is returned as a result", getConversationDto, result);
    }

    @Test
    public void testCountConversations() {
        long conversationsCount = 3L;

        when(conversationService.countUpdatedConversationsPerParticipant()).thenReturn(conversationsCount);

        ResponseEntity result = conversationController.countConversations();

        verify(conversationService).countUpdatedConversationsPerParticipant();
        verifyNoMoreInteractions(conversationService);

        assertEquals("3L returned as a response as a new conversations count", 200, result.getStatusCodeValue());
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

        assertEquals("GetConversationDto is returned as a result", getConversationDto, result);
    }

    @Test
    public void testReadMessages() {
        Long conversationId = 1L;
        List<Long> messageIds = Arrays.asList(2L, 3L);

        when(readMessagesDto.getMessages()).thenReturn(messageIds);

        conversationController.readMessages(conversationId, readMessagesDto);

        verify(conversationService).markMessagesAsRead(conversationId, messageIds);
        verifyNoMoreInteractions(conversationService);
    }
}
