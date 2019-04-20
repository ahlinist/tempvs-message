package club.tempvs.message.controller;

import club.tempvs.message.api.ForbiddenException;
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

import java.time.Instant;
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
    private Participant author, receiver, participant;
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
        Long callerId = 5L;
        String timeZone = "UTC";
        List<Message> messages = Arrays.asList(message, message, message);
        Set<Participant> participants = new HashSet<>(Arrays.asList(participant, receiver));

        when(userInfoDto.getProfileId()).thenReturn(callerId);
        when(userInfoDto.getTimezone()).thenReturn(timeZone);
        when(participantService.getParticipant(callerId)).thenReturn(participant);
        when(conversationService.getConversation(id)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);
        when(conversation.getType()).thenReturn(Conversation.Type.CONFERENCE);
        when(message.getAuthor()).thenReturn(author);
        when(message.getCreatedDate()).thenReturn(Instant.now());
        when(messageService.getMessagesFromConversation(conversation, page, size)).thenReturn(messages);

        GetConversationDto result = conversationController.getConversation(userInfoDto, id, page, size);

        verify(participantService).getParticipant(callerId);
        verify(conversationService).getConversation(id);
        verify(messageService).getMessagesFromConversation(conversation, page, size);
        verifyNoMoreInteractions(participantService, conversationService, messageService);

        assertTrue("Result is a conversation", result instanceof GetConversationDto);
    }

    @Test(expected = ForbiddenException.class)
    public void testGetConversationWithWrongCaller() {
        long id = 1L;
        int page = 0;
        int size = 40;
        Long callerId = 5L;
        Set<Participant> participants = new HashSet<>(Arrays.asList(author, receiver));

        when(userInfoDto.getProfileId()).thenReturn(callerId);
        when(participantService.getParticipant(callerId)).thenReturn(participant);
        when(conversationService.getConversation(id)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);

        conversationController.getConversation(userInfoDto, id, page, size);

        verify(userInfoDto).getProfileId();
        verify(participantService).getParticipant(callerId);
        verify(conversationService).getConversation(id);
        verify(conversation).getParticipants();
        verifyNoMoreInteractions(message, conversation,
                userInfoDto, participantService, conversationService, messageService, getConversationDto);
    }

    @Test
    public void testGetConversationsByParticipant() {
        Long participantId = 1L;
        int page = 0;
        int size = 40;
        String timeZone = "UTC";
        List<Conversation> conversations = new ArrayList<>();
        conversations.add(conversation);
        List<ConversationDtoBean> conversationDtoBeans = new ArrayList<>();
        conversationDtoBeans.add(new ConversationDtoBean());

        when(conversationService.getConversationsAttended(page, size)).thenReturn(getConversationsDto);

        ResponseEntity result = conversationController.getConversationsByParticipant(page, size);

        verify(conversationService).getConversationsAttended(page, size);
        verifyNoMoreInteractions(participantService, conversationService);

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
        Long authorId = 1L;
        Long conversationId = 2L;
        Set<Participant> participants = new HashSet<>();
        participants.add(receiver);
        String text = "new message text";
        String timeZone = "UTC";
        int page = 0;
        int size = 40;
        List<Message> messages = Arrays.asList(message, message, message);

        when(addMessageDto.getText()).thenReturn(text);
        when(userInfoDto.getProfileId()).thenReturn(authorId);
        when(userInfoDto.getTimezone()).thenReturn(timeZone);
        when(participantService.getParticipant(authorId)).thenReturn(author);
        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);
        when(message.getAuthor()).thenReturn(author);
        when(message.getCreatedDate()).thenReturn(Instant.now());
        when(messageService.createMessage(author, participants, text, false, null, null)).thenReturn(message);
        when(conversationService.addMessage(conversation, message)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, page, size)).thenReturn(messages);

        GetConversationDto result = conversationController.addMessage(userInfoDto, conversationId, addMessageDto);

        verify(participantService).getParticipant(authorId);
        verify(conversationService).getConversation(conversationId);
        verify(messageService).createMessage(author, participants, text, false, null, null);
        verify(conversationService).addMessage(conversation, message);
        verify(messageService).getMessagesFromConversation(conversation, page, size);
        verifyNoMoreInteractions(participantService, conversationService, messageService);

        assertTrue("GetConversationDto object is returned as a body", result instanceof GetConversationDto);
    }

    @Test
    public void testAddParticipant() {
        Long conversationId = 1L;
        Long initiatorId = 2L;
        Long receiverId = 3L;
        int page = 0;
        int max = 40;
        String timeZone = "UTC";
        List<Message> messages = Arrays.asList(message, message);
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(receiverId));
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver));
        Set<Participant> participants = new HashSet<>(Arrays.asList(participant));

        when(userInfoDto.getProfileId()).thenReturn(initiatorId);
        when(userInfoDto.getTimezone()).thenReturn(timeZone);
        when(addParticipantsDto.getParticipants()).thenReturn(receiverIds);
        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(participantService.getParticipant(initiatorId)).thenReturn(author);
        when(participantService.getParticipants(receiverIds)).thenReturn(receivers);
        when(conversation.getParticipants()).thenReturn(participants);
        when(message.getAuthor()).thenReturn(author);
        when(message.getCreatedDate()).thenReturn(Instant.now());
        when(conversationService.addParticipants(conversation, author, receivers)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, page, max)).thenReturn(messages);

        GetConversationDto result = conversationController.addParticipants(userInfoDto, conversationId, addParticipantsDto);

        verify(conversationService).getConversation(conversationId);
        verify(participantService).getParticipant(initiatorId);
        verify(participantService).getParticipants(receiverIds);
        verify(conversationService).addParticipants(conversation, author, receivers);
        verify(messageService).getMessagesFromConversation(conversation, page, max);
        verifyNoMoreInteractions(conversationService, participantService, messageService);

        assertTrue("GetConversationDto is returned as a result", result instanceof GetConversationDto);
    }

    @Test(expected = IllegalStateException.class)
    public void testAddParticipantsForExistingMember() {
        Long conversationId = 1L;
        Long initiatorId = 2L;
        Long subjectId = 3L;
        Set<Long> subjectIds = new HashSet<>(Arrays.asList(subjectId));
        Set<Participant> participants = new HashSet<>(Arrays.asList(receiver));

        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(userInfoDto.getProfileId()).thenReturn(initiatorId);
        when(participantService.getParticipant(initiatorId)).thenReturn(author);
        when(addParticipantsDto.getParticipants()).thenReturn(subjectIds);
        when(participantService.getParticipants(subjectIds)).thenReturn(participants);
        when(conversation.getParticipants()).thenReturn(participants);

        conversationController.addParticipants(userInfoDto, conversationId, addParticipantsDto);
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

        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(readMessagesDto.getMessages()).thenReturn(messageIds);
        when(messageService.findMessagesByIds(messageIds)).thenReturn(messages);
        when(userInfoDto.getProfileId()).thenReturn(participantId);
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(messageService.markAsRead(conversation, participant, messages)).thenReturn(messages);

        conversationController.readMessages(userInfoDto, conversationId, readMessagesDto);

        verify(conversationService).getConversation(conversationId);
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

        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(userInfoDto.getProfileId()).thenReturn(participantId);
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(readMessagesDto.getMessages()).thenReturn(messageIds);
        when(messageService.findMessagesByIds(messageIds)).thenReturn(messages);

        conversationController.readMessages(userInfoDto, conversationId, readMessagesDto);
    }
}
