package club.tempvs.message.controller;

import club.tempvs.message.api.BadRequestException;
import club.tempvs.message.api.NotFoundException;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.*;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.service.MessageService;
import club.tempvs.message.service.ParticipantService;
import club.tempvs.message.util.AuthHelper;
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

    private static UpdateParticipantsDto.Action addAction = UpdateParticipantsDto.Action.ADD;
    private static UpdateParticipantsDto.Action removeAction = UpdateParticipantsDto.Action.REMOVE;

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
    private AuthHelper authHelper;
    @Mock
    private CreateConversationDto createConversationDto;
    @Mock
    private Message message;
    @Mock
    private Participant author;
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
    @Mock
    private UpdateParticipantsDto updateParticipantsDto;

    @Before
    public void setup() {
        conversationController = new ConversationController(objectFactory, conversationService, participantService,
                messageService, authHelper);
    }

    @Test
    public void testGetPong() {
        assertEquals("getPong() method returns 'pong!' string", "pong!", conversationController.getPong());
    }

    @Test
    public void testCreateConversation() {
        String token = "token";
        String text = "text";
        String name = "name";
        Set<Long> receiverIds = new HashSet<>();
        receiverIds.add(2L);
        receiverIds.add(3L);
        Set<Participant> receivers = new HashSet<>();
        receivers.add(receiver);
        receivers.add(participant);
        List<Message> messages = Arrays.asList(message, message, message);

        when(createConversationDto.getAuthor()).thenReturn(1L);
        when(createConversationDto.getReceivers()).thenReturn(receiverIds);
        when(participantService.getParticipant(1L)).thenReturn(author);
        when(participantService.getParticipant(2L)).thenReturn(receiver);
        when(participantService.getParticipant(3L)).thenReturn(participant);
        when(createConversationDto.getText()).thenReturn(text);
        when(createConversationDto.getName()).thenReturn(name);
        when(messageService.createMessage(author, receivers, text,false)).thenReturn(message);
        when(conversationService.createConversation(author, receivers, name, message)).thenReturn(conversation);
        when(conversation.getMessages()).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.createConversation(token, createConversationDto);

        verify(createConversationDto).validate();
        verify(createConversationDto).getAuthor();
        verify(createConversationDto).getReceivers();
        verify(participantService).getParticipant(1L);
        verify(participantService).getParticipant(2L);
        verify(participantService).getParticipant(3L);
        verify(createConversationDto).getText();
        verify(createConversationDto).getName();
        verify(messageService).createMessage(author, receivers, text, false);
        verify(conversationService).createConversation(author, receivers, name, message);
        verify(conversation).getMessages();
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages);
        verifyNoMoreInteractions(message, createConversationDto, participantService, messageService, conversationService, objectFactory);

        assertEquals("Result is a conversation", result, getConversationDto);
    }

    @Test
    public void testCreateConversationForExistentDialogue() {
        Long authorId = 1L;
        Long receiverId = 2L;
        String token = "token";
        String text = "text";
        String name = "name";
        Set<Long> receiverIds = new HashSet<>();
        receiverIds.add(2L);
        Set<Participant> receivers = new HashSet<>();
        receivers.add(receiver);
        Set<Participant> participants = new HashSet<>();
        participants.add(author);
        participants.add(receiver);
        List<Message> messages = Arrays.asList(message, message, message);

        when(createConversationDto.getAuthor()).thenReturn(authorId);
        when(createConversationDto.getReceivers()).thenReturn(receiverIds);
        when(participantService.getParticipant(authorId)).thenReturn(author);
        when(participantService.getParticipant(receiverId)).thenReturn(receiver);
        when(createConversationDto.getText()).thenReturn(text);
        when(createConversationDto.getName()).thenReturn(name);
        when(messageService.createMessage(author, receivers, text,false)).thenReturn(message);
        when(conversationService.findDialogue(author, receiver)).thenReturn(conversation);
        when(conversationService.addMessage(conversation, message)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.createConversation(token, createConversationDto);

        verify(createConversationDto).validate();
        verify(createConversationDto).getAuthor();
        verify(createConversationDto).getReceivers();
        verify(participantService).getParticipant(authorId);
        verify(participantService).getParticipant(receiverId);
        verify(createConversationDto).getText();
        verify(createConversationDto).getName();
        verify(messageService).createMessage(author, receivers, text, false);
        verify(conversationService).findDialogue(author, receiver);
        verify(conversationService).addMessage(conversation, message);
        verify(messageService).getMessagesFromConversation(conversation);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages);
        verifyNoMoreInteractions(message, createConversationDto, participantService, messageService, conversationService, objectFactory);

        assertEquals("Result is a conversation", result, getConversationDto);
    }

    @Test
    public void testGetConversation() {
        String token = "token";
        long id = 1L;
        int page = 0;
        int size = 20;
        List<Message> messages = Arrays.asList(message, message, message);

        when(conversationService.getConversation(id)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, page, size)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.getConversation(token, id, page, size);

        verify(conversationService).getConversation(id);
        verify(messageService).getMessagesFromConversation(conversation, page, size);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages);
        verifyNoMoreInteractions(message, conversationService, messageService, objectFactory, getConversationDto);

        assertEquals("Result is a conversation", result, getConversationDto);
    }

    @Test(expected = BadRequestException.class)
    public void testGetConversationForLargeAmountOfDataPerRequest() {
        String token = "token";
        long id = 1L;
        int page = 0;
        int size = 21;

        conversationController.getConversation(token, id, page, size);

        verifyNoMoreInteractions(message, conversationService, messageService, objectFactory, getConversationDto);
    }

    @Test
    public void testGetConversationsByParticipant() {
        String token = "token";
        Long participantId = 1L;
        int page = 0;
        int size = 20;
        List<Conversation> conversations = new ArrayList<>();
        conversations.add(conversation);

        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(conversationService.getConversationsByParticipant(participant, page, size)).thenReturn(conversations);
        when(objectFactory.getInstance(GetConversationsDto.class, conversations)).thenReturn(getConversationsDto);

        GetConversationsDto result = conversationController.getConversationsByParticipant(token, participantId, page, size);

        verify(participantService).getParticipant(participantId);
        verify(conversationService).getConversationsByParticipant(participant, page, size);
        verify(objectFactory).getInstance(GetConversationsDto.class, conversations);
        verifyNoMoreInteractions(participantService, conversationService, objectFactory);

        assertEquals("GetCoversationsDto is returned as a result", getConversationsDto, result);
    }

    @Test(expected = BadRequestException.class)
    public void testGetConversationsByParticipantForLargeAmountOfDataBeingRetrieved() {
        String token = "token";
        Long participantId = 1L;
        int page = 0;
        int size = 200;

        conversationController.getConversationsByParticipant(token, participantId, page, size);

        verifyNoMoreInteractions(participantService, conversationService, objectFactory);
    }

    @Test
    public void testAddMessage() {
        String token = "token";
        Long authorId = 1L;
        Long conversationId = 2L;
        Set<Participant> participants = new HashSet<>();
        participants.add(receiver);
        String text = "new message text";
        Boolean isSystem = Boolean.FALSE;

        when(addMessageDto.getAuthor()).thenReturn(authorId);
        when(addMessageDto.getText()).thenReturn(text);
        when(addMessageDto.getSystem()).thenReturn(isSystem);
        when(participantService.getParticipant(authorId)).thenReturn(author);
        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);
        when(messageService.createMessage(conversation, author, participants, text, isSystem)).thenReturn(message);
        when(conversationService.addMessage(conversation, message)).thenReturn(conversation);

        ResponseEntity result = conversationController.addMessage(token, conversationId, addMessageDto);

        verify(addMessageDto).validate();
        verify(addMessageDto).getAuthor();
        verify(addMessageDto).getText();
        verify(addMessageDto).getSystem();
        verify(participantService).getParticipant(authorId);
        verify(conversationService).getConversation(conversationId);
        verify(conversation).getParticipants();
        verify(messageService).createMessage(conversation, author, participants, text, isSystem);
        verify(conversationService).addMessage(conversation, message);
        verifyNoMoreInteractions(
                addMessageDto, participantService, conversationService, conversation, messageService, objectFactory);

        assertTrue("Status code 200 is returned", result.getStatusCodeValue() == 200);
    }

    @Test
    public void testAddMessageForMissingConversation() {
        String token = "token";
        Long authorId = 1L;
        Long conversationId = 2L;
        Set<Participant> participants = new HashSet<>();
        participants.add(receiver);
        String text = "new message text";
        Boolean isSystem = Boolean.FALSE;

        when(addMessageDto.getAuthor()).thenReturn(authorId);
        when(addMessageDto.getText()).thenReturn(text);
        when(addMessageDto.getSystem()).thenReturn(isSystem);
        when(conversationService.getConversation(conversationId)).thenReturn(null);

        ResponseEntity result = conversationController.addMessage(token, conversationId, addMessageDto);

        verify(addMessageDto).validate();
        verify(addMessageDto).getAuthor();
        verify(addMessageDto).getText();
        verify(addMessageDto).getSystem();
        verify(conversationService).getConversation(conversationId);
        verifyNoMoreInteractions(
                addMessageDto, participantService, conversationService, conversation, messageService, objectFactory);

        assertTrue("Status code 404 is returned", result.getStatusCodeValue() == 404);
    }

    @Test
    public void testUpdateParticipantsForAdd() {
        String token = "token";
        Long conversationId = 1L;
        Long initiatorId = 2L;
        Long subjectId = 3L;
        int page = 0;
        int max = 20;
        List<Message> messages = Arrays.asList(message, message);

        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(updateParticipantsDto.getInitiator()).thenReturn(initiatorId);
        when(updateParticipantsDto.getSubject()).thenReturn(subjectId);
        when(updateParticipantsDto.getAction()).thenReturn(addAction);
        when(participantService.getParticipant(initiatorId)).thenReturn(author);
        when(participantService.getParticipant(subjectId)).thenReturn(receiver);
        when(conversationService.addParticipant(conversation, author, receiver)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, page, max)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.updateParticipants(token, conversationId, updateParticipantsDto);

        verify(updateParticipantsDto).validate();
        verify(conversationService).getConversation(conversationId);
        verify(updateParticipantsDto).getInitiator();
        verify(updateParticipantsDto).getSubject();
        verify(updateParticipantsDto).getAction();
        verify(participantService).getParticipant(initiatorId);
        verify(participantService).getParticipant(subjectId);
        verify(conversationService).addParticipant(conversation, author, receiver);
        verify(messageService).getMessagesFromConversation(conversation, page, max);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages);
        verifyNoMoreInteractions(conversationService, updateParticipantsDto, participantService, objectFactory);

        assertEquals("GetConversationDto is returned as a result", getConversationDto, result);
    }

    @Test
    public void testUpdateParticipantsForRemove() {
        String token = "token";
        Long conversationId = 1L;
        Long initiatorId = 2L;
        Long subjectId = 3L;
        int page = 0;
        int max = 20;
        List<Message> messages = Arrays.asList(message, message);

        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(updateParticipantsDto.getInitiator()).thenReturn(initiatorId);
        when(updateParticipantsDto.getSubject()).thenReturn(subjectId);
        when(updateParticipantsDto.getAction()).thenReturn(removeAction);
        when(participantService.getParticipant(initiatorId)).thenReturn(author);
        when(participantService.getParticipant(subjectId)).thenReturn(receiver);
        when(conversationService.removeParticipant(conversation, author, receiver)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, page, max)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.updateParticipants(token, conversationId, updateParticipantsDto);

        verify(updateParticipantsDto).validate();
        verify(conversationService).getConversation(conversationId);
        verify(updateParticipantsDto).getInitiator();
        verify(updateParticipantsDto).getSubject();
        verify(updateParticipantsDto).getAction();
        verify(participantService).getParticipant(initiatorId);
        verify(participantService).getParticipant(subjectId);
        verify(conversationService).removeParticipant(conversation, author, receiver);
        verify(messageService).getMessagesFromConversation(conversation, page, max);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages);
        verifyNoMoreInteractions(conversationService, updateParticipantsDto, participantService, objectFactory);

        assertEquals("GetConversationDto is returned as a result", getConversationDto, result);
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateParticipantsForEmptyConversation() {
        String token = "token";
        Long conversationId = 1L;
        Long initiatorId = 2L;
        Long subjectId = 3L;

        when(conversationService.getConversation(conversationId)).thenReturn(null);
        when(updateParticipantsDto.getInitiator()).thenReturn(initiatorId);
        when(updateParticipantsDto.getSubject()).thenReturn(subjectId);
        when(updateParticipantsDto.getAction()).thenReturn(removeAction);

        conversationController.updateParticipants(token, conversationId, updateParticipantsDto);

        verify(updateParticipantsDto).validate();
        verify(conversationService).getConversation(conversationId);
        verify(updateParticipantsDto).getInitiator();
        verify(updateParticipantsDto).getSubject();
        verify(updateParticipantsDto).getAction();
        verifyNoMoreInteractions(conversationService, updateParticipantsDto, participantService, objectFactory);
    }
}
