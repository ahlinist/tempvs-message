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
import club.tempvs.message.util.LocaleHelper;
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

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int MAX_PAGE_SIZE = 40;

    private ConversationController conversationController;
    private String token = "token";
    private String lang = "en";
    private Locale locale = Locale.ENGLISH;

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
    private AddParticipantsDto addParticipantsDto;
    @Mock
    private ParticipantDto authorDto;
    @Mock
    private ParticipantDto receiverDto;
    @Mock
    private ParticipantDto participantDto;
    @Mock
    private LocaleHelper localeHelper;
    @Mock
    private UpdateConversationNameDto updateConversationNameDto;
    @Mock
    private ReadMessagesDto readMessagesDto;

    @Before
    public void setup() {
        conversationController = new ConversationController(objectFactory, conversationService, participantService,
                messageService, authHelper, localeHelper);
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
        String text = "text";
        String name = "name";
        String timeZone = "UTC";
        Set<ParticipantDto> receiverDtos = new HashSet<>(Arrays.asList(receiverDto, participantDto));
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver, participant));
        List<Message> messages = Arrays.asList(message, message, message);

        when(localeHelper.getLocale(lang)).thenReturn(locale);
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
        when(messageService.getMessagesFromConversation(conversation, locale, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, author, timeZone, locale)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.createConversation(token, lang, timeZone, createConversationDto);

        verify(localeHelper).getLocale(lang);
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
        verify(messageService).getMessagesFromConversation(conversation, locale, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, author, timeZone, locale);
        verifyNoMoreInteractions(authorDto, message, receiverDto, participantDto,
                createConversationDto, participantService, messageService, conversationService, objectFactory);

        assertEquals("Result is a getConversationDto", result, getConversationDto);
    }

    @Test
    public void testCreateConversationForExistentDialogue() {
        Long authorId = 1L;
        Long receiverId = 2L;
        String text = "text";
        String name = "name";
        String timeZone = "UTC";
        Set<ParticipantDto> receiverDtos = new HashSet<>(Arrays.asList(receiverDto));
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver));
        List<Message> messages = Arrays.asList(message, message, message);

        when(localeHelper.getLocale(lang)).thenReturn(locale);
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
        when(messageService.getMessagesFromConversation(conversation, locale, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, author, timeZone, locale)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.createConversation(token, lang, timeZone, createConversationDto);

        verify(localeHelper).getLocale(lang);
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
        verify(messageService).getMessagesFromConversation(conversation, locale, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, author, timeZone, locale);
        verifyNoMoreInteractions(authorDto, message, receiverDto,
                createConversationDto, participantService, messageService, conversationService, objectFactory);

        assertEquals("Result is a conversation", result, getConversationDto);
    }

    @Test(expected = BadRequestException.class)
    public void testCreateConversationWith1Participant() {
        Long authorId = 1L;
        String text = "text";
        String name = "name";
        String timeZone = "UTC";
        Set<ParticipantDto> receiverDtos = new HashSet<>();
        Set<Participant> receivers = new HashSet<>();

        when(createConversationDto.getAuthor()).thenReturn(authorDto);
        when(authorDto.getId()).thenReturn(authorId);
        when(createConversationDto.getReceivers()).thenReturn(receiverDtos);
        when(participantService.getParticipant(authorId)).thenReturn(author);
        when(createConversationDto.getText()).thenReturn(text);
        when(createConversationDto.getName()).thenReturn(name);
        when(messageService.createMessage(author, receivers, text)).thenReturn(message);

        conversationController.createConversation(token, lang, timeZone, createConversationDto);

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
        long id = 1L;
        int page = 0;
        int size = 40;
        Long callerId = 5L;
        String timeZone = "UTC";
        List<Message> messages = Arrays.asList(message, message, message);
        Set<Participant> participants = new HashSet<>(Arrays.asList(participant, receiver));

        when(localeHelper.getLocale(lang)).thenReturn(locale);
        when(participantService.getParticipant(callerId)).thenReturn(participant);
        when(conversationService.getConversation(id)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);
        when(messageService.getMessagesFromConversation(conversation, locale, page, size)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, participant, timeZone, locale)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.getConversation(token, lang, timeZone, id, page, size, callerId);

        verify(localeHelper).getLocale(lang);
        verify(participantService).getParticipant(callerId);
        verify(conversationService).getConversation(id);
        verify(conversation).getParticipants();
        verify(messageService).getMessagesFromConversation(conversation, locale, page, size);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, participant, timeZone, locale);
        verifyNoMoreInteractions(message, conversation,
                participantService, conversationService, messageService, objectFactory, getConversationDto);

        assertEquals("Result is a conversation", result, getConversationDto);
    }

    @Test(expected = BadRequestException.class)
    public void testGetConversationForLargeAmountOfDataPerRequest() {
        long id = 1L;
        int page = 0;
        int size = 21;
        Long callerId = 5L;
        String timeZone = "UTC";

        conversationController.getConversation(token, lang, timeZone, id, page, size, callerId);

        verifyNoMoreInteractions(message, conversation, conversationService, messageService, objectFactory, getConversationDto);
    }

    @Test(expected = ForbiddenException.class)
    public void testGetConversationWithWrongCaller() {
        long id = 1L;
        int page = 0;
        int size = 40;
        Long callerId = 5L;
        String timeZone = "UTC";
        Set<Participant> participants = new HashSet<>(Arrays.asList(author, receiver));

        when(participantService.getParticipant(callerId)).thenReturn(participant);
        when(conversationService.getConversation(id)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);

        conversationController.getConversation(token, lang, timeZone, id, page, size, callerId);

        verify(participantService).getParticipant(callerId);
        verify(conversationService).getConversation(id);
        verify(conversation).getParticipants();
        verifyNoMoreInteractions(message, conversation,
                participantService, conversationService, messageService, objectFactory, getConversationDto);
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

        when(localeHelper.getLocale(lang)).thenReturn(locale);
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(conversationService.getConversationsByParticipant(participant, locale, page, size)).thenReturn(conversations);
        when(objectFactory.getInstance(GetConversationsDto.class, conversations, participant, timeZone, locale)).thenReturn(getConversationsDto);
        when(objectFactory.getInstance(HttpHeaders.class)).thenReturn(new HttpHeaders());
        when(getConversationsDto.getConversations()).thenReturn(conversationDtoBeans);

        ResponseEntity result = conversationController.getConversationsByParticipant(token, lang, timeZone, participantId, page, size);

        verify(localeHelper).getLocale(lang);
        verify(participantService).getParticipant(participantId);
        verify(conversationService).getConversationsByParticipant(participant, locale, page, size);
        verify(objectFactory).getInstance(GetConversationsDto.class, conversations, participant, timeZone, locale);
        verify(objectFactory).getInstance(HttpHeaders.class);
        verify(getConversationsDto).getConversations();
        verifyNoMoreInteractions(localeHelper, participantService, conversationService, objectFactory, getConversationsDto);

        assertEquals("GetCoversationsDto is returned as a result", getConversationsDto, result.getBody());
    }

    @Test(expected = BadRequestException.class)
    public void testGetConversationsByParticipantForLargeAmountOfDataBeingRetrieved() {
        Long participantId = 1L;
        int page = 0;
        int size = 200;
        String timeZone = "UTC";

        conversationController.getConversationsByParticipant(token, lang, timeZone, participantId, page, size);

        verifyNoMoreInteractions(participantService, conversationService, objectFactory);
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

        when(localeHelper.getLocale(lang)).thenReturn(locale);
        when(addMessageDto.getAuthor()).thenReturn(authorDto);
        when(authorDto.getId()).thenReturn(authorId);
        when(addMessageDto.getText()).thenReturn(text);
        when(participantService.getParticipant(authorId)).thenReturn(author);
        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);
        when(messageService.createMessage(author, participants, text)).thenReturn(message);
        when(conversationService.addMessage(conversation, message)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, locale, page, size)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, author, timeZone, locale)).thenReturn(getConversationDto);

        ResponseEntity result = conversationController.addMessage(token, lang, timeZone, conversationId, addMessageDto);

        verify(localeHelper).getLocale(lang);
        verify(addMessageDto).validate();
        verify(addMessageDto).getAuthor();
        verify(authorDto).getId();
        verify(addMessageDto).getText();
        verify(participantService).getParticipant(authorId);
        verify(conversationService).getConversation(conversationId);
        verify(conversation).getParticipants();
        verify(messageService).createMessage(author, participants, text);
        verify(conversationService).addMessage(conversation, message);
        verify(messageService).getMessagesFromConversation(conversation, locale, page, size);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, author, timeZone, locale);
        verifyNoMoreInteractions(authorDto,
                addMessageDto, participantService, conversationService, conversation, messageService, objectFactory);

        assertTrue("Status code 200 is returned", result.getStatusCodeValue() == 200);
        assertTrue("GetConversationDto object is returned as a body", result.getBody().equals(getConversationDto));
    }

    @Test
    public void testAddMessageForMissingConversation() {
        Long authorId = 1L;
        Long conversationId = 2L;
        Set<Participant> participants = new HashSet<>();
        participants.add(receiver);
        String text = "new message text";
        String timeZone = "UTC";

        when(addMessageDto.getAuthor()).thenReturn(authorDto);
        when(authorDto.getId()).thenReturn(authorId);
        when(addMessageDto.getText()).thenReturn(text);
        when(conversationService.getConversation(conversationId)).thenReturn(null);

        ResponseEntity result = conversationController.addMessage(token, lang, timeZone, conversationId, addMessageDto);

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
    public void testAddParticipant() {
        Long conversationId = 1L;
        Long initiatorId = 2L;
        Long subjectId = 3L;
        int page = 0;
        int max = 40;
        String timeZone = "UTC";
        List<Message> messages = Arrays.asList(message, message);
        List<ParticipantDto> receiverDtos = Arrays.asList(receiverDto);
        List<Participant> receivers = Arrays.asList(receiver);

        when(localeHelper.getLocale(lang)).thenReturn(locale);
        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(addParticipantsDto.getInitiator()).thenReturn(authorDto);
        when(authorDto.getId()).thenReturn(initiatorId);
        when(addParticipantsDto.getSubjects()).thenReturn(receiverDtos);
        when(receiverDto.getId()).thenReturn(subjectId);
        when(participantService.getParticipant(initiatorId)).thenReturn(author);
        when(participantService.getParticipant(subjectId)).thenReturn(receiver);
        when(conversationService.addParticipants(conversation, author, receivers)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, locale, page, max)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, author, timeZone, locale)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.addParticipants(token, lang, timeZone, conversationId, addParticipantsDto);

        verify(localeHelper).getLocale(lang);
        verify(addParticipantsDto).validate();
        verify(conversationService).getConversation(conversationId);
        verify(addParticipantsDto).getInitiator();
        verify(authorDto).getId();
        verify(addParticipantsDto).getSubjects();
        verify(receiverDto).getId();
        verify(participantService).getParticipant(initiatorId);
        verify(participantService).getParticipant(subjectId);
        verify(conversationService).addParticipants(conversation, author, receivers);
        verify(messageService).getMessagesFromConversation(conversation, locale, page, max);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, author, timeZone, locale);
        verifyNoMoreInteractions(authorDto,
                receiverDto, conversationService, addParticipantsDto, participantService, objectFactory);

        assertEquals("GetConversationDto is returned as a result", getConversationDto, result);
    }

    @Test
    public void testRemoveParticipant() {
        Long conversationId = 1L;
        Long initiatorId = 2L;
        Long subjectId = 3L;
        String timeZone = "UTC";
        int page = 0;
        int max = 40;
        List<Message> messages = Arrays.asList(message, message);

        when(localeHelper.getLocale(lang)).thenReturn(locale);
        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(participantService.getParticipant(initiatorId)).thenReturn(author);
        when(participantService.getParticipant(subjectId)).thenReturn(receiver);
        when(conversationService.removeParticipant(conversation, author, receiver)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, locale, page, max)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, author, timeZone, locale)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.removeParticipant(token, lang, timeZone, conversationId, subjectId, initiatorId);

        verify(localeHelper).getLocale(lang);
        verify(conversationService).getConversation(conversationId);
        verify(participantService).getParticipant(initiatorId);
        verify(participantService).getParticipant(subjectId);
        verify(conversationService).removeParticipant(conversation, author, receiver);
        verify(messageService).getMessagesFromConversation(conversation, locale, page, max);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, author, timeZone, locale);
        verifyNoMoreInteractions(authorDto, receiverDto, conversationService, participantService, objectFactory);

        assertEquals("GetConversationDto is returned as a result", getConversationDto, result);
    }

    @Test(expected = NotFoundException.class)
    public void testAddParticipantForNonExistentConversation() {
        Long conversationId = 1L;
        String timeZone = "UTC";

        when(conversationService.getConversation(conversationId)).thenReturn(null);

        conversationController.addParticipants(token, lang, timeZone, conversationId, addParticipantsDto);

        verify(addParticipantsDto).validate();
        verify(conversationService).getConversation(conversationId);
        verifyNoMoreInteractions(conversationService, addParticipantsDto, participantService, objectFactory);
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveParticipantForNonExistentConversation() {
        Long conversationId = 1L;
        Long subjectId = 2L;
        Long initiatorId = 1L;
        String timeZone = "UTC";

        when(conversationService.getConversation(conversationId)).thenReturn(null);

        conversationController.removeParticipant(token, lang, timeZone, conversationId, subjectId, initiatorId);

        verify(addParticipantsDto).validate();
        verify(conversationService).getConversation(conversationId);
        verifyNoMoreInteractions(conversationService, addParticipantsDto, participantService, objectFactory);
    }

    @Test
    public void testCountConversations() {
        Long participantId = 1L;
        long conversationsCount = 3L;

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
        Long participantId = 1L;

        when(participantService.getParticipant(participantId)).thenReturn(null);

        conversationController.countConversations(token, participantId);

        verify(authHelper).authenticate(token);
        verify(participantService).getParticipant(participantId);
        verifyNoMoreInteractions(participantService, participant, authHelper);
    }

    @Test
    public void testUpdateConversationName() {
        Long conversationId = 1L;
        Long participantId = 3L;
        String conversationName = "name";
        String timeZone = "UTC";
        List<Message> messages = Arrays.asList(message);

        when(localeHelper.getLocale(lang)).thenReturn(locale);
        when(updateConversationNameDto.getName()).thenReturn(conversationName);
        when(updateConversationNameDto.getInitiator()).thenReturn(participantDto);
        when(participantDto.getId()).thenReturn(participantId);
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(conversationService.updateName(conversation, participant, conversationName)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, locale, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, participant, timeZone, locale)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.updateConversationName(token, lang, timeZone, conversationId, updateConversationNameDto);

        verify(authHelper).authenticate(token);
        verify(updateConversationNameDto).validate();
        verify(localeHelper).getLocale(lang);
        verify(updateConversationNameDto).getName();
        verify(updateConversationNameDto).getInitiator();
        verify(participantDto).getId();
        verify(participantService).getParticipant(participantId);
        verify(conversationService).getConversation(conversationId);
        verify(conversationService).updateName(conversation, participant, conversationName);
        verify(messageService).getMessagesFromConversation(conversation, locale, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, participant, timeZone, locale);
        verifyNoMoreInteractions(authHelper, localeHelper, updateConversationNameDto, participantService,
                conversationService, messageService, objectFactory);

        assertEquals("GetConversationDto is returned as a result", getConversationDto, result);
    }

    @Test
    public void testReadMessages() {
        Long conversationId = 1L;
        Long participantId = 4L;
        List<Long> messageIds = Arrays.asList(2L, 3L);
        List<Message> messages = Arrays.asList(message, message);

        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(readMessagesDto.getParticipant()).thenReturn(participantDto);
        when(participantDto.getId()).thenReturn(participantId);
        when(readMessagesDto.getMessageIds()).thenReturn(messageIds);
        when(messageService.findMessagesByIds(messageIds)).thenReturn(messages);
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(messageService.markAsRead(conversation, participant, messages)).thenReturn(messages);

        ResponseEntity result = conversationController.readMessages(conversationId, token, readMessagesDto);

        verify(authHelper).authenticate(token);
        verify(readMessagesDto).validate();
        verify(conversationService).getConversation(conversationId);
        verify(readMessagesDto).getParticipant();
        verify(participantDto).getId();
        verify(readMessagesDto).getMessageIds();
        verify(messageService).findMessagesByIds(messageIds);
        verify(participantService).getParticipant(participantId);
        verify(messageService).markAsRead(conversation, participant, messages);
        verifyNoMoreInteractions(conversationService, authHelper, readMessagesDto);

        assertEquals("Response is ok", result, ResponseEntity.ok().build());
    }

    @Test(expected = BadRequestException.class)
    public void testReadMessagesForMissingConversation() {
        Long conversationId = 1L;

        when(conversationService.getConversation(conversationId)).thenReturn(null);

        conversationController.readMessages(conversationId, token, readMessagesDto);
    }

    @Test(expected = BadRequestException.class)
    public void testReadMessagesForMissingParticipant() {
        Long conversationId = 1L;
        Long participantId = 4L;

        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(readMessagesDto.getParticipant()).thenReturn(participantDto);
        when(participantDto.getId()).thenReturn(participantId);
        when(participantService.getParticipant(participantId)).thenReturn(null);

        conversationController.readMessages(conversationId, token, readMessagesDto);
    }

    @Test(expected = BadRequestException.class)
    public void testReadMessagesForMissingMessages() {
        Long conversationId = 1L;
        Long participantId = 4L;
        List<Long> messageIds = Arrays.asList(2L, 3L);
        List<Message> messages = Arrays.asList(message, null);

        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(readMessagesDto.getParticipant()).thenReturn(participantDto);
        when(participantDto.getId()).thenReturn(participantId);
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(readMessagesDto.getMessageIds()).thenReturn(messageIds);
        when(messageService.findMessagesByIds(messageIds)).thenReturn(messages);

        conversationController.readMessages(conversationId, token, readMessagesDto);
    }
}
