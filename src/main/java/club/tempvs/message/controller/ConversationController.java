package club.tempvs.message.controller;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.CreateConversationDto;
import club.tempvs.message.dto.GetConversationDto;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.service.ParticipantService;
import club.tempvs.message.util.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static java.util.stream.Collectors.*;

@RestController
@RequestMapping("/api")
public class ConversationController {

    private final ObjectFactory objectFactory;
    private final ConversationService conversationService;
    private final ParticipantService participantService;

    @Autowired
    public ConversationController(ObjectFactory objectFactory, ConversationService conversationService,
                                  ParticipantService participantService) {
        this.objectFactory = objectFactory;
        this.conversationService = conversationService;
        this.participantService = participantService;
    }

    @RequestMapping(value="/conversation", method = POST,
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    GetConversationDto createConversation(@RequestBody CreateConversationDto createConversationDto) {
        Participant sender = participantService.getParticipant(createConversationDto.getSender());
        Set<Participant> receivers = createConversationDto.getReceivers().stream()
                .map(participantService::getParticipant).collect(toSet());
        String text = createConversationDto.getText();
        String name = createConversationDto.getName();
        Conversation conversation = conversationService.createConversation(sender, receivers, text, name);
        GetConversationDto getConversationDto = objectFactory.getInstance(GetConversationDto.class, conversation);
        return getConversationDto;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String processValidationError(Exception ex) {
        return ex.getMessage();
    }
}
