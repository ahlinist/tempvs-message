package club.tempvs.message.controller;

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
import org.springframework.context.i18n.LocaleContextHolder;
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
    private Participant author, receiver, participant;
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
    private ParticipantDto authorDto, receiverDto, participantDto;
    @Mock
    private LocaleHelper localeHelper;
    @Mock
    private UpdateConversationNameDto updateConversationNameDto;
    @Mock
    private ReadMessagesDto readMessagesDto;
    @Mock
    private ErrorsDto errorsDto;
    @Mock
    private UserInfoDto userInfoDto;

    @Before
    public void setup() {
        LocaleContextHolder.setLocale(locale);
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
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(receiverId, participantId));
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver, participant));
        List<Message> messages = Arrays.asList(message, message, message);

        when(localeHelper.getLocale(lang)).thenReturn(locale);
        when(userInfoDto.getProfileId()).thenReturn(authorId);
        when(createConversationDto.getReceivers()).thenReturn(receiverIds);
        when(participantService.getParticipant(authorId)).thenReturn(author);
        when(participantService.getParticipants(receiverIds)).thenReturn(receivers);
        when(createConversationDto.getText()).thenReturn(text);
        when(createConversationDto.getName()).thenReturn(name);
        when(messageService.createMessage(author, receivers, text, false, null, null)).thenReturn(message);
        when(conversationService.createConversation(author, receivers, name, message)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, author, timeZone)).thenReturn(getConversationDto);
        when(objectFactory.getInstance(HttpHeaders.class)).thenReturn(new HttpHeaders());

        ResponseEntity result = conversationController.createConversation(userInfoDto, token, lang, timeZone, createConversationDto);

        verify(authHelper).authenticate(token);
        verify(localeHelper).getLocale(lang);
        verify(userInfoDto).getProfileId();
        verify(createConversationDto).getReceivers();
        verify(participantService).getParticipant(authorId);
        verify(participantService).getParticipants(receiverIds);
        verify(createConversationDto).getText();
        verify(createConversationDto).getName();
        verify(messageService).createMessage(author, receivers, text, false, null, null);
        verify(conversationService).createConversation(author, receivers, name, message);
        verify(messageService).getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, author, timeZone);
        verify(objectFactory).getInstance(HttpHeaders.class);
        verifyNoMoreInteractions(authHelper, authorDto, message, receiverDto, participantDto, userInfoDto,
                createConversationDto, participantService, messageService, conversationService, objectFactory);

        assertEquals("Result is a getConversationDto", getConversationDto, result.getBody());
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateConversationMissingAuthor() {
        Long authorId = 1L;
        String timeZone = "UTC";

        when(userInfoDto.getProfileId()).thenReturn(authorId);
        when(participantService.getParticipant(authorId)).thenReturn(null);

        conversationController.createConversation(userInfoDto, token, lang, timeZone, createConversationDto);
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
        when(userInfoDto.getProfileId()).thenReturn(callerId);
        when(participantService.getParticipant(callerId)).thenReturn(participant);
        when(conversationService.getConversation(id)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);
        when(messageService.getMessagesFromConversation(conversation, page, size)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, participant, timeZone)).thenReturn(getConversationDto);
        when(objectFactory.getInstance(HttpHeaders.class)).thenReturn(new HttpHeaders());

        ResponseEntity result = conversationController.getConversation(userInfoDto, token, lang, timeZone, id, page, size);

        verify(authHelper).authenticate(token);
        verify(localeHelper).getLocale(lang);
        verify(userInfoDto).getProfileId();
        verify(participantService).getParticipant(callerId);
        verify(conversationService).getConversation(id);
        verify(conversation).getParticipants();
        verify(messageService).getMessagesFromConversation(conversation, page, size);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, participant, timeZone);
        verify(objectFactory).getInstance(HttpHeaders.class);
        verifyNoMoreInteractions(authHelper, message, conversation, userInfoDto,
                participantService, conversationService, messageService, objectFactory, getConversationDto);

        assertEquals("Result is a conversation", getConversationDto, result.getBody());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetConversationForLargeAmountOfDataPerRequest() {
        long id = 1L;
        int page = 0;
        int size = 21;
        String timeZone = "UTC";

        conversationController.getConversation(userInfoDto, token, lang, timeZone, id, page, size);
    }

    @Test(expected = ForbiddenException.class)
    public void testGetConversationWithWrongCaller() {
        long id = 1L;
        int page = 0;
        int size = 40;
        Long callerId = 5L;
        String timeZone = "UTC";
        Set<Participant> participants = new HashSet<>(Arrays.asList(author, receiver));

        when(userInfoDto.getProfileId()).thenReturn(callerId);
        when(participantService.getParticipant(callerId)).thenReturn(participant);
        when(conversationService.getConversation(id)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);

        conversationController.getConversation(userInfoDto, token, lang, timeZone, id, page, size);

        verify(authHelper).authenticate(token);
        verify(userInfoDto).getProfileId();
        verify(participantService).getParticipant(callerId);
        verify(conversationService).getConversation(id);
        verify(conversation).getParticipants();
        verifyNoMoreInteractions(authHelper, message, conversation, userInfoDto,
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
        when(userInfoDto.getProfileId()).thenReturn(participantId);
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(conversationService.getConversationsByParticipant(participant, page, size)).thenReturn(conversations);
        when(objectFactory.getInstance(GetConversationsDto.class, conversations, participant, timeZone, locale)).thenReturn(getConversationsDto);
        when(objectFactory.getInstance(HttpHeaders.class)).thenReturn(new HttpHeaders());
        when(getConversationsDto.getConversations()).thenReturn(conversationDtoBeans);

        ResponseEntity result = conversationController.getConversationsByParticipant(userInfoDto, token, lang, timeZone, page, size);

        verify(authHelper).authenticate(token);
        verify(localeHelper).getLocale(lang);
        verify(userInfoDto).getProfileId();
        verify(participantService).getParticipant(participantId);
        verify(conversationService).getConversationsByParticipant(participant, page, size);
        verify(objectFactory).getInstance(GetConversationsDto.class, conversations, participant, timeZone, locale);
        verify(objectFactory).getInstance(HttpHeaders.class);
        verify(getConversationsDto).getConversations();
        verifyNoMoreInteractions(authHelper, localeHelper,
                userInfoDto, participantService, conversationService, objectFactory, getConversationsDto);

        assertEquals("GetCoversationsDto is returned as a result", getConversationsDto, result.getBody());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetConversationsByParticipantForLargeAmountOfDataBeingRetrieved() {
        int page = 0;
        int size = 200;
        String timeZone = "UTC";

        conversationController.getConversationsByParticipant(userInfoDto, token, lang, timeZone, page, size);
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
        when(addMessageDto.getText()).thenReturn(text);
        when(userInfoDto.getProfileId()).thenReturn(authorId);
        when(participantService.getParticipant(authorId)).thenReturn(author);
        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);
        when(messageService.createMessage(author, participants, text, false, null, null)).thenReturn(message);
        when(conversationService.addMessage(conversation, message)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, page, size)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, author, timeZone)).thenReturn(getConversationDto);
        when(objectFactory.getInstance(HttpHeaders.class)).thenReturn(new HttpHeaders());

        ResponseEntity result = conversationController.addMessage(userInfoDto, token, lang, timeZone, conversationId, addMessageDto);

        verify(authHelper).authenticate(token);
        verify(localeHelper).getLocale(lang);
        verify(localeHelper).getLocale(lang);
        verify(userInfoDto).getProfileId();
        verify(addMessageDto).getText();
        verify(participantService).getParticipant(authorId);
        verify(conversationService).getConversation(conversationId);
        verify(conversation).getParticipants();
        verify(messageService).createMessage(author, participants, text, false, null, null);
        verify(conversationService).addMessage(conversation, message);
        verify(messageService).getMessagesFromConversation(conversation, page, size);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, author, timeZone);
        verify(objectFactory).getInstance(HttpHeaders.class);
        verifyNoMoreInteractions(authHelper, localeHelper, authorDto, userInfoDto,
                addMessageDto, participantService, conversationService, conversation, messageService, objectFactory);

        assertTrue("Status code 200 is returned", result.getStatusCodeValue() == 200);
        assertTrue("GetConversationDto object is returned as a body", result.getBody().equals(getConversationDto));
    }

    @Test
    public void testAddMessageForMissingConversation() {
        Long conversationId = 2L;
        Set<Participant> participants = new HashSet<>();
        participants.add(receiver);
        String text = "new message text";
        String timeZone = "UTC";

        when(addMessageDto.getText()).thenReturn(text);
        when(conversationService.getConversation(conversationId)).thenReturn(null);

        ResponseEntity result = conversationController.addMessage(userInfoDto, token, lang, timeZone, conversationId, addMessageDto);

        verify(authHelper).authenticate(token);
        verify(localeHelper).getLocale(lang);
        verify(addMessageDto).getText();
        verify(conversationService).getConversation(conversationId);
        verifyNoMoreInteractions(authHelper, localeHelper, authorDto, userInfoDto,
                addMessageDto, participantService, conversationService, conversation, messageService, objectFactory);

        assertTrue("Status code 404 is returned", result.getStatusCodeValue() == 404);
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

        when(localeHelper.getLocale(lang)).thenReturn(locale);
        when(userInfoDto.getProfileId()).thenReturn(initiatorId);
        when(addParticipantsDto.getParticipants()).thenReturn(receiverIds);
        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(participantService.getParticipant(initiatorId)).thenReturn(author);
        when(participantService.getParticipants(receiverIds)).thenReturn(receivers);
        when(conversation.getParticipants()).thenReturn(participants);
        when(conversationService.addParticipants(conversation, author, receivers)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, page, max)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, author, timeZone)).thenReturn(getConversationDto);
        when(objectFactory.getInstance(HttpHeaders.class)).thenReturn(new HttpHeaders());

        ResponseEntity result = conversationController.addParticipants(userInfoDto, token, lang, timeZone, conversationId, addParticipantsDto);

        verify(authHelper).authenticate(token);
        verify(localeHelper).getLocale(lang);
        verify(userInfoDto).getProfileId();
        verify(conversationService).getConversation(conversationId);
        verify(addParticipantsDto).getParticipants();
        verify(participantService).getParticipant(initiatorId);
        verify(participantService).getParticipants(receiverIds);
        verify(conversation).getParticipants();
        verify(conversationService).addParticipants(conversation, author, receivers);
        verify(messageService).getMessagesFromConversation(conversation, page, max);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, author, timeZone);
        verify(objectFactory).getInstance(HttpHeaders.class);
        verifyNoMoreInteractions(authHelper, authorDto, receiverDto, userInfoDto, conversationService,
                addParticipantsDto, participantService, objectFactory, errorsDto, conversation, receiver, author);

        assertEquals("GetConversationDto is returned as a result", getConversationDto, result.getBody());
    }

    @Test(expected = NotFoundException.class)
    public void testAddParticipantToNonExistentConversation() {
        Long conversationId = 1L;
        String timeZone = "UTC";

        when(localeHelper.getLocale(lang)).thenReturn(locale);
        when(conversationService.getConversation(conversationId)).thenReturn(null);

        conversationController.addParticipants(userInfoDto, token, lang, timeZone, conversationId, addParticipantsDto);
    }

    @Test(expected = IllegalStateException.class)
    public void testAddParticipantForNonExistentInitiator() {
        Long conversationId = 1L;
        Long initiatorId = 2L;
        String timeZone = "UTC";

        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(userInfoDto.getProfileId()).thenReturn(initiatorId);
        when(participantService.getParticipant(initiatorId)).thenReturn(null);

        conversationController.addParticipants(userInfoDto, token, lang, timeZone, conversationId, addParticipantsDto);
    }

    @Test(expected = IllegalStateException.class)
    public void testAddParticipantForNonExistentSubject() {
        Long conversationId = 1L;
        Long initiatorId = 2L;
        Long subjectId = 3L;
        Set<Long> subjectIds = new HashSet<>(Arrays.asList(subjectId));
        String timeZone = "UTC";

        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(userInfoDto.getProfileId()).thenReturn(initiatorId);
        when(participantService.getParticipant(initiatorId)).thenReturn(author);
        when(addParticipantsDto.getParticipants()).thenReturn(subjectIds);
        when(participantService.getParticipants(subjectIds)).thenReturn(null);

        conversationController.addParticipants(userInfoDto, token, lang, timeZone, conversationId, addParticipantsDto);
    }

    @Test(expected = IllegalStateException.class)
    public void testAddParticipantsForExistingMember() {
        Long conversationId = 1L;
        Long initiatorId = 2L;
        Long subjectId = 3L;
        Set<Long> subjectIds = new HashSet<>(Arrays.asList(subjectId));
        Set<Participant> participants = new HashSet<>(Arrays.asList(receiver));
        String timeZone = "UTC";

        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(userInfoDto.getProfileId()).thenReturn(initiatorId);
        when(participantService.getParticipant(initiatorId)).thenReturn(author);
        when(addParticipantsDto.getParticipants()).thenReturn(subjectIds);
        when(participantService.getParticipants(subjectIds)).thenReturn(participants);
        when(conversation.getParticipants()).thenReturn(participants);

        conversationController.addParticipants(userInfoDto, token, lang, timeZone, conversationId, addParticipantsDto);
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
        when(userInfoDto.getProfileId()).thenReturn(initiatorId);
        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(participantService.getParticipant(initiatorId)).thenReturn(author);
        when(participantService.getParticipant(subjectId)).thenReturn(receiver);
        when(conversationService.removeParticipant(conversation, author, receiver)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, page, max)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, author, timeZone)).thenReturn(getConversationDto);
        when(objectFactory.getInstance(HttpHeaders.class)).thenReturn(new HttpHeaders());

        ResponseEntity result = conversationController.removeParticipant(userInfoDto, token, lang, timeZone, conversationId, subjectId);

        verify(authHelper).authenticate(token);
        verify(localeHelper).getLocale(lang);
        verify(userInfoDto).getProfileId();
        verify(conversationService).getConversation(conversationId);
        verify(participantService).getParticipant(initiatorId);
        verify(participantService).getParticipant(subjectId);
        verify(conversationService).removeParticipant(conversation, author, receiver);
        verify(messageService).getMessagesFromConversation(conversation, page, max);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, author, timeZone);
        verify(objectFactory).getInstance(HttpHeaders.class);
        verifyNoMoreInteractions(authHelper, userInfoDto, authorDto, receiverDto, localeHelper, messageService,
                message, author, receiver, conversationService, participantService, objectFactory, conversation);

        assertEquals("GetConversationDto is returned as a result", getConversationDto, result.getBody());
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveParticipantForNonExistentConversation() {
        Long conversationId = 1L;
        Long subjectId = 2L;
        String timeZone = "UTC";

        when(conversationService.getConversation(conversationId)).thenReturn(null);

        conversationController.removeParticipant(userInfoDto, token, lang, timeZone, conversationId, subjectId);
    }

    @Test(expected = IllegalStateException.class)
    public void testRemoveParticipantForNonExistentInitiator() {
        Long conversationId = 1L;
        Long subjectId = 2L;
        Long initiatorId = 1L;
        String timeZone = "UTC";

        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(userInfoDto.getProfileId()).thenReturn(initiatorId);
        when(participantService.getParticipant(initiatorId)).thenReturn(null);

        conversationController.removeParticipant(userInfoDto, token, lang, timeZone, conversationId, subjectId);
    }

    @Test(expected = IllegalStateException.class)
    public void testRemoveParticipantForNonExistentSubject() {
        Long conversationId = 1L;
        Long subjectId = 2L;
        Long initiatorId = 1L;
        String timeZone = "UTC";

        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(userInfoDto.getProfileId()).thenReturn(initiatorId);
        when(participantService.getParticipant(initiatorId)).thenReturn(author);
        when(participantService.getParticipant(subjectId)).thenReturn(null);

        conversationController.removeParticipant(userInfoDto, token, lang, timeZone, conversationId, subjectId);
    }

    @Test
    public void testCountConversations() {
        Long participantId = 1L;
        long conversationsCount = 3L;

        when(userInfoDto.getProfileId()).thenReturn(participantId);
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(conversationService.countUpdatedConversationsPerParticipant(participant)).thenReturn(conversationsCount);
        when(objectFactory.getInstance(HttpHeaders.class)).thenReturn(new HttpHeaders());

        ResponseEntity result = conversationController.countConversations(userInfoDto, token);

        verify(authHelper).authenticate(token);
        verify(userInfoDto).getProfileId();
        verify(participantService).getParticipant(participantId);
        verify(conversationService).countUpdatedConversationsPerParticipant(participant);
        verify(objectFactory).getInstance(HttpHeaders.class);
        verifyNoMoreInteractions(userInfoDto, participantService, participant, authHelper, objectFactory);

        assertTrue("3L returned as a response as a new conversations count", result.getStatusCodeValue() == 200);
    }

    @Test(expected = IllegalStateException.class)
    public void testCountNewConversationsForMissingParticipant() {
        Long participantId = 1L;

        when(userInfoDto.getProfileId()).thenReturn(participantId);
        when(participantService.getParticipant(participantId)).thenReturn(null);

        conversationController.countConversations(userInfoDto, token);
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
        when(userInfoDto.getProfileId()).thenReturn(participantId);
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(conversationService.rename(conversation, participant, conversationName)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE)).thenReturn(messages);
        when(objectFactory.getInstance(GetConversationDto.class, conversation, messages, participant, timeZone)).thenReturn(getConversationDto);
        when(objectFactory.getInstance(HttpHeaders.class)).thenReturn(new HttpHeaders());

        ResponseEntity result = conversationController.renameConversation(userInfoDto, token, lang, timeZone, conversationId, updateConversationNameDto);

        verify(authHelper).authenticate(token);
        verify(localeHelper).getLocale(lang);
        verify(userInfoDto).getProfileId();
        verify(updateConversationNameDto).getName();
        verify(participantService).getParticipant(participantId);
        verify(conversationService).getConversation(conversationId);
        verify(conversationService).rename(conversation, participant, conversationName);
        verify(messageService).getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation, messages, participant, timeZone);
        verify(objectFactory).getInstance(HttpHeaders.class);
        verifyNoMoreInteractions(authHelper, localeHelper, updateConversationNameDto, participantService, userInfoDto,
                conversationService, messageService, objectFactory);

        assertEquals("GetConversationDto is returned as a result", getConversationDto, result.getBody());
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
        when(objectFactory.getInstance(HttpHeaders.class)).thenReturn(new HttpHeaders());

        ResponseEntity result = conversationController.readMessages(userInfoDto, token, conversationId, readMessagesDto);

        verify(authHelper).authenticate(token);
        verify(conversationService).getConversation(conversationId);
        verify(readMessagesDto).getMessages();
        verify(messageService).findMessagesByIds(messageIds);
        verify(userInfoDto).getProfileId();
        verify(participantService).getParticipant(participantId);
        verify(messageService).markAsRead(conversation, participant, messages);
        verify(objectFactory).getInstance(HttpHeaders.class);
        verifyNoMoreInteractions(conversationService, authHelper, readMessagesDto, objectFactory, messageService,
                userInfoDto, participantService, conversation, message, participant);

        assertEquals("Response is ok", result, ResponseEntity.ok().build());
    }

    @Test(expected = NotFoundException.class)
    public void testReadMessagesForMissingConversation() {
        Long conversationId = 1L;

        when(conversationService.getConversation(conversationId)).thenReturn(null);

        conversationController.readMessages(userInfoDto, token, conversationId, readMessagesDto);
    }

    @Test(expected = IllegalStateException.class)
    public void testReadMessagesForMissingParticipant() {
        Long conversationId = 1L;
        Long participantId = 4L;

        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(userInfoDto.getProfileId()).thenReturn(participantId);
        when(participantService.getParticipant(participantId)).thenReturn(null);

        conversationController.readMessages(userInfoDto, token, conversationId, readMessagesDto);
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

        conversationController.readMessages(userInfoDto, token, conversationId, readMessagesDto);
    }
}
