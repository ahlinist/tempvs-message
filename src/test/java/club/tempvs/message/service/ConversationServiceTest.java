package club.tempvs.message.service;

import static club.tempvs.message.domain.Conversation.Type.CONFERENCE;

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

        when(messageService.addMessage(conversation, message)).thenReturn(conversation);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.addMessage(conversation, message);

        verify(messageService).addMessage(conversation, message);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(messageService, conversationRepository);

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
        verify(messageService).createMessage(author, receivers, text, isSystem, null, oneMoreReceiver);
        verify(messageService).addMessage(conversation, message);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(objectFactory, messageService, conversationRepository, validationHelper);

        assertEquals("Conversation is returned as a result", conversation, result);
    }

    @Test
    public void testRemoveParticipant() {
        Long conversationId = 1L;
        Long initiatorId = 3L;
        Long subjectId = 2L;
        String timeZone = "UTC";
        String text = "conversation.remove.participant";
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> participants = new HashSet<>(Arrays.asList(author, receiver, oneMoreReceiver, participant));
        Set<Participant> receivers = new HashSet<>(Arrays.asList(oneMoreReceiver, participant));
        int page = 0;
        int max = 40;
        List<Message> messages = Arrays.asList(message, message);

        when(userHolder.getUser()).thenReturn(user);
        when(user.getProfileId()).thenReturn(initiatorId);
        when(user.getTimezone()).thenReturn(timeZone);
        when(conversation.getAdmin()).thenReturn(author);
        when(conversation.getParticipants()).thenReturn(participants);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(participantService.getParticipant(initiatorId)).thenReturn(author);
        when(participantService.getParticipant(subjectId)).thenReturn(receiver);
        when(messageService.createMessage(author, receivers, text, isSystem, null, receiver)).thenReturn(message);
        when(messageService.addMessage(conversation, message)).thenReturn(conversation);
        when(conversationRepository.save(conversation)).thenReturn(conversation);
        when(message.getAuthor()).thenReturn(author);
        when(message.getCreatedDate()).thenReturn(Instant.now());
        when(messageService.getMessagesFromConversation(conversation, page, max)).thenReturn(messages);

        GetConversationDto result = conversationService.removeParticipant(conversationId, subjectId);

        verify(participantService).getParticipant(initiatorId);
        verify(participantService).getParticipant(subjectId);
        verify(conversationRepository).findById(conversationId);
        verify(messageService).createMessage(author, receivers, text, isSystem, null, receiver);
        verify(messageService).addMessage(conversation, message);
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
        when(messageService.addMessage(conversation, message)).thenReturn(conversation);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        GetConversationDto result = conversationService.removeParticipant(conversationId, subjectId);

        verify(participantService).getParticipant(initiatorId);
        verify(participantService).getParticipant(subjectId);
        verify(conversationRepository).findById(conversationId);
        verify(messageService).createMessage(author, receivers, text, isSystem, null, null);
        verify(messageService).addMessage(conversation, message);
        verify(conversationRepository).save(conversation);
        verify(messageService).getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        verifyNoMoreInteractions(participantService, messageService, conversationRepository);

        assertTrue("GetConversationDto is returned as a result", result instanceof GetConversationDto);
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
        when(messageService.addMessage(conversation, message)).thenReturn(conversation);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        GetConversationDto result = conversationService.rename(conversationId, name);

        verify(participantService).getParticipant(participantId);
        verify(conversationRepository).findById(conversationId);
        verify(messageService).createMessage(participant, receivers, CONVERSATION_RENAMED, isSystem, name, null);
        verify(messageService).addMessage(conversation, message);
        verify(conversationRepository).save(conversation);
        verify(messageService).getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        verifyNoMoreInteractions(participantService, messageService, conversationRepository);

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
        when(messageService.addMessage(conversation, message)).thenReturn(conversation);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        GetConversationDto result = conversationService.rename(conversationId, name);

        verify(participantService).getParticipant(participantId);
        verify(conversationRepository).findById(conversationId);
        verify(messageService).createMessage(participant, receivers,
                CONVERSATION_NAME_DROPPED, isSystem, null, null);        verify(conversationRepository).save(conversation);
        verify(messageService).addMessage(conversation, message);
        verify(messageService).getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        verifyNoMoreInteractions(messageService, conversationRepository, participantService);

        assertTrue("GetConversationDto is returned as a result", result instanceof GetConversationDto);
    }
}
