package club.tempvs.message.service;

import club.tempvs.message.api.ForbiddenException;
import club.tempvs.message.dao.ConversationRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.ErrorsDto;
import club.tempvs.message.service.impl.ConversationServiceImpl;
import club.tempvs.message.util.ObjectFactory;
import static org.junit.Assert.*;

import club.tempvs.message.util.ValidationHelper;
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
    @Mock
    private ValidationHelper validationHelper;
    @Mock
    private ErrorsDto errorsDto;

    @Before
    public void setup() {
        this.conversationService = new ConversationServiceImpl(objectFactory, messageService, conversationRepository,
                messageSource, validationHelper);
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

        verify(conversation).addParticipant(receiver);
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
        List<Conversation> conversations = new ArrayList<>();
        conversations.add(conversation);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "lastMessage.createdDate");
        String systemArgs = "systemArgs";
        List<Object[]> unreadMessagesPerConversation = new ArrayList<>();
        unreadMessagesPerConversation.add(new Object[]{conversation, 3L});

        when(conversationRepository.findByParticipantsIn(participant, pageable)).thenReturn(conversations);
        when(conversationRepository.countUnreadMessages(conversations, participant)).thenReturn(unreadMessagesPerConversation);
        when(conversation.getLastMessage()).thenReturn(message);
        when(message.getSystem()).thenReturn(true);
        when(message.getText()).thenReturn(text);
        when(message.getSystemArgs()).thenReturn(systemArgs);
        when(messageSource.getMessage(text, new String[]{systemArgs}, text, locale)).thenReturn(translatedText);

        List<Conversation> result = conversationService.getConversationsByParticipant(participant, locale, page, size);

        verify(conversationRepository).findByParticipantsIn(participant, pageable);
        verify(conversationRepository).countUnreadMessages(conversations, participant);
        verify(conversation).setUnreadMessagesCount(3L);
        verify(conversation).getLastMessage();
        verify(message).getSystem();
        verify(message).getText();
        verify(message).getSystemArgs();
        verify(messageSource).getMessage(text, new String[]{systemArgs}, text, locale);
        verify(message).setText(translatedText);
        verify(conversation).setLastMessage(message);
        verifyNoMoreInteractions(participant, message, conversation, messageSource, conversationRepository);

        assertEquals("A list of one conversation is returned", result, conversations);
    }

    @Test
    public void testAddParticipantForConversationOf2() {
        String text = "conversation.conference.created";
        String userType = "USER";
        String emptyString = "";
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> participantsToAdd = new HashSet<>(Arrays.asList(oneMoreReceiver));
        Set<Participant> initialParticipants = new HashSet<>(Arrays.asList(author, receiver));
        Set<Participant> receivers = new HashSet<>(initialParticipants);
        receivers.add(oneMoreReceiver);
        receivers.remove(author);
        Set<Participant> finalParticipants = new HashSet<>(initialParticipants);
        finalParticipants.add(oneMoreReceiver);

        when(validationHelper.getErrors()).thenReturn(errorsDto);
        when(conversation.getParticipants()).thenReturn(initialParticipants);
        when(author.getType()).thenReturn(userType);
        when(author.getPeriod()).thenReturn(emptyString);
        when(oneMoreReceiver.getType()).thenReturn(userType);
        when(oneMoreReceiver.getPeriod()).thenReturn(emptyString);
        when(conversation.getType()).thenReturn(Conversation.Type.DIALOGUE);
        when(objectFactory.getInstance(Conversation.class)).thenReturn(newConversation);
        when(messageService.createMessage(author, receivers, text, isSystem, null)).thenReturn(message);
        when(newConversation.getParticipants()).thenReturn(finalParticipants);
        when(conversationRepository.save(newConversation)).thenReturn(newConversation);

        Conversation result = conversationService.addParticipants(conversation, author, participantsToAdd);

        verify(validationHelper).getErrors();
        verify(conversation).getParticipants();
        verify(oneMoreReceiver).getType();
        verify(oneMoreReceiver).getPeriod();
        verify(author).getType();
        verify(author).getPeriod();
        verify(conversation).getType();
        verify(validationHelper).processErrors(errorsDto);
        verify(messageService).createMessage(author, receivers, text, isSystem, null);
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
        String clubType = "CLUB";
        String antiquity = "ANTIQUITY";
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> participantsToAdd = new HashSet<>(Arrays.asList(oneMoreReceiver));
        Set<Participant> initialParticipants = new HashSet<>();
        initialParticipants.add(author);
        initialParticipants.add(receiver);
        Set<Participant> receivers = new HashSet<>(initialParticipants);
        receivers.add(oneMoreReceiver);
        receivers.remove(author);

        when(validationHelper.getErrors()).thenReturn(errorsDto);
        when(conversation.getParticipants()).thenReturn(initialParticipants);
        when(oneMoreReceiver.getType()).thenReturn(clubType);
        when(oneMoreReceiver.getPeriod()).thenReturn(antiquity);
        when(author.getType()).thenReturn(clubType);
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
        verifyNoMoreInteractions(conversation, message, messageService, conversationRepository, author, receiver);

        assertEquals("Conversation is returned as a result", conversation, result);
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
