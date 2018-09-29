package club.tempvs.message.controller;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.CreateConversationDto;
import club.tempvs.message.dto.GetConversationDto;
import club.tempvs.message.dto.GetConversationsDto;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.service.MessageService;
import club.tempvs.message.service.ParticipantService;
import club.tempvs.message.util.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static java.util.stream.Collectors.*;

@RestController
@RequestMapping("/api")
public class ConversationController {

    private static final int MAX_PAGE_SIZE = 20;

    private final ObjectFactory objectFactory;
    private final ConversationService conversationService;
    private final ParticipantService participantService;
    private final MessageService messageService;

    @Autowired
    public ConversationController(ObjectFactory objectFactory, ConversationService conversationService,
                                  ParticipantService participantService, MessageService messageService) {
        this.objectFactory = objectFactory;
        this.conversationService = conversationService;
        this.participantService = participantService;
        this.messageService = messageService;
    }

    @RequestMapping(value="/conversation", method = POST,
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public GetConversationDto createConversation(@RequestBody CreateConversationDto createConversationDto) {
        Participant sender = participantService.getParticipant(createConversationDto.getSender());
        Set<Participant> receivers = createConversationDto.getReceivers().stream()
                .map(participantService::getParticipant).collect(toSet());
        String text = createConversationDto.getText();
        String name = createConversationDto.getName();
        boolean isSystem = false;
        Message message = messageService.createMessage(sender, receivers, text, isSystem);
        Conversation conversation = conversationService.createConversation(sender, receivers, name, message);
        return objectFactory.getInstance(GetConversationDto.class, conversation);
    }

    @RequestMapping(value="/conversation/{id}", method = GET, produces = APPLICATION_JSON_VALUE)
    public GetConversationDto getConversation(@PathVariable("id") Long id) {
        Conversation conversation = conversationService.getConversation(id);
        return objectFactory.getInstance(GetConversationDto.class, conversation);
    }

    @RequestMapping(value="/conversation", method = GET, produces = APPLICATION_JSON_VALUE)
    public GetConversationsDto getConversationsByParticipant(
            @RequestParam("participant") Long participantId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must not be larger than " + MAX_PAGE_SIZE + "!");
        }

        Participant participant = participantService.getParticipant(participantId);
        List<Conversation> conversations = conversationService.getConversationsByParticipant(participant, page, size);
        return objectFactory.getInstance(GetConversationsDto.class, conversations);
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
