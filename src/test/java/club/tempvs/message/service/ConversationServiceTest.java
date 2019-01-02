package club.tempvs.message.service;

import club.tempvs.message.api.ForbiddenException;
import club.tempvs.message.dao.ConversationRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.ErrorsDto;
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

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class ConversationServiceTest {

    private static final String CONVERSATION_RENAMED = "conversation.rename";
    private static final String CONVERSATION_NAME_DROPPED = "conversation.drop.name";
    private static final String EMPTY_STRING = "";
    private static final String USER_TYPE = "USER";
    private static final String CLUB_TYPE = "CLUB";

    private ConversationService conversationService;

    @Mock
    private Message message;

    @Mock
    private Conversation conversation;

    @Mock
    private Conversation newConversation;

    @Mock
    private Participant author;

    @Mock
    private Participant receiver;

    @Mock
    private Participant participant;

    @Mock
    private Participant oneMoreReceiver;

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

    @Before
    public void setup() {
        this.conversationService = new ConversationServiceImpl(
                objectFactory, messageService, conversationRepository, localeHelper, validationHelper);
    }

    @Test
    public void testCreateConversationOf2ParticipantsForExistentOne() {
        String text = "text";
        String conversationName = "name";
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver));

        when(validationHelper.getErrors()).thenReturn(errorsDto);
        when(message.getText()).thenReturn(text);
        when(receiver.getType()).thenReturn(USER_TYPE);
        when(author.getType()).thenReturn(USER_TYPE);
        when(receiver.getPeriod()).thenReturn(EMPTY_STRING);
        when(author.getPeriod()).thenReturn(EMPTY_STRING);
        when(conversationRepository.findDialogue(Conversation.Type.DIALOGUE, author, receiver)).thenReturn(conversation);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.createConversation(author, receivers, conversationName, message);

        verify(validationHelper).getErrors();
        verify(message).getText();
        verify(receiver).getType();
        verify(author).getType();
        verify(receiver).getPeriod();
        verify(author).getPeriod();
        verify(validationHelper).processErrors(errorsDto);
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

        conversationService.createConversation(author, receivers, null, message);
    }

    @Test
    public void testCreateConversationOf2ParticipantsForNonExistentOne() {
        String text = "text";
        String conversationName = "name";
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver));

        Set<Participant> participants = new HashSet<>(Arrays.asList(author, receiver));

        when(validationHelper.getErrors()).thenReturn(errorsDto);
        when(message.getText()).thenReturn(text);
        when(receiver.getType()).thenReturn(USER_TYPE);
        when(author.getType()).thenReturn(USER_TYPE);
        when(receiver.getPeriod()).thenReturn(EMPTY_STRING);
        when(author.getPeriod()).thenReturn(EMPTY_STRING);
        when(conversationRepository.findDialogue(Conversation.Type.DIALOGUE, author, receiver)).thenReturn(null);
        when(objectFactory.getInstance(Conversation.class)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.createConversation(author, receivers, conversationName, message);

        verify(validationHelper).getErrors();
        verify(message).getText();
        verify(receiver).getType();
        verify(author).getType();
        verify(receiver).getPeriod();
        verify(author).getPeriod();
        verify(validationHelper).processErrors(errorsDto);
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
        String text = "text";
        String conversationName = "name";
        Set<Participant> receivers = new HashSet<>();
        receivers.add(receiver);
        receivers.add(oneMoreReceiver);

        Set<Participant> participants = new HashSet<>();
        participants.add(author);
        participants.add(receiver);
        participants.add(oneMoreReceiver);

        when(validationHelper.getErrors()).thenReturn(errorsDto);
        when(message.getText()).thenReturn(text);
        when(receiver.getType()).thenReturn(USER_TYPE);
        when(oneMoreReceiver.getType()).thenReturn(USER_TYPE);
        when(author.getType()).thenReturn(USER_TYPE);
        when(receiver.getPeriod()).thenReturn(EMPTY_STRING);
        when(oneMoreReceiver.getPeriod()).thenReturn(EMPTY_STRING);
        when(author.getPeriod()).thenReturn(EMPTY_STRING);
        when(objectFactory.getInstance(Conversation.class)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.createConversation(author, receivers, conversationName, message);

        verify(validationHelper).getErrors();
        verify(message).getText();
        verify(receiver).getType();
        verify(oneMoreReceiver).getType();
        verify(author).getType();
        verify(receiver).getPeriod();
        verify(oneMoreReceiver).getPeriod();
        verify(author).getPeriod();
        verify(validationHelper).processErrors(errorsDto);
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

    @Test
    public void testGetConversationNotFound() {
        long conversationId = 1L;

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.ofNullable(null));

        Conversation result = conversationService.getConversation(conversationId);

        verify(conversationRepository).findById(conversationId);
        verifyNoMoreInteractions(conversationRepository);

        assertEquals("A conversation with given id is retrieved", result, null);
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
        List<Conversation> conversations = new ArrayList<>();
        conversations.add(conversation);
        Pageable pageable = PageRequest.of(page, size);
        List<Object[]> conversationsPerParticipant = new ArrayList<>();
        conversationsPerParticipant.add(new Object[]{conversation, 3L});

        when(conversationRepository.findConversationsPerParticipant(participant, pageable)).thenReturn(conversationsPerParticipant);
        when(conversation.getLastMessage()).thenReturn(message);
        when(localeHelper.translateMessageIfSystem(message)).thenReturn(message);

        List<Conversation> result = conversationService.getConversationsByParticipant(participant, page, size);

        verify(conversationRepository).findConversationsPerParticipant(participant, pageable);
        verify(conversation).setUnreadMessagesCount(3L);
        verify(conversation).getLastMessage();
        verify(localeHelper).translateMessageIfSystem(message);
        verify(conversation).setLastMessage(message);
        verifyNoMoreInteractions(participant, message, conversation, localeHelper, conversationRepository);

        assertEquals("A list of one conversation is returned", result, conversations);
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

        when(validationHelper.getErrors()).thenReturn(errorsDto);
        when(conversation.getParticipants()).thenReturn(initialParticipants);
        when(author.getType()).thenReturn(USER_TYPE);
        when(author.getPeriod()).thenReturn(EMPTY_STRING);
        when(oneMoreReceiver.getType()).thenReturn(USER_TYPE);
        when(oneMoreReceiver.getPeriod()).thenReturn(EMPTY_STRING);
        when(receiver.getType()).thenReturn(USER_TYPE);
        when(receiver.getPeriod()).thenReturn(EMPTY_STRING);
        when(conversation.getType()).thenReturn(Conversation.Type.DIALOGUE);
        when(objectFactory.getInstance(Conversation.class)).thenReturn(newConversation);
        when(messageService.createMessage(author, receivers, text, true, null, null)).thenReturn(message);
        when(message.getText()).thenReturn(text);
        when(newConversation.getParticipants()).thenReturn(finalParticipants);
        when(conversationRepository.save(newConversation)).thenReturn(newConversation);

        Conversation result = conversationService.addParticipants(conversation, author, participantsToAdd);

        verify(validationHelper, times(2)).getErrors();
        verify(conversation).getParticipants();
        verify(oneMoreReceiver, times(2)).getType();
        verify(oneMoreReceiver, times(2)).getPeriod();
        verify(receiver).getType();
        verify(receiver).getPeriod();
        verify(author, times(2)).getType();
        verify(author, times(2)).getPeriod();
        verify(conversation).getType();
        verify(validationHelper, times(2)).processErrors(errorsDto);
        verify(messageService).createMessage(author, receivers, text, true, null, null);
        verify(message).getText();
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
        String antiquity = "ANTIQUITY";
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> participantsToAdd = new LinkedHashSet<>(Arrays.asList(oneMoreReceiver));
        Set<Participant> initialParticipants = new LinkedHashSet<>(Arrays.asList(author, receiver, participant));
        Set<Participant> receivers = new LinkedHashSet<>(initialParticipants);
        receivers.add(oneMoreReceiver);
        receivers.remove(author);

        when(validationHelper.getErrors()).thenReturn(errorsDto);
        when(conversation.getParticipants()).thenReturn(initialParticipants);
        when(oneMoreReceiver.getType()).thenReturn(CLUB_TYPE);
        when(oneMoreReceiver.getPeriod()).thenReturn(antiquity);
        when(author.getType()).thenReturn(CLUB_TYPE);
        when(author.getPeriod()).thenReturn(antiquity);
        when(conversation.getType()).thenReturn(Conversation.Type.CONFERENCE);
        when(messageService.createMessage(author, receivers, text, isSystem, null, oneMoreReceiver)).thenReturn(message);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.addParticipants(conversation, author, participantsToAdd);

        verify(validationHelper).getErrors();
        verify(conversation).getParticipants();
        verify(oneMoreReceiver).getType();
        verify(oneMoreReceiver).getPeriod();
        verify(author).getType();
        verify(author).getPeriod();
        verify(conversation).getType();
        verify(validationHelper).processErrors(errorsDto);
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
        long conversationCount = 3L;

        when(conversationRepository.countByNewMessagesPerParticipant(participant)).thenReturn(conversationCount);

        long result = conversationService.countUpdatedConversationsPerParticipant(participant);

        verify(conversationRepository).countByNewMessagesPerParticipant(participant);
        verifyNoMoreInteractions(participant, conversationRepository);

        assertEquals("3L is returned as a count of new conversations", conversationCount, result);
    }

    @Test
    public void testRename() {
        String name = "name";
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver));

        when(conversation.getParticipants()).thenReturn(receivers);
        when(messageService.createMessage(participant, receivers, CONVERSATION_RENAMED, isSystem, name, null)).thenReturn(message);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.rename(conversation, participant, name);

        verify(messageService).createMessage(participant, receivers, CONVERSATION_RENAMED, isSystem, name, null);
        verify(conversation).getParticipants();
        verify(conversation).setName(name);
        verify(conversation).addMessage(message);
        verify(conversation).setLastMessage(message);
        verify(message).setConversation(conversation);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(participant, message, conversation, messageService, conversationRepository);

        assertEquals("Updated conversation is returned as a result", conversation, result);
    }

    @Test
    public void testRenameForEmptyName() {
        String name = "";
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver));

        when(conversation.getParticipants()).thenReturn(receivers);
        when(messageService.createMessage(participant, receivers,
                CONVERSATION_NAME_DROPPED, isSystem, null, null)).thenReturn(message);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.rename(conversation, participant, name);

        verify(messageService).createMessage(participant, receivers,
                CONVERSATION_NAME_DROPPED, isSystem, null, null);
        verify(conversation).getParticipants();
        verify(conversation).setName(name);
        verify(conversation).addMessage(message);
        verify(conversation).setLastMessage(message);
        verify(message).setConversation(conversation);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(participant, receiver, message, conversation, messageService, conversationRepository);

        assertEquals("Updated conversation is returned as a result", conversation, result);
    }
}
