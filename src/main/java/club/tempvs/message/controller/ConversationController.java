package club.tempvs.message.controller;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.*;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.service.MessageService;
import club.tempvs.message.service.ParticipantService;
import club.tempvs.message.util.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static java.util.stream.Collectors.*;

@RestController
@RequestMapping("/api")
public class ConversationController {

    private static final int DEFAULT_PAGE_NUMBER = 0;
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

    @RequestMapping("/ping")
    public String getPong() {
        return "pong!";
    }

    @RequestMapping(value="/conversations", method = POST,
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public GetConversationDto createConversation(@RequestBody CreateConversationDto createConversationDto) {
        createConversationDto.validate();
        Participant author = participantService.getParticipant(createConversationDto.getAuthor());
        Set<Participant> receivers = createConversationDto.getReceivers().stream()
                .map(participantService::getParticipant).collect(toSet());
        String text = createConversationDto.getText();
        String name = createConversationDto.getName();
        boolean isSystem = false;
        Message message = messageService.createMessage(author, receivers, text, isSystem);
        Conversation conversation = conversationService.createConversation(author, receivers, name, message);
        return objectFactory.getInstance(GetConversationDto.class, conversation, conversation.getMessages());
    }

    @RequestMapping(value="/conversations/{id}", method = GET, produces = APPLICATION_JSON_VALUE)
    public GetConversationDto getConversation(
            @PathVariable("id") Long id,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must not be larger than " + MAX_PAGE_SIZE + "!");
        }

        Conversation conversation = conversationService.getConversation(id);
        List<Message> messages = messageService.getMessagesFromConversation(conversation, page, size);
        return objectFactory.getInstance(GetConversationDto.class, conversation, messages);
    }

    @RequestMapping(value="/conversations", method = GET, produces = APPLICATION_JSON_VALUE)
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

    @RequestMapping(value="/conversations/{conversationId}/messages", method = POST,
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity addMessage(
            @PathVariable("conversationId") Long conversationId,
            @RequestBody AddMessageDto addMessageDto) {
        addMessageDto.validate();
        Long authorId = addMessageDto.getAuthor();
        String text = addMessageDto.getText();
        Boolean isSystem = addMessageDto.getSystem();

        Conversation conversation = conversationService.getConversation(conversationId);

        if (conversation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Conversation with id " + conversationId + " doesn't exist.");
        }

        Participant author = participantService.getParticipant(authorId);
        Set<Participant> participants = conversation.getParticipants();
        Message message = messageService.createMessage(conversation, author, participants, text, isSystem);
        conversationService.addMessage(conversation, message);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value="/conversations/{conversationId}/participants", method = PATCH,
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public GetConversationDto updateParticipants(
            @PathVariable("conversationId") Long conversationId,
            @RequestBody UpdateParticipantsDto updateParticipantsDto) {
        updateParticipantsDto.validate();
        Long initiatorId = updateParticipantsDto.getInitiator();
        Long subjectId = updateParticipantsDto.getSubject();
        UpdateParticipantsDto.Action action = updateParticipantsDto.getAction();
        Conversation conversation = conversationService.getConversation(conversationId);

        if (conversation == null) {
            throw new IllegalStateException("Conversation with id '" + conversationId + "' has not been found.");
        }

        Participant initiator = participantService.getParticipant(initiatorId);
        Participant subject = participantService.getParticipant(subjectId);

        Conversation result;

        if (action == UpdateParticipantsDto.Action.ADD) {
            result = conversationService.addParticipant(conversation, initiator, subject);
        } else if (action == UpdateParticipantsDto.Action.REMOVE) {
            result = conversationService.removeParticipant(conversation, initiator, subject);
        } else {
            throw new RuntimeException("Action '" + action + "' is not supported.");
        }

        List<Message> messages = messageService.getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        return objectFactory.getInstance(GetConversationDto.class, result, messages);
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

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String processIllegalArgumentException(IllegalStateException ex) {
        return ex.getMessage();
    }
}
