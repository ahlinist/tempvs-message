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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class ConversationServiceTest {

    private ConversationService conversationService;

    @Mock
    private Message message;
    @Mock
    private Conversation conversation;
    @Mock
    private Participant sender;
    @Mock
    private Participant receiver;
    @Mock
    private Participant participant;
    @Mock
    private Participant oneMoreReceiver;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private ConversationRepository conversationRepository;

    @Before
    public void setup() {
        this.conversationService = new ConversationServiceImpl(objectFactory, conversationRepository);
    }

    @Test
    public void testCreateConversationOf2Participants() {
        String conversationName = "name";
        Set<Participant> receivers = new LinkedHashSet<>();
        receivers.add(receiver);

        Set<Participant> participants = new LinkedHashSet<>();
        participants.add(sender);
        participants.add(receiver);

        when(objectFactory.getInstance(Conversation.class)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);
        when(conversationRepository.saveAndFlush(conversation)).thenReturn(conversation);

        Conversation result = conversationService.createConversation(sender, receivers, conversationName, message);

        verify(conversation).setParticipants(receivers);
        verify(conversation).addParticipant(sender);
        verify(conversation).getParticipants();
        verify(conversation).setName(conversationName);
        verify(conversation).addMessage(message);
        verify(conversation).setLastMessage(message);
        verify(message).setConversation(conversation);
        verify(conversationRepository).saveAndFlush(conversation);
        verifyNoMoreInteractions(conversation, sender, receiver, message, conversationRepository);

        assertEquals("Service returns a conversation instance", result, conversation);
    }

    @Test
    public void testCreateConversationOf3Participants() {
        String conversationName = "name";
        Set<Participant> receivers = new LinkedHashSet<>();
        receivers.add(receiver);
        receivers.add(oneMoreReceiver);

        Set<Participant> participants = new LinkedHashSet<>();
        participants.add(sender);
        participants.add(receiver);
        participants.add(oneMoreReceiver);

        when(objectFactory.getInstance(Conversation.class)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);
        when(conversationRepository.saveAndFlush(conversation)).thenReturn(conversation);

        Conversation result = conversationService.createConversation(sender, receivers, conversationName, message);

        verify(conversation).setParticipants(receivers);
        verify(conversation).addParticipant(sender);
        verify(conversation).getParticipants();
        verify(conversation).setAdmin(sender);
        verify(conversation).setName(conversationName);
        verify(conversation).addMessage(message);
        verify(conversation).setLastMessage(message);
        verify(message).setConversation(conversation);
        verify(conversationRepository).saveAndFlush(conversation);
        verifyNoMoreInteractions(conversation, sender, receiver, message, conversationRepository);

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

        Conversation result = conversationService.getConversation(conversationId);

        verify(conversationRepository).findById(conversationId);
        verifyNoMoreInteractions(conversationRepository);

        assertEquals("A conversation with given id is retrieved", result, null);
    }


    @Test
    public void testAddMessage() {
        Set<Participant> receivers = new LinkedHashSet<>();
        receivers.add(receiver);
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        Conversation result = conversationService.addMessage(conversation, message);

        verify(conversation).addMessage(message);
        verify(conversation).setLastMessage(message);
        verify(conversationRepository).save(conversation);
        verifyNoMoreInteractions(conversation, conversationRepository, sender, receiver);

        assertEquals("Updated conversation is returned as a successful result", result, conversation);
    }

    @Test
    public void testGetConversationsByParticipant() {
        int page = 0;
        int size = 20;
        Set<Participant> participants = new HashSet<>();
        participants.add(participant);
        List<Conversation> conversations = new ArrayList<>();
        conversations.add(conversation);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "lastMessage.createdDate");

        when(conversationRepository.findByParticipantsIn(participants, pageable)).thenReturn(conversations);

        List<Conversation> result = conversationService.getConversationsByParticipant(participant, page, size);

        verify(conversationRepository).findByParticipantsIn(participants, pageable);
        verifyNoMoreInteractions(conversationRepository);

        assertEquals("A list of one conversation is returned", result, conversations);
    }
}
