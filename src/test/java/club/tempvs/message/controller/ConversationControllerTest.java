package club.tempvs.message.controller;

import club.tempvs.message.controller.ConversationController;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.CreateConversationDto;
import club.tempvs.message.dto.GetConversationDto;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.service.ParticipantService;
import club.tempvs.message.util.ObjectFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class ConversationControllerTest {

    private ConversationController conversationController;

    @Mock
    private ConversationService conversationService;

    @Mock
    private ParticipantService participantService;

    @Mock
    private ObjectFactory objectFactory;

    @Mock
    private CreateConversationDto createConversationDto;

    @Mock
    private Participant sender;

    @Mock
    private Participant receiver;

    @Mock
    private Conversation conversation;

    @Mock
    private GetConversationDto getConversationDto;

    @Before
    public void setup() {
        conversationController = new ConversationController(objectFactory, conversationService, participantService);
    }

    @Test
    public void testCreateConversation() {
        String text = "text";
        String name = "name";
        Set<Long> receiverIds = new HashSet<>();
        receiverIds.add(2L);
        Set<Participant> receivers = new HashSet<>();
        receivers.add(receiver);

        when(createConversationDto.getSender()).thenReturn(1L);
        when(participantService.getParticipant(1L)).thenReturn(sender);
        when(participantService.getParticipant(2L)).thenReturn(receiver);
        when(createConversationDto.getReceivers()).thenReturn(receiverIds);
        when(createConversationDto.getText()).thenReturn(text);
        when(createConversationDto.getName()).thenReturn(name);
        when(conversationService.createConversation(sender, receivers, text, name)).thenReturn(conversation);
        when(objectFactory.getInstance(GetConversationDto.class, conversation)).thenReturn(getConversationDto);

        GetConversationDto result = conversationController.createConversation(createConversationDto);

        verify(createConversationDto).getSender();
        verify(participantService).getParticipant(1L);
        verify(participantService).getParticipant(2L);
        verify(createConversationDto).getReceivers();
        verify(createConversationDto).getText();
        verify(createConversationDto).getName();
        verify(conversationService).createConversation(sender, receivers, text, name);
        verify(objectFactory).getInstance(GetConversationDto.class, conversation);
        verifyNoMoreInteractions(createConversationDto);
        verifyNoMoreInteractions(participantService);
        verifyNoMoreInteractions(conversationService);

        assertEquals("Result is a conversation", result, getConversationDto);
    }
}
