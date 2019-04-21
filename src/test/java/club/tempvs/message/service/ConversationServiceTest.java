package club.tempvs.message.service;

import static club.tempvs.message.domain.Conversation.Type.CONFERENCE;

import club.tempvs.message.api.ForbiddenException;
import club.tempvs.message.dao.ConversationRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.GetConversationDto;
import club.tempvs.message.dto.GetConversationsDto;
import club.tempvs.message.holder.UserHolder;
import club.tempvs.message.model.User;
import club.tempvs.message.service.impl.ConversationServiceImpl;
import club.tempvs.message.util.LocaleHelper;
import club.tempvs.message.util.ObjectFactory;
import static org.junit.Assert.*;

import club.tempvs.message.util.ValidationHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class ConversationServiceTest {

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int MAX_PAGE_SIZE = 40;
    private static final String CONVERSATION_RENAMED = "conversation.rename";
    private static final String CONVERSATION_NAME_DROPPED = "conversation.drop.name";

    private ConversationService conversationService;

    @Mock
    private Message message;
    @Mock
    private Conversation conversation;
    @Mock
    private Participant participant, author, receiver, oneMoreReceiver;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private MessageService messageService;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private LocaleHelper localeHelper;
    @Mock
    private ValidationHelper validationHelper;
    @Mock
    private ParticipantService participantService;
    @Mock
    private UserHolder userHolder;
    @Mock
    private User user;

    @Before
    public void setup() {
        this.conversationService = new ConversationServiceImpl(objectFactory,
                messageService, conversationRepository, localeHelper, validationHelper, participantService, userHolder);
    }

    @Test
    public void testCreateConversation() {
        Long authorId = 1L;
        String text = "text";
        String name = "name";
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(2L, 3L));
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver, participant));
        List<Message> messages = Arrays.asList(message, message, message);

        when(userHolder.getUser()).thenReturn(user);
        when(user.getProfileId()).thenReturn(authorId);
        when(user.getTimezone()).thenReturn("UTC");
        when(participantService.getParticipant(authorId)).thenReturn(author);
        when(participantService.getParticipants(receiverIds)).thenReturn(receivers);
        when(objectFactory.getInstance(Conversation.class)).thenReturn(conversation);
        when(messageService.createMessage(author, receivers, text, false, null, null)).thenReturn(message);
        when(message.getAuthor()).thenReturn(author);
        when(message.getCreatedDate()).thenReturn(Instant.now());
        when(messageService.getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE)).thenReturn(messages);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        GetConversationDto result = conversationService.createConversation(receiverIds, name, text);

        verify(participantService).getParticipant(authorId);
        verify(participantService).getParticipants(receiverIds);
        verify(objectFactory).getInstance(Conversation.class);
        verify(messageService).createMessage(author, receivers, text, false, null, null);
        verify(messageService).addMessage(conversation, message, author);
        verify(messageService).getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(participantService, messageService, conversationRepository);

        assertTrue("An instance of GetConversationDto is returned", result instanceof GetConversationDto);
    }

    @Test
    public void testGetConversation() {
        long conversationId = 1L;
        int page = 0;
        int size = 40;
        long participantId = 2L;
        Set<Participant> participants = new HashSet<>(Arrays.asList(participant, receiver));
        List<Message> messages = Arrays.asList(message, message, message);

        when(userHolder.getUser()).thenReturn(user);
        when(user.getProfileId()).thenReturn(participantId);
        when(user.getTimezone()).thenReturn("UTC");
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(conversation.getParticipants()).thenReturn(participants);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(message.getAuthor()).thenReturn(author);
        when(message.getCreatedDate()).thenReturn(Instant.now());
        when(messageService.getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE)).thenReturn(messages);

        GetConversationDto result = conversationService.getConversation(conversationId, page, size);

        verify(participantService).getParticipant(participantId);
        verify(conversationRepository).findById(conversationId);
        verify(messageService).getMessagesFromConversation(conversation, page, size);
        verifyNoMoreInteractions(conversationRepository, participantService, messageService);

        assertTrue("GetConversationDto is returned", result instanceof GetConversationDto);
    }

    @Test(expected = ForbiddenException.class)
    public void testGetConversationForWrongCaller() {
        long conversationId = 1L;
        int page = 0;
        int size = 40;
        long participantId = 2L;
        Set<Participant> participants = new HashSet<>(Arrays.asList(receiver, oneMoreReceiver));

        when(userHolder.getUser()).thenReturn(user);
        when(user.getProfileId()).thenReturn(participantId);
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(conversation.getParticipants()).thenReturn(participants);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        conversationService.getConversation(conversationId, page, size);
    }

    @Test
    public void testAddMessage() {
        long conversationId = 1l;
        String text = "text";
        long participantId = 2l;
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver));
        Set<Participant> participants = new HashSet<>(Arrays.asList(author, receiver));
        List<Message> messages = Arrays.asList(message, message, message);

        when(userHolder.getUser()).thenReturn(user);
        when(user.getProfileId()).thenReturn(participantId);
        when(user.getTimezone()).thenReturn("UTC");
        when(participantService.getParticipant(participantId)).thenReturn(author);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversation.getParticipants()).thenReturn(participants);
        when(messageService.createMessage(author, receivers, text, false, null, null)).thenReturn(message);
        when(messageService.addMessage(conversation, message, author)).thenReturn(conversation);
        when(conversationRepository.save(conversation)).thenReturn(conversation);
        when(message.getAuthor()).thenReturn(author);
        when(message.getCreatedDate()).thenReturn(Instant.now());
        when(messageService.getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE)).thenReturn(messages);

        GetConversationDto result = conversationService.addMessage(conversationId, text);

        verify(conversationRepository).findById(conversationId);
        verify(participantService).getParticipant(participantId);
        verify(messageService).createMessage(author, receivers, text, false, null, null);
        verify(messageService).addMessage(conversation, message, author);
        verify(conversationRepository).save(conversation);
        verify(messageService).getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        verifyNoMoreInteractions(messageService, participantService, conversationRepository);

        assertTrue("GetConversationDto is returned", result instanceof GetConversationDto);
    }

    @Test
    public void testGetConversationsByParticipant() {
        int page = 0;
        int size = 40;
        long participantId = 1l;
        List<Conversation> conversations = new ArrayList<>();
        conversations.add(conversation);
        Pageable pageable = PageRequest.of(page, size);
        List<Object[]> conversationsPerParticipant = new ArrayList<>();
        conversationsPerParticipant.add(new Object[]{conversation, 3L});

        when(userHolder.getUser()).thenReturn(user);
        when(user.getProfileId()).thenReturn(participantId);
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(conversationRepository.findConversationsPerParticipant(participant, pageable)).thenReturn(conversationsPerParticipant);
        when(conversation.getLastMessage()).thenReturn(message);
        when(conversation.getType()).thenReturn(CONFERENCE);
        when(message.getAuthor()).thenReturn(participant);
        when(message.getCreatedDate()).thenReturn(Instant.now());
        when(user.getTimezone()).thenReturn("UTC");
        when(localeHelper.translateMessageIfSystem(message)).thenReturn(message);

        GetConversationsDto result = conversationService.getConversationsAttended(page, size);

        verify(userHolder).getUser();
        verify(participantService).getParticipant(participantId);
        verify(conversationRepository).findConversationsPerParticipant(participant, pageable);
        verify(localeHelper).translateMessageIfSystem(message);
        verifyNoMoreInteractions(localeHelper, conversationRepository, userHolder, participantService);

        assertTrue("An GetConversationsDtoinstance is returned", result instanceof GetConversationsDto);
    }

    @Test
    public void testAddParticipantToConversationOf2() {
        long conversationId = 1l;
        long authorId = 2l;
        Set<Long> addedIds = new HashSet<>(Arrays.asList(3L));
        String text = "conversation.conference.created";
        Set<Participant> participantsToAdd = new LinkedHashSet<>(Arrays.asList(oneMoreReceiver));
        Set<Participant> initialParticipants = new LinkedHashSet<>(Arrays.asList(author, receiver));
        Set<Participant> receivers = new LinkedHashSet<>(Arrays.asList(receiver, oneMoreReceiver));
        List<Message> messages = Arrays.asList(message, message, message);

        when(userHolder.getUser()).thenReturn(user);
        when(user.getProfileId()).thenReturn(authorId);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(participantService.getParticipant(authorId)).thenReturn(author);
        when(participantService.getParticipants(addedIds)).thenReturn(participantsToAdd);
        when(conversation.getParticipants()).thenReturn(initialParticipants);
        when(conversation.getType()).thenReturn(Conversation.Type.DIALOGUE);
        when(objectFactory.getInstance(Conversation.class)).thenReturn(conversation);
        when(messageService.createMessage(author, receivers, text, true, null, null)).thenReturn(message);
        when(conversationRepository.save(conversation)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE)).thenReturn(messages);
        when(message.getAuthor()).thenReturn(participant);
        when(message.getCreatedDate()).thenReturn(Instant.now());
        when(user.getTimezone()).thenReturn("UTC");

        GetConversationDto result = conversationService.addParticipants(conversationId, addedIds);

        verify(conversationRepository).findById(conversationId);
        verify(participantService).getParticipant(authorId);
        verify(participantService).getParticipants(addedIds);
        verify(validationHelper).validateParticipantsAddition(author, participantsToAdd, initialParticipants);
        verify(validationHelper).validateConversationCreation(author, receivers, message);
        verify(messageService).createMessage(author, receivers, text, true, null, null);
        verify(objectFactory).getInstance(Conversation.class);
        verify(messageService).addMessage(conversation, message, author);
        verify(conversationRepository).save(conversation);
        verify(messageService).getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        verifyNoMoreInteractions(participantService, messageService, conversationRepository, validationHelper);

        assertTrue("GetConversationDto is returned", result instanceof GetConversationDto);
    }

    @Test
    public void testAddParticipantForConversationOf3() {
        long conversationId = 1l;
        long authorId = 2l;
        Set<Long> addedIds = new HashSet<>(Arrays.asList(2L, 3L));
        String text = "conversation.add.participant";
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> participantsToAdd = new LinkedHashSet<>(Arrays.asList(oneMoreReceiver));
        Set<Participant> initialParticipants = new LinkedHashSet<>(Arrays.asList(author, receiver, participant));
        Set<Participant> receivers = new LinkedHashSet<>(Arrays.asList(receiver, participant, oneMoreReceiver));
        List<Message> messages = Arrays.asList(message, message, message);

        when(userHolder.getUser()).thenReturn(user);
        when(user.getProfileId()).thenReturn(authorId);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(participantService.getParticipant(authorId)).thenReturn(author);
        when(participantService.getParticipants(addedIds)).thenReturn(participantsToAdd);
        when(conversation.getParticipants()).thenReturn(initialParticipants);
        when(conversation.getType()).thenReturn(Conversation.Type.CONFERENCE);
        when(messageService.createMessage(author, receivers, text, isSystem, null, oneMoreReceiver)).thenReturn(message);
        when(conversationRepository.save(conversation)).thenReturn(conversation);
        when(messageService.getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE)).thenReturn(messages);
        when(message.getAuthor()).thenReturn(participant);
        when(message.getCreatedDate()).thenReturn(Instant.now());
        when(user.getTimezone()).thenReturn("UTC");

        GetConversationDto result = conversationService.addParticipants(conversationId, addedIds);

        verify(conversationRepository).findById(conversationId);
        verify(participantService).getParticipant(authorId);
        verify(participantService).getParticipants(addedIds);
        verify(validationHelper).validateParticipantsAddition(author, participantsToAdd, initialParticipants);
        verify(messageService).createMessage(author, receivers, text, isSystem, null, oneMoreReceiver);
        verify(messageService).addMessage(conversation, message, author);
        verify(conversationRepository).save(conversation);
        verify(messageService).getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        verifyNoMoreInteractions(participantService, messageService, conversationRepository, validationHelper);

        assertTrue("GetConversationDto is returned", result instanceof GetConversationDto);
    }

    @Test
    public void testRemoveParticipant() {
        Long conversationId = 1L;
        Long initiatorId = 3L;
        Long subjectId = 2L;
        String text = "conversation.remove.participant";
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> participants = new HashSet<>(Arrays.asList(author, receiver, oneMoreReceiver, participant));
        Set<Participant> receivers = new HashSet<>(Arrays.asList(oneMoreReceiver, participant));
        int page = 0;
        int max = 40;
        List<Message> messages = Arrays.asList(message, message);

        when(userHolder.getUser()).thenReturn(user);
        when(user.getProfileId()).thenReturn(initiatorId);
        when(user.getTimezone()).thenReturn("UTC");
        when(conversation.getAdmin()).thenReturn(author);
        when(conversation.getParticipants()).thenReturn(participants);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(participantService.getParticipant(initiatorId)).thenReturn(author);
        when(participantService.getParticipant(subjectId)).thenReturn(receiver);
        when(messageService.createMessage(author, receivers, text, isSystem, null, receiver)).thenReturn(message);
        when(messageService.addMessage(conversation, message, author)).thenReturn(conversation);
        when(conversationRepository.save(conversation)).thenReturn(conversation);
        when(message.getAuthor()).thenReturn(author);
        when(message.getCreatedDate()).thenReturn(Instant.now());
        when(messageService.getMessagesFromConversation(conversation, page, max)).thenReturn(messages);

        GetConversationDto result = conversationService.removeParticipant(conversationId, subjectId);

        verify(participantService).getParticipant(initiatorId);
        verify(participantService).getParticipant(subjectId);
        verify(conversationRepository).findById(conversationId);
        verify(messageService).createMessage(author, receivers, text, isSystem, null, receiver);
        verify(messageService).addMessage(conversation, message, author);
        verify(conversationRepository).save(conversation);
        verify(messageService).getMessagesFromConversation(conversation, page, max);
        verifyNoMoreInteractions(conversationRepository, messageService, participantService);

        assertTrue("GetConversationDto is returned as a result", result instanceof GetConversationDto);
    }

    @Test
    public void testRemoveParticipantForSelfremoval() {
        Long conversationId = 1L;
        Long subjectId = 2L;
        Long initiatorId = 3L;
        String text = "conversation.selfremove.participant";
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> participants = new HashSet<>(Arrays.asList(author, receiver, oneMoreReceiver, participant));
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver, oneMoreReceiver, participant));

        when(userHolder.getUser()).thenReturn(user);
        when(user.getProfileId()).thenReturn(initiatorId);
        when(participantService.getParticipant(initiatorId)).thenReturn(author);
        when(participantService.getParticipant(subjectId)).thenReturn(author);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversation.getAdmin()).thenReturn(author);
        when(conversation.getParticipants()).thenReturn(participants);
        when(messageService.createMessage(author, receivers, text, isSystem, null, null)).thenReturn(message);
        when(messageService.addMessage(conversation, message, author)).thenReturn(conversation);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        GetConversationDto result = conversationService.removeParticipant(conversationId, subjectId);

        verify(participantService).getParticipant(initiatorId);
        verify(participantService).getParticipant(subjectId);
        verify(conversationRepository).findById(conversationId);
        verify(messageService).createMessage(author, receivers, text, isSystem, null, null);
        verify(messageService).addMessage(conversation, message, author);
        verify(conversationRepository).save(conversation);
        verify(messageService).getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        verifyNoMoreInteractions(participantService, messageService, conversationRepository);

        assertTrue("GetConversationDto is returned as a result", result instanceof GetConversationDto);
    }

    @Test
    public void testCountUpdatedConversationsPerParticipant() {
        long participantId = 1L;
        long conversationCount = 3L;

        when(userHolder.getUser()).thenReturn(user);
        when(user.getProfileId()).thenReturn(participantId);
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(conversationRepository.countByNewMessagesPerParticipant(participant)).thenReturn(conversationCount);

        long result = conversationService.countUpdatedConversationsPerParticipant();

        verify(participantService).getParticipant(participantId);
        verify(conversationRepository).countByNewMessagesPerParticipant(participant);
        verifyNoMoreInteractions(participantService, conversationRepository);

        assertEquals("3L is returned as a count of new conversations", conversationCount, result);
    }

    @Test
    public void testRename() {
        Long conversationId = 1L;
        Long participantId = 3L;
        String name = "name";
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver));

        when(userHolder.getUser()).thenReturn(user);
        when(user.getProfileId()).thenReturn(participantId);
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(conversation.getParticipants()).thenReturn(receivers);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(messageService.createMessage(participant, receivers, CONVERSATION_RENAMED, isSystem, name, null)).thenReturn(message);
        when(messageService.addMessage(conversation, message, participant)).thenReturn(conversation);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        GetConversationDto result = conversationService.rename(conversationId, name);

        verify(participantService).getParticipant(participantId);
        verify(conversationRepository).findById(conversationId);
        verify(messageService).createMessage(participant, receivers, CONVERSATION_RENAMED, isSystem, name, null);
        verify(messageService).addMessage(conversation, message, participant);
        verify(conversationRepository).save(conversation);
        verify(messageService).getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        verifyNoMoreInteractions(participantService, messageService, conversationRepository);

        assertTrue("GetConversationDto is returned as a result", result instanceof GetConversationDto);
    }

    @Test
    public void testRenameForEmptyName() {
        Long conversationId = 1L;
        Long participantId = 3L;
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver));

        when(userHolder.getUser()).thenReturn(user);
        when(user.getProfileId()).thenReturn(participantId);
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(conversation.getParticipants()).thenReturn(receivers);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(messageService.createMessage(participant, receivers,
                CONVERSATION_NAME_DROPPED, isSystem, null, null)).thenReturn(message);
        when(messageService.addMessage(conversation, message, participant)).thenReturn(conversation);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        GetConversationDto result = conversationService.rename(conversationId, "");

        verify(participantService).getParticipant(participantId);
        verify(conversationRepository).findById(conversationId);
        verify(messageService).createMessage(participant, receivers,
                CONVERSATION_NAME_DROPPED, isSystem, null, null);        verify(conversationRepository).save(conversation);
        verify(messageService).addMessage(conversation, message, participant);
        verify(messageService).getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        verifyNoMoreInteractions(messageService, conversationRepository, participantService);

        assertTrue("GetConversationDto is returned as a result", result instanceof GetConversationDto);
    }

    @Test
    public void testMarkMessagesAsRead() {
        long conversationId = 1l;
        long participantId = 2l;
        List<Long> messageIds = Arrays.asList(2L, 3L);
        List<Message> messages = Arrays.asList(message, message);
        Set<Participant> participants = new HashSet<>(Arrays.asList(participant, receiver));

        when(userHolder.getUser()).thenReturn(user);
        when(user.getProfileId()).thenReturn(participantId);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(messageService.findMessagesByIds(messageIds)).thenReturn(messages);
        when(message.getConversation()).thenReturn(conversation);
        when(message.getCreatedDate()).thenReturn(Instant.now());
        when(conversation.getParticipants()).thenReturn(participants);

        conversationService.markMessagesAsRead(conversationId, messageIds);

        verify(participantService).getParticipant(participantId);
        verify(conversationRepository).findById(conversationId);
        verify(messageService).findMessagesByIds(messageIds);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(messageService, participantService, conversationRepository);
    }
}
