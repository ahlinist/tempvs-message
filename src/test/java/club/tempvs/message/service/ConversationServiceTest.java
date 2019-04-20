package club.tempvs.message.service;

import static club.tempvs.message.domain.Conversation.Type.CONFERENCE;

import club.tempvs.message.api.ForbiddenException;
import club.tempvs.message.dao.ConversationRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.ErrorsDto;
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
    private Conversation conversation, newConversation;
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
    private ErrorsDto errorsDto;
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
    public void testBuildConversation() {
        Long authorId = 1L;
        Long receiverId = 2L;
        Long participantId = 3L;
        String text = "text";
        String name = "name";
        String timeZone = "UTC";
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(receiverId, participantId));
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver, participant));
        List<Message> messages = Arrays.asList(message, message, message);

        when(userHolder.getUser()).thenReturn(user);
        when(user.getProfileId()).thenReturn(authorId);
        when(user.getTimezone()).thenReturn(timeZone);
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
        verify(conversation).addParticipant(receiver);
        verify(conversation).addParticipant(author);
        verify(conversation, times(2)).getParticipants();
        verify(conversation).setName(name);
        verify(conversation).addMessage(message);
        verify(conversation).setLastMessage(message);
        verify(conversation).setType(Conversation.Type.DIALOGUE);
        verify(message).setConversation(conversation);
        verify(messageService).createMessage(author, receivers, text, false, null, null);
        verify(messageService).getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(participantService, messageService, conversationRepository);

        assertTrue("An instance of GetConversationDto is returned", result instanceof GetConversationDto);
    }

    @Test
    public void testBuildConversationOf2ParticipantsForExistentOne() {
        String conversationName = "name";
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver));

        when(conversationRepository.findDialogue(Conversation.Type.DIALOGUE, author, receiver)).thenReturn(conversation);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.buildConversation(author, receivers, conversationName, message);

        verify(validationHelper).validateConversationCreation(author, receivers, message);
        verify(conversationRepository).findDialogue(Conversation.Type.DIALOGUE, author, receiver);
        verify(conversation).addMessage(message);
        verify(conversation).setLastMessage(message);
        verify(message).setConversation(conversation);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(conversation, objectFactory, validationHelper, errorsDto,
                author, receiver, message, messageService, conversationRepository);

        assertEquals("Service returns a conversation instance", result, conversation);
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateConversationWhereAuthorEqualsTheOnlyReceiver() {
        Set<Participant> receivers = new HashSet<>(Arrays.asList(author));

        conversationService.buildConversation(author, receivers, null, message);
    }

    @Test
    public void testCreateConversationOf2ParticipantsForNonExistentOne() {
        String conversationName = "name";
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver));

        Set<Participant> participants = new HashSet<>(Arrays.asList(author, receiver));

        when(conversationRepository.findDialogue(Conversation.Type.DIALOGUE, author, receiver)).thenReturn(null);
        when(objectFactory.getInstance(Conversation.class)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.buildConversation(author, receivers, conversationName, message);

        verify(validationHelper).validateConversationCreation(author, receivers, message);
        verify(conversationRepository).findDialogue(Conversation.Type.DIALOGUE, author, receiver);
        verify(objectFactory).getInstance(Conversation.class);
        verify(conversation).addParticipant(receiver);
        verify(conversation).addParticipant(author);
        verify(conversation).getParticipants();
        verify(conversation).setName(conversationName);
        verify(conversation).addMessage(message);
        verify(conversation).setLastMessage(message);
        verify(conversation).setType(Conversation.Type.DIALOGUE);
        verify(message).setConversation(conversation);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(conversation, objectFactory, validationHelper,
                author, receiver, message, messageService, conversationRepository);

        assertEquals("Service returns a conversation instance", result, conversation);
    }

    @Test
    public void testCreateConversationOf3Participants() {
        String conversationName = "name";
        Set<Participant> receivers = new HashSet<>();
        receivers.add(receiver);
        receivers.add(oneMoreReceiver);

        Set<Participant> participants = new HashSet<>();
        participants.add(author);
        participants.add(receiver);
        participants.add(oneMoreReceiver);

        when(objectFactory.getInstance(Conversation.class)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.buildConversation(author, receivers, conversationName, message);

        verify(validationHelper).validateConversationCreation(author, receivers, message);
        verify(objectFactory).getInstance(Conversation.class);
        verify(conversation).addParticipant(author);
        verify(conversation).addParticipant(receiver);
        verify(conversation).addParticipant(oneMoreReceiver);
        verify(conversation).getParticipants();
        verify(conversation).setAdmin(author);
        verify(conversation).setName(conversationName);
        verify(conversation).addMessage(message);
        verify(conversation).setLastMessage(message);
        verify(conversation).setType(Conversation.Type.CONFERENCE);
        verify(message).setConversation(conversation);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(conversation, author, objectFactory, validationHelper, errorsDto,
                receiver, oneMoreReceiver, message, messageService, conversationRepository);

        assertEquals("Service returns a conversation instance", result, conversation);
    }

    @Test
    public void testGetConversation() {
        long conversationId = 1L;

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        Conversation result = conversationService.getConversation(conversationId);

        verify(conversationRepository).findById(conversationId);
        verifyNoMoreInteractions(conversationRepository, conversation);

        assertEquals("A conversation with given id is retrieved", result, conversation);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetConversationNotFound() {
        long conversationId = 1L;

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.ofNullable(null));

        conversationService.getConversation(conversationId);
    }

    @Test
    public void testAddMessage() {
        Set<Participant> receivers = new HashSet<>();
        receivers.add(receiver);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.addMessage(conversation, message);

        verify(conversation).addMessage(message);
        verify(conversation).setLastMessage(message);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(conversation, conversationRepository, author, receiver);

        assertEquals("Updated conversation is returned as a successful result", result, conversation);
    }

    @Test
    public void testGetConversationsByParticipant() {
        int page = 0;
        int size = 40;
        long participantId = 1l;
        String timeZone = "UTC";
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
        when(user.getTimezone()).thenReturn(timeZone);
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
        String text = "conversation.conference.created";
        Set<Participant> participantsToAdd = new LinkedHashSet<>(Arrays.asList(oneMoreReceiver));
        Set<Participant> initialParticipants = new LinkedHashSet<>(Arrays.asList(author, receiver));
        Set<Participant> receivers = new LinkedHashSet<>(initialParticipants);
        receivers.add(oneMoreReceiver);
        receivers.remove(author);
        Set<Participant> finalParticipants = new LinkedHashSet<>(initialParticipants);
        finalParticipants.add(oneMoreReceiver);

        when(conversation.getParticipants()).thenReturn(initialParticipants);
        when(conversation.getType()).thenReturn(Conversation.Type.DIALOGUE);
        when(objectFactory.getInstance(Conversation.class)).thenReturn(newConversation);
        when(messageService.createMessage(author, receivers, text, true, null, null)).thenReturn(message);
        when(newConversation.getParticipants()).thenReturn(finalParticipants);
        when(conversationRepository.save(newConversation)).thenReturn(newConversation);

        Conversation result = conversationService.addParticipants(conversation, author, participantsToAdd);

        verify(validationHelper).validateParticipantsAddition(author, participantsToAdd, initialParticipants);
        verify(validationHelper).validateConversationCreation(author, receivers, message);
        verify(conversation).getParticipants();
        verify(conversation).getType();
        verify(messageService).createMessage(author, receivers, text, true, null, null);
        verify(objectFactory).getInstance(Conversation.class);
        verify(newConversation).addParticipant(receiver);
        verify(newConversation).addParticipant(oneMoreReceiver);
        verify(newConversation).addParticipant(author);
        verify(newConversation).setName(null);
        verify(newConversation).addMessage(message);
        verify(newConversation).setLastMessage(message);
        verify(message).setConversation(newConversation);
        verify(newConversation).getParticipants();
        verify(newConversation).setAdmin(author);
        verify(newConversation).setType(Conversation.Type.CONFERENCE);
        verify(conversationRepository).save(newConversation);
        verifyNoMoreInteractions(author, conversation, validationHelper, oneMoreReceiver,
                newConversation, objectFactory, messageService, conversationRepository);

        assertEquals("New conversation is returned as a result", newConversation, result);
    }

    @Test
    public void testAddParticipantForConversationOf3() {
        String text = "conversation.add.participant";
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> participantsToAdd = new LinkedHashSet<>(Arrays.asList(oneMoreReceiver));
        Set<Participant> initialParticipants = new LinkedHashSet<>(Arrays.asList(author, receiver, participant));
        Set<Participant> receivers = new LinkedHashSet<>(initialParticipants);
        receivers.add(oneMoreReceiver);
        receivers.remove(author);

        when(conversation.getParticipants()).thenReturn(initialParticipants);
        when(conversation.getType()).thenReturn(Conversation.Type.CONFERENCE);
        when(messageService.createMessage(author, receivers, text, isSystem, null, oneMoreReceiver)).thenReturn(message);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.addParticipants(conversation, author, participantsToAdd);

        verify(validationHelper).validateParticipantsAddition(author, participantsToAdd, initialParticipants);
        verify(conversation).getParticipants();
        verify(conversation).getType();
        verify(messageService).createMessage(author, receivers, text, isSystem, null, oneMoreReceiver);
        verify(conversation).addMessage(message);
        verify(conversation).setLastMessage(message);
        verify(conversation).addParticipant(oneMoreReceiver);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(author, receiver, conversation, objectFactory, messageService, conversationRepository,
                validationHelper, oneMoreReceiver);

        assertEquals("Conversation is returned as a result", conversation, result);
    }

    @Test
    public void testRemoveParticipant() {
        String text = "conversation.remove.participant";
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> participants = new HashSet<>(Arrays.asList(author, receiver, oneMoreReceiver, participant));
        Set<Participant> receivers = new HashSet<>(Arrays.asList(oneMoreReceiver, participant));

        when(conversation.getAdmin()).thenReturn(author);
        when(conversation.getParticipants()).thenReturn(participants);
        when(messageService.createMessage(author, receivers, text, isSystem, null, receiver)).thenReturn(message);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.removeParticipant(conversation, author, receiver);

        verify(conversation).getAdmin();
        verify(conversation).getParticipants();
        verify(conversation).removeParticipant(receiver);
        verify(messageService).createMessage(author, receivers, text, isSystem, null,receiver);
        verify(conversation).addMessage(message);
        verify(message).setConversation(conversation);
        verify(conversation).setLastMessage(message);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(conversation, conversationRepository, messageService, message, receiver, author);

        assertEquals("Conversation is returned as a result", conversation, result);
    }

    @Test(expected = ForbiddenException.class)
    public void testRemoveParticipantAsNonAdmin() {
        when(conversation.getAdmin()).thenReturn(participant);

        conversationService.removeParticipant(conversation, author, receiver);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveParticipantForConversationOf2() {
        Set<Participant> participants = new HashSet<>(Arrays.asList(author, receiver));

        when(conversation.getParticipants()).thenReturn(participants);
        when(validationHelper.getErrors()).thenReturn(errorsDto);
        doThrow(new IllegalArgumentException()).when(validationHelper).processErrors(errorsDto);

        conversationService.removeParticipant(conversation, author, receiver);
    }

    @Test
    public void testRemoveParticipantForSelfremoval() {
        String text = "conversation.selfremove.participant";
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> participants = new HashSet<>(Arrays.asList(author, receiver, oneMoreReceiver, participant));
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver, oneMoreReceiver, participant));

        when(conversation.getAdmin()).thenReturn(author);
        when(conversation.getParticipants()).thenReturn(participants);
        when(messageService.createMessage(author, receivers, text, isSystem, null, null)).thenReturn(message);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.removeParticipant(conversation, author, author);

        verify(conversation).getAdmin();
        verify(conversation).getParticipants();
        verify(conversation).removeParticipant(author);
        verify(messageService).createMessage(author, receivers, text, isSystem, null, null);
        verify(conversation).addMessage(message);
        verify(conversation).setLastMessage(message);
        verify(message).setConversation(conversation);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(conversation, message, messageService, conversationRepository, author, receiver);

        assertEquals("Conversation is returned as a result", conversation, result);
    }

    @Test
    public void testFindConversation() {
        when(conversationRepository.findDialogue(Conversation.Type.DIALOGUE, author, receiver)).thenReturn(conversation);

        Conversation result = conversationService.findDialogue(author, receiver);

        verify(conversationRepository).findDialogue(Conversation.Type.DIALOGUE, author, receiver);
        verifyNoMoreInteractions(conversationRepository, conversation, author, receiver);

        assertEquals("Conversation is returned as a result", conversation, result);
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
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        GetConversationDto result = conversationService.rename(conversationId, name);

        verify(participantService).getParticipant(participantId);
        verify(conversationRepository).findById(conversationId);
        verify(messageService).createMessage(participant, receivers, CONVERSATION_RENAMED, isSystem, name, null);
        verify(conversationRepository).save(conversation);
        verify(messageService).getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        verifyNoMoreInteractions(participant, messageService, conversationRepository);

        assertTrue("GetConversationDto is returned as a result", result instanceof GetConversationDto);
    }

    @Test
    public void testRenameForEmptyName() {
        Long conversationId = 1L;
        Long participantId = 3L;
        String name = "";
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver));

        when(userHolder.getUser()).thenReturn(user);
        when(user.getProfileId()).thenReturn(participantId);
        when(participantService.getParticipant(participantId)).thenReturn(participant);
        when(conversation.getParticipants()).thenReturn(receivers);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(messageService.createMessage(participant, receivers,
                CONVERSATION_NAME_DROPPED, isSystem, null, null)).thenReturn(message);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        GetConversationDto result = conversationService.rename(conversationId, name);

        verify(participantService).getParticipant(participantId);
        verify(conversationRepository).findById(conversationId);
        verify(messageService).createMessage(participant, receivers,
                CONVERSATION_NAME_DROPPED, isSystem, null, null);        verify(conversationRepository).save(conversation);
        verify(messageService).getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        verifyNoMoreInteractions(participant, messageService, conversationRepository);

        assertTrue("GetConversationDto is returned as a result", result instanceof GetConversationDto);
    }
}
