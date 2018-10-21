package club.tempvs.message.controller;

import club.tempvs.message.api.BadRequestException;
import club.tempvs.message.api.ForbiddenException;
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
import org.springframework.http.HttpHeaders;
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
    @Mock
    private ParticipantDto authorDto;
    @Mock
    private ParticipantDto receiverDto;
    @Mock
    private ParticipantDto participantDto;

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
        Long authorId = 1L;
        Long receiverId = 2L;
        Long participantId = 3L;
        String token = "token";
        String text = "text";
        String name = "name";
        Set<ParticipantDto> receiverDtos = new HashSet<>(Arrays.asList(receiverDto, participantDto));
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver, participant));
        List<Message> messages = Arrays.asList(message, message, message);

        when(createConversationDto.getAuthor()).thenReturn(authorDto);
        when(createConversationDto.getReceivers()).thenReturn(receiverDtos);
        when(authorDto.getId()).thenReturn(authorId);
        when(receiverDto.getId()).thenReturn(receiverId);
        when(participantDto.getId()).thenReturn(participantId);
        when(participantService.getParticipant(authorId)).thenReturn(author);
        when(participantService.getParticipant(receiverId)).thenReturn(receiver);
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(createConversationDto.getText()).thenReturn(text);
        when(createConversationDto.getName()).thenReturn(name);
        when(messageService.createMessage(author, receivers, text)).thenReturn(message);
        when(conversationService.createConversation(author, receivers, name, message)).thenReturn(conversation);
        when(conversation.getMessages()).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, author)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.createConversation(token, createConversationDto);

        verify(createConversationDto).validate();
        verify(createConversationDto).getAuthor();
        verify(authorDto).getId();
        verify(receiverDto).getId();
        verify(participantDto).getId();
        verify(createConversationDto).getReceivers();
        verify(participantService).getParticipant(authorId);
        verify(participantService).getParticipant(receiverId);
        verify(participantService).getParticipant(participantId);
        verify(createConversationDto).getText();
        verify(createConversationDto).getName();
        verify(messageService).createMessage(author, receivers, text);
        verify(conversationService).createConversation(author, receivers, name, message);
        verify(conversation).getMessages();
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, author);
        verifyNoMoreInteractions(authorDto, message, receiverDto, participantDto,
                createConversationDto, participantService, messageService, conversationService, objectFactory);

        assertEquals("Result is a getConversationDto", result, getConversationDto);
    }

    @Test
    public void testCreateConversationForExistentDialogue() {
        Long authorId = 1L;
        Long receiverId = 2L;
        String token = "token";
        String text = "text";
        String name = "name";
        Set<ParticipantDto> receiverDtos = new HashSet<>(Arrays.asList(receiverDto));
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver));
        List<Message> messages = Arrays.asList(message, message, message);

        when(createConversationDto.getAuthor()).thenReturn(authorDto);
        when(createConversationDto.getReceivers()).thenReturn(receiverDtos);
        when(authorDto.getId()).thenReturn(authorId);
        when(receiverDto.getId()).thenReturn(receiverId);
        when(participantService.getParticipant(authorId)).thenReturn(author);
        when(participantService.getParticipant(receiverId)).thenReturn(receiver);
        when(createConversationDto.getText()).thenReturn(text);
        when(createConversationDto.getName()).thenReturn(name);
        when(messageService.createMessage(author, receivers, text)).thenReturn(message);
        when(conversationService.findDialogue(author, receiver)).thenReturn(conversation);
        when(conversationService.addMessage(conversation, message)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, author)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.createConversation(token, createConversationDto);

        verify(createConversationDto).validate();
        verify(createConversationDto).getAuthor();
        verify(createConversationDto).getReceivers();
        verify(authorDto).getId();
        verify(receiverDto).getId();
        verify(participantService).getParticipant(authorId);
        verify(participantService).getParticipant(receiverId);
        verify(createConversationDto).getText();
        verify(createConversationDto).getName();
        verify(messageService).createMessage(author, receivers, text);
        verify(conversationService).findDialogue(author, receiver);
        verify(conversationService).addMessage(conversation, message);
        verify(messageService).getMessagesFromConversation(conversation);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, author);
        verifyNoMoreInteractions(authorDto, message, receiverDto,
                createConversationDto, participantService, messageService, conversationService, objectFactory);

        assertEquals("Result is a conversation", result, getConversationDto);
    }

    @Test(expected = BadRequestException.class)
    public void testCreateConversationWith1Participant() {
        Long authorId = 1L;
        String token = "token";
        String text = "text";
        String name = "name";
        Set<ParticipantDto> receiverDtos = new HashSet<>();
        Set<Participant> receivers = new HashSet<>();

        when(createConversationDto.getAuthor()).thenReturn(authorDto);
        when(authorDto.getId()).thenReturn(authorId);
        when(createConversationDto.getReceivers()).thenReturn(receiverDtos);
        when(participantService.getParticipant(authorId)).thenReturn(author);
        when(createConversationDto.getText()).thenReturn(text);
        when(createConversationDto.getName()).thenReturn(name);
        when(messageService.createMessage(author, receivers, text)).thenReturn(message);

        conversationController.createConversation(token, createConversationDto);

        verify(createConversationDto).validate();
        verify(createConversationDto).getAuthor();
        verify(authorDto).getId();
        verify(createConversationDto).getReceivers();
        verify(participantService).getParticipant(authorId);
        verify(createConversationDto).getText();
        verify(createConversationDto).getName();
        verify(messageService).createMessage(author, receivers, text);
        verifyNoMoreInteractions(authorDto, message, receiverDto,
                createConversationDto, participantService, messageService, conversationService, objectFactory);
    }

    @Test
    public void testGetConversation() {
        String token = "token";
        long id = 1L;
        int page = 0;
        int size = 20;
        Long callerId = 5L;
        List<Message> messages = Arrays.asList(message, message, message);
        Set<Participant> participants = new HashSet<>(Arrays.asList(participant, receiver));

        when(participantService.getParticipant(callerId)).thenReturn(participant);
        when(conversationService.getConversation(id)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);
        when(messageService.getMessagesFromConversation(conversation, page, size)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, participant)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.getConversation(token, id, page, size, callerId);

        verify(participantService).getParticipant(callerId);
        verify(conversationService).getConversation(id);
        verify(conversation).getParticipants();
        verify(messageService).getMessagesFromConversation(conversation, page, size);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, participant);
        verifyNoMoreInteractions(message, conversation,
                participantService, conversationService, messageService, objectFactory, getConversationDto);

        assertEquals("Result is a conversation", result, getConversationDto);
    }

    @Test(expected = BadRequestException.class)
    public void testGetConversationForLargeAmountOfDataPerRequest() {
        String token = "token";
        long id = 1L;
        int page = 0;
        int size = 21;
        Long callerId = 5L;

        conversationController.getConversation(token, id, page, size, callerId);

        verifyNoMoreInteractions(message, conversation, conversationService, messageService, objectFactory, getConversationDto);
    }

    @Test(expected = ForbiddenException.class)
    public void testGetConversationWithWrongCaller() {
        String token = "token";
        long id = 1L;
        int page = 0;
        int size = 20;
        Long callerId = 5L;
        Set<Participant> participants = new HashSet<>(Arrays.asList(author, receiver));

        when(participantService.getParticipant(callerId)).thenReturn(participant);
        when(conversationService.getConversation(id)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);

        conversationController.getConversation(token, id, page, size, callerId);

        verify(participantService).getParticipant(callerId);
        verify(conversationService).getConversation(id);
        verify(conversation).getParticipants();
        verifyNoMoreInteractions(message, conversation,
                participantService, conversationService, messageService, objectFactory, getConversationDto);
    }

    @Test
    public void testGetConversationsByParticipant() {
        String token = "token";
        Long participantId = 1L;
        int page = 0;
        int size = 20;
        List<Conversation> conversations = new ArrayList<>();
        conversations.add(conversation);
        List<ConversationDtoBean> conversationDtoBeans = new ArrayList<>();
        conversationDtoBeans.add(new ConversationDtoBean());

        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(conversationService.getConversationsByParticipant(participant, page, size)).thenReturn(conversations);
        when(objectFactory.getInstance(GetConversationsDto.class, conversations, participant)).thenReturn(getConversationsDto);
        when(objectFactory.getInstance(HttpHeaders.class)).thenReturn(new HttpHeaders());
        when(getConversationsDto.getConversations()).thenReturn(conversationDtoBeans);

        ResponseEntity result = conversationController.getConversationsByParticipant(token, participantId, page, size);

        verify(participantService).getParticipant(participantId);
        verify(conversationService).getConversationsByParticipant(participant, page, size);
        verify(objectFactory).getInstance(GetConversationsDto.class, conversations, participant);
        verify(objectFactory).getInstance(HttpHeaders.class);
        verify(getConversationsDto).getConversations();
        verifyNoMoreInteractions(participantService, conversationService, objectFactory, getConversationsDto);

        assertEquals("GetCoversationsDto is returned as a result", getConversationsDto, result.getBody());
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
        int page = 0;
        int size = 20;
        List<Message> messages = Arrays.asList(message, message, message);

        when(addMessageDto.getAuthor()).thenReturn(authorDto);
        when(authorDto.getId()).thenReturn(authorId);
        when(addMessageDto.getText()).thenReturn(text);
        when(participantService.getParticipant(authorId)).thenReturn(author);
        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);
        when(messageService.createMessage(author, participants, text)).thenReturn(message);
        when(conversationService.addMessage(conversation, message)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, page, size)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, author)).thenReturn(getConversationDto);

        ResponseEntity result = conversationController.addMessage(token, conversationId, addMessageDto);

        verify(addMessageDto).validate();
        verify(addMessageDto).getAuthor();
        verify(authorDto).getId();
        verify(addMessageDto).getText();
        verify(participantService).getParticipant(authorId);
        verify(conversationService).getConversation(conversationId);
        verify(conversation).getParticipants();
        verify(messageService).createMessage(author, participants, text);
        verify(conversationService).addMessage(conversation, message);
        verify(messageService).getMessagesFromConversation(conversation, page, size);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, author);
        verifyNoMoreInteractions(authorDto,
                addMessageDto, participantService, conversationService, conversation, messageService, objectFactory);

        assertTrue("Status code 200 is returned", result.getStatusCodeValue() == 200);
        assertTrue("GetConversationDto object is returned as a body", result.getBody().equals(getConversationDto));
    }

    @Test
    public void testAddMessageForMissingConversation() {
        String token = "token";
        Long authorId = 1L;
        Long conversationId = 2L;
        Set<Participant> participants = new HashSet<>();
        participants.add(receiver);
        String text = "new message text";

        when(addMessageDto.getAuthor()).thenReturn(authorDto);
        when(authorDto.getId()).thenReturn(authorId);
        when(addMessageDto.getText()).thenReturn(text);
        when(conversationService.getConversation(conversationId)).thenReturn(null);

        ResponseEntity result = conversationController.addMessage(token, conversationId, addMessageDto);

        verify(addMessageDto).validate();
        verify(addMessageDto).getAuthor();
        verify(authorDto).getId();
        verify(addMessageDto).getText();
        verify(conversationService).getConversation(conversationId);
        verifyNoMoreInteractions(authorDto,
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
        when(updateParticipantsDto.getInitiator()).thenReturn(authorDto);
        when(authorDto.getId()).thenReturn(initiatorId);
        when(updateParticipantsDto.getSubject()).thenReturn(receiverDto);
        when(receiverDto.getId()).thenReturn(subjectId);
        when(updateParticipantsDto.getAction()).thenReturn(addAction);
        when(participantService.getParticipant(initiatorId)).thenReturn(author);
        when(participantService.getParticipant(subjectId)).thenReturn(receiver);
        when(conversationService.addParticipant(conversation, author, receiver)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, page, max)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, author)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.updateParticipants(token, conversationId, updateParticipantsDto);

        verify(updateParticipantsDto).validate();
        verify(conversationService).getConversation(conversationId);
        verify(updateParticipantsDto).getInitiator();
        verify(authorDto).getId();
        verify(updateParticipantsDto).getSubject();
        verify(receiverDto).getId();
        verify(updateParticipantsDto).getAction();
        verify(participantService).getParticipant(initiatorId);
        verify(participantService).getParticipant(subjectId);
        verify(conversationService).addParticipant(conversation, author, receiver);
        verify(messageService).getMessagesFromConversation(conversation, page, max);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, author);
        verifyNoMoreInteractions(authorDto,
                receiverDto, conversationService, updateParticipantsDto, participantService, objectFactory);

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
        when(updateParticipantsDto.getInitiator()).thenReturn(authorDto);
        when(authorDto.getId()).thenReturn(initiatorId);
        when(updateParticipantsDto.getSubject()).thenReturn(receiverDto);
        when(receiverDto.getId()).thenReturn(subjectId);
        when(updateParticipantsDto.getAction()).thenReturn(removeAction);
        when(participantService.getParticipant(initiatorId)).thenReturn(author);
        when(participantService.getParticipant(subjectId)).thenReturn(receiver);
        when(conversationService.removeParticipant(conversation, author, receiver)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, page, max)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, author)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.updateParticipants(token, conversationId, updateParticipantsDto);

        verify(updateParticipantsDto).validate();
        verify(conversationService).getConversation(conversationId);
        verify(updateParticipantsDto).getInitiator();
        verify(authorDto).getId();
        verify(updateParticipantsDto).getSubject();
        verify(receiverDto).getId();
        verify(updateParticipantsDto).getAction();
        verify(participantService).getParticipant(initiatorId);
        verify(participantService).getParticipant(subjectId);
        verify(conversationService).removeParticipant(conversation, author, receiver);
        verify(messageService).getMessagesFromConversation(conversation, page, max);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, author);
        verifyNoMoreInteractions(authorDto,
                receiverDto, conversationService, updateParticipantsDto, participantService, objectFactory);

        assertEquals("GetConversationDto is returned as a result", getConversationDto, result);
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateParticipantsForEmptyConversation() {
        String token = "token";
        Long conversationId = 1L;

        when(conversationService.getConversation(conversationId)).thenReturn(null);

        conversationController.updateParticipants(token, conversationId, updateParticipantsDto);

        verify(updateParticipantsDto).validate();
        verify(conversationService).getConversation(conversationId);
        verifyNoMoreInteractions(conversationService, updateParticipantsDto, participantService, objectFactory);
    }

    @Test
    public void testCountConversations() {
        String token = "token";
        Long participantId = 1L;
        long conversationsCount = 3L;
        boolean isNew = false;

        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(conversationService.countUpdatedConversationsPerParticipant(participant)).thenReturn(conversationsCount);
        when(objectFactory.getInstance(HttpHeaders.class)).thenReturn(new HttpHeaders());

        ResponseEntity result = conversationController.countConversations(token, participantId);

        verify(authHelper).authenticate(token);
        verify(participantService).getParticipant(participantId);
        verify(conversationService).countUpdatedConversationsPerParticipant(participant);
        verify(objectFactory).getInstance(HttpHeaders.class);
        verifyNoMoreInteractions(participantService, participant, authHelper, objectFactory);

        assertTrue("3L returned as a response as a new conversations count", result.getStatusCodeValue() == 200);
    }

    @Test(expected = BadRequestException.class)
    public void testCountNewConversationsForMissingParticipant() {
        String token = "token";
        Long participantId = 1L;

        when(participantService.getParticipant(participantId)).thenReturn(null);

        conversationController.countConversations(token, participantId);

        verify(authHelper).authenticate(token);
        verify(participantService).getParticipant(participantId);
        verifyNoMoreInteractions(participantService, participant, authHelper);
    }
}
