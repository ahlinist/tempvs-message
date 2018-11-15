package club.tempvs.message.service;

import club.tempvs.message.dao.ConversationRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.service.impl.ConversationServiceImpl;
import club.tempvs.message.util.ObjectFactory;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class ConversationServiceTest {

    private static final String CONVERSATION_RENAMED = "conversation.update.name";

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
    private MessageSource messageSource;

    @Before
    public void setup() {
        this.conversationService = new ConversationServiceImpl(objectFactory, messageService, conversationRepository, messageSource);
    }

    @Test
    public void testCreateConversationOf2Participants() {
        String conversationName = "name";
        Set<Participant> receivers = new HashSet<>();
        receivers.add(receiver);

        Set<Participant> participants = new HashSet<>();
        participants.add(author);
        participants.add(receiver);

        when(objectFactory.getInstance(Conversation.class)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.createConversation(author, receivers, conversationName, message);

        verify(conversation).setParticipants(receivers);
        verify(conversation).addParticipant(author);
        verify(conversation).getParticipants();
        verify(conversation).setName(conversationName);
        verify(conversation).addMessage(message);
        verify(conversation).setLastMessage(message);
        verify(conversation).setType(Conversation.Type.DIALOGUE);
        verify(message).setConversation(conversation);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(conversation, author, receiver, message, conversationRepository);

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

        Conversation result = conversationService.createConversation(author, receivers, conversationName, message);

        verify(conversation).setParticipants(receivers);
        verify(conversation).addParticipant(author);
        verify(conversation).getParticipants();
        verify(conversation).setAdmin(author);
        verify(conversation).setName(conversationName);
        verify(conversation).addMessage(message);
        verify(conversation).setLastMessage(message);
        verify(conversation).setType(Conversation.Type.CONFERENCE);
        verify(message).setConversation(conversation);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(conversation, author, receiver, message, conversationRepository);

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
        String text = "text";
        String translatedText = "translated text";
        Locale locale = Locale.ENGLISH;
        Set<Participant> participants = new HashSet<>();
        participants.add(participant);
        List<Conversation> conversations = new ArrayList<>();
        conversations.add(conversation);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "lastMessage.createdDate");

        when(conversationRepository.findByParticipantsIn(participants, pageable)).thenReturn(conversations);
        when(conversation.getLastMessage()).thenReturn(message);
        when(message.getSystem()).thenReturn(true);
        when(message.getText()).thenReturn(text);
        when(messageSource.getMessage(text, null, locale)).thenReturn(translatedText);

        List<Conversation> result = conversationService.getConversationsByParticipant(participant, locale, page, size);

        verify(conversation).getLastMessage();
        verify(message).getSystem();
        verify(message).getText();
        verify(messageSource).getMessage(message.getText(), null, locale);
        verify(message).setText(translatedText);
        verify(conversation).setLastMessage(message);
        verify(conversationRepository).findByParticipantsIn(participants, pageable);
        verifyNoMoreInteractions(participant, conversation, messageSource, conversationRepository);

        assertEquals("A list of one conversation is returned", result, conversations);
    }

    @Test
    public void testAddParticipantForConversationOf2() {
        String text = "conversation.conference.created";
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> initialParticipants = new HashSet<>(Arrays.asList(author, receiver));
        Set<Participant> receivers = new HashSet<>(initialParticipants);
        receivers.add(oneMoreReceiver);
        receivers.remove(author);
        Set<Participant> finalParticipants = new HashSet<>(initialParticipants);
        finalParticipants.add(oneMoreReceiver);

        when(conversation.getParticipants()).thenReturn(initialParticipants);
        when(conversation.getType()).thenReturn(Conversation.Type.DIALOGUE);
        when(objectFactory.getInstance(Conversation.class)).thenReturn(newConversation);
        when(conversation.getAdmin()).thenReturn(author);
        when(messageService.createMessage(author, receivers, text, isSystem, null)).thenReturn(message);
        when(newConversation.getParticipants()).thenReturn(finalParticipants);
        when(conversationRepository.save(newConversation)).thenReturn(newConversation);

        Conversation result = conversationService.addParticipant(conversation, author, oneMoreReceiver);

        verify(conversation).getParticipants();
        verify(conversation).getType();
        verify(conversation).getAdmin();
        verify(messageService).createMessage(author, receivers, text, isSystem, null);
        verify(objectFactory).getInstance(Conversation.class);
        verify(newConversation).setParticipants(receivers);
        verify(newConversation).addParticipant(author);
        verify(newConversation).setName(null);
        verify(newConversation).addMessage(message);
        verify(newConversation).setLastMessage(message);
        verify(message).setConversation(newConversation);
        verify(newConversation).getParticipants();
        verify(newConversation).setAdmin(author);
        verify(newConversation).setType(Conversation.Type.CONFERENCE);
        verify(conversationRepository).save(newConversation);
        verifyNoMoreInteractions(author, conversation,
                newConversation, objectFactory, messageService, conversationRepository);

        assertEquals("New conversation is returned as a result", newConversation, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAlreadyPresentParticipantToConversationOf2() {
        Set<Participant> initialParticipants = new HashSet<>(Arrays.asList(author, receiver));

        when(conversation.getParticipants()).thenReturn(initialParticipants);

        conversationService.addParticipant(conversation, author, receiver);

        verify(conversation).getParticipants();
        verifyNoMoreInteractions(author, conversation,
                newConversation, objectFactory, messageService, conversationRepository);
    }

    @Test
    public void testAddParticipantForConversationOf4() {
        String text = "conversation.add.participant";
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> initialParticipants = new HashSet<>();
        initialParticipants.add(author);
        initialParticipants.add(receiver);
        initialParticipants.add(participant);
        Set<Participant> receivers = new HashSet<>(initialParticipants);
        receivers.add(oneMoreReceiver);
        receivers.remove(author);

        when(conversation.getParticipants()).thenReturn(initialParticipants);
        when(conversation.getType()).thenReturn(Conversation.Type.CONFERENCE);
        when(conversation.getAdmin()).thenReturn(author);
        when(messageService.createMessage(author, receivers, text, isSystem, null, oneMoreReceiver)).thenReturn(message);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.addParticipant(conversation, author, oneMoreReceiver);

        verify(conversation).getParticipants();
        verify(conversation).getType();
        verify(conversation).getAdmin();
        verify(messageService).createMessage(author, receivers, text, isSystem, null, oneMoreReceiver);
        verify(conversation).addMessage(message);
        verify(conversation).setLastMessage(message);
        verify(conversation).addParticipant(oneMoreReceiver);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(author, conversation, objectFactory, messageService, conversationRepository);

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
        verifyNoMoreInteractions(conversation, conversationRepository);

        assertEquals("Conversation is returned as a result", conversation, result);
    }

    @Test
    public void testRemoveParticipantForSelfremoval() {
        String text = "conversation.selfremove.participant";
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> participants = new HashSet<>(Arrays.asList(author, receiver, oneMoreReceiver, participant));
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver, oneMoreReceiver, participant));

        when(conversation.getAdmin()).thenReturn(participant);
        when(conversation.getParticipants()).thenReturn(participants);
        when(messageService.createMessage(author, receivers, text, isSystem, null)).thenReturn(message);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.removeParticipant(conversation, author, author);

        verify(conversation).getAdmin();
        verify(conversation).getParticipants();
        verify(conversation).removeParticipant(author);
        verify(messageService).createMessage(author, receivers, text, isSystem, null);
        verify(conversation).addMessage(message);
        verify(conversation).setLastMessage(message);
        verify(message).setConversation(conversation);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(conversation, message, messageService, conversationRepository);

        assertEquals("Conversation is returned as a result", conversation, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveParticipantFor2MembersOnly() {
        Set<Participant> participants = new HashSet<>();
        participants.add(author);
        participants.add(receiver);

        when(conversation.getParticipants()).thenReturn(participants);

        conversationService.removeParticipant(conversation, author, receiver);

        verify(conversation).getParticipants();
        verifyNoMoreInteractions(conversation, conversationRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveParticipantByNonAdmin() {
        Set<Participant> participants = new HashSet<>();
        participants.add(author);
        participants.add(receiver);
        participants.add(oneMoreReceiver);
        participants.add(participant);

        when(conversation.getAdmin()).thenReturn(oneMoreReceiver);
        when(conversation.getParticipants()).thenReturn(participants);

        conversationService.removeParticipant(conversation, author, receiver);

        verify(conversation).getAdmin();
        verify(conversation).getParticipants();
        verifyNoMoreInteractions(conversation, conversationRepository);
    }

    @Test
    public void testFindConversation() {
        Set<Participant> authorSet = new HashSet<>(Arrays.asList(author));
        Set<Participant> receiverSet = new HashSet<>(Arrays.asList(receiver));

        when(conversationRepository
                .findOneByTypeAndParticipantsContainsAndParticipantsContains(Conversation.Type.DIALOGUE, authorSet, receiverSet))
                .thenReturn(conversation);

        Conversation result = conversationService.findDialogue(author, receiver);

        verify(conversationRepository).findOneByTypeAndParticipantsContainsAndParticipantsContains(Conversation.Type.DIALOGUE, authorSet, receiverSet);
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
    public void testUpdateName() {
        String name = "name";
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver));

        when(conversation.getParticipants()).thenReturn(receivers);
        when(messageService.createMessage(participant, receivers, CONVERSATION_RENAMED, isSystem, name)).thenReturn(message);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.updateName(conversation, participant, name);

        verify(messageService).createMessage(participant, receivers, CONVERSATION_RENAMED, isSystem, name);
        verify(conversation).getParticipants();
        verify(conversation).setName(name);
        verify(conversation).addMessage(message);
        verify(conversation).setLastMessage(message);
        verify(message).setConversation(conversation);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(participant, message, conversation, messageService, conversationRepository);

        assertEquals("Updated conversation is returned as a result", conversation, result);
    }
}
