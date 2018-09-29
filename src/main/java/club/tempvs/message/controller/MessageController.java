package club.tempvs.message.controller;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.AddMessageDto;
import club.tempvs.message.dto.SuccessDto;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.service.MessageService;
import club.tempvs.message.service.ParticipantService;
import club.tempvs.message.util.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api")
public class MessageController {

    private final ObjectFactory objectFactory;
    private final ConversationService conversationService;
    private final ParticipantService participantService;
    private final MessageService messageService;

    @Autowired
    public MessageController(ObjectFactory objectFactory,
                             ConversationService conversationService,
                             ParticipantService participantService,
                             MessageService messageService) {
        this.objectFactory = objectFactory;
        this.conversationService = conversationService;
        this.participantService = participantService;
        this.messageService = messageService;
    }

    @RequestMapping("/ping")
    public String getPong() {
        return "pong!";
    }


    @RequestMapping(value="/message", method = POST,
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public SuccessDto addMessage(@RequestBody AddMessageDto addMessageDto) {
        addMessageDto.validate();
        Long senderId = addMessageDto.getSender();
        Long conversationId = addMessageDto.getConversation();
        String text = addMessageDto.getText();
        Boolean isSystem = addMessageDto.getSystem();

        Participant sender = participantService.getParticipant(senderId);
        Conversation conversation = conversationService.getConversation(conversationId);

        if (conversation == null) {
            throw new IllegalArgumentException("Conversation with id " + conversationId + " doesn't exist.");
        }

        Set<Participant> participants = conversation.getParticipants();
        Message message = messageService.createMessage(conversation, sender, participants, text, isSystem);
        Conversation updatedConversation = conversationService.addMessage(conversation, message);

        return objectFactory.getInstance(SuccessDto.class, updatedConversation != null);
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String processValidationError(Exception ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String processIllegalArgumentException(IllegalArgumentException ex) {
        return ex.getMessage();
    }
}
