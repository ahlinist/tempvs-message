package club.tempvs.message.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.AddMessageDto;
import club.tempvs.message.dto.SuccessDto;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.service.MessageService;
import club.tempvs.message.service.ParticipantService;
import club.tempvs.message.util.ObjectFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class MessageControllerTest {

    private MessageController messageController;

    @Mock
    private AddMessageDto addMessageDto;
    @Mock
    private SuccessDto successDto;
    @Mock
    private Participant sender;
    @Mock
    private Participant receiver;
    @Mock
    private ConversationService conversationService;
    @Mock
    private ParticipantService participantService;
    @Mock
    private Conversation conversation;
    @Mock
    private MessageService messageService;
    @Mock
    private Message message;
    @Mock
    private ObjectFactory objectFactory;

    @Before
    public void setup() {
        messageController = new MessageController(objectFactory, conversationService, participantService, messageService);
    }

    @Test
    public void testGetPong() {
        assertEquals("getPong() method returns 'pong!' string", "pong!", messageController.getPong());
    }

    @Test
    public void testAddMessage() {
        Long senderId = 1L;
        Long conversationId = 2L;
        Set<Participant> participants = new HashSet<>();
        participants.add(receiver);
        String text = "new message text";
        Boolean isSystem = Boolean.FALSE;

        when(addMessageDto.getSender()).thenReturn(senderId);
        when(addMessageDto.getConversation()).thenReturn(conversationId);
        when(addMessageDto.getText()).thenReturn(text);
        when(addMessageDto.getSystem()).thenReturn(isSystem);
        when(participantService.getParticipant(senderId)).thenReturn(sender);
        when(conversationService.getConversation(conversationId)).thenReturn(conversation);
        when(conversation.getParticipants()).thenReturn(participants);
        when(messageService.createMessage(conversation, sender, participants, text, isSystem)).thenReturn(message);
        when(conversationService.addMessage(conversation, message)).thenReturn(conversation);
        when(objectFactory.getInstance(SuccessDto.class, Boolean.TRUE)).thenReturn(successDto);

        SuccessDto result = messageController.addMessage(addMessageDto);

        verify(addMessageDto).validate();
        verify(addMessageDto).getSender();
        verify(addMessageDto).getConversation();
        verify(addMessageDto).getText();
        verify(addMessageDto).getSystem();
        verify(participantService).getParticipant(senderId);
        verify(conversationService).getConversation(conversationId);
        verify(conversation).getParticipants();
        verify(messageService).createMessage(conversation, sender, participants, text, isSystem);
        verify(conversationService).addMessage(conversation, message);
        verify(objectFactory).getInstance(SuccessDto.class, Boolean.TRUE);
        verifyNoMoreInteractions(
                addMessageDto, participantService, conversationService, conversation, messageService, objectFactory);

        assertEquals("SuccessDto is returned as a result", successDto, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddMessageForMissingConversation() {
        Long senderId = 1L;
        Long conversationId = 2L;
        Set<Participant> participants = new HashSet<>();
        participants.add(receiver);
        String text = "new message text";
        Boolean isSystem = Boolean.FALSE;

        when(addMessageDto.getSender()).thenReturn(senderId);
        when(addMessageDto.getConversation()).thenReturn(conversationId);
        when(addMessageDto.getText()).thenReturn(text);
        when(addMessageDto.getSystem()).thenReturn(isSystem);
        when(participantService.getParticipant(senderId)).thenReturn(sender);
        when(conversationService.getConversation(conversationId)).thenReturn(null);

        messageController.addMessage(addMessageDto);

        verify(addMessageDto).validate();
        verify(addMessageDto).getSender();
        verify(addMessageDto).getConversation();
        verify(addMessageDto).getText();
        verify(addMessageDto).getSystem();
        verify(participantService).getParticipant(senderId);
        verify(conversationService).getConversation(conversationId);
        verifyNoMoreInteractions(
                addMessageDto, participantService, conversationService, conversation, messageService, objectFactory);
    }
}
