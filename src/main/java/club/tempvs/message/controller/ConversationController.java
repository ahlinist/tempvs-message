package club.tempvs.message.controller;

import club.tempvs.message.api.*;
import club.tempvs.message.domain.*;
import club.tempvs.message.dto.*;
import club.tempvs.message.service.*;
import club.tempvs.message.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static java.util.stream.Collectors.*;

@RestController
@RequestMapping("/api")
public class ConversationController {

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int MAX_PAGE_SIZE = 20;
    private static final String COUNT_HEADER = "X-Total-Count";

    private final ObjectFactory objectFactory;
    private final ConversationService conversationService;
    private final ParticipantService participantService;
    private final MessageService messageService;
    private final AuthHelper authHelper;

    @Autowired
    public ConversationController(ObjectFactory objectFactory,
                                  ConversationService conversationService,
                                  ParticipantService participantService,
                                  MessageService messageService,
                                  AuthHelper authHelper) {
        this.objectFactory = objectFactory;
        this.conversationService = conversationService;
        this.participantService = participantService;
        this.messageService = messageService;
        this.authHelper = authHelper;
    }

    @GetMapping("/ping")
    public String getPong() {
        return "pong!";
    }

    @PostMapping(value="/conversations", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public GetConversationDto createConversation(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody CreateConversationDto createConversationDto) {
        authHelper.authenticate(token);
        createConversationDto.validate();
        Participant author = participantService.getParticipant(createConversationDto.getAuthor().getId());
        Set<Participant> receivers = createConversationDto.getReceivers().stream()
                .map(participantDto -> participantService.getParticipant(participantDto.getId()))
                .collect(toSet());
        String text = createConversationDto.getText();
        String name = createConversationDto.getName();
        Message message = messageService.createMessage(author, receivers, text);
        Set<Participant> participants = new HashSet<>();
        participants.add(author);
        participants.addAll(receivers);
        Conversation conversation = null;
        List<Message> messages = null;

        if (participants.size() < 2) {
            throw new BadRequestException("Conversation must contain at least 2 participants.");
        } else if (participants.size() == 2) {
            conversation = conversationService.findDialogue(author, receivers.iterator().next());

            if (conversation != null) {
                conversation = conversationService.addMessage(conversation, message);
                messages = messageService.getMessagesFromConversation(conversation);
            }
        }

        if (conversation == null) {
            conversation = conversationService.createConversation(author, receivers, name, message);
            messages = conversation.getMessages();
        }

        return objectFactory.getInstance(GetConversationDto.class, conversation, messages, author);
    }

    @GetMapping(value="/conversations/{id}", produces = APPLICATION_JSON_VALUE)
    public GetConversationDto getConversation(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable("id") Long id,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
            @RequestParam(value = "caller", required = false) Long callerId) {
        authHelper.authenticate(token);

        if (size > MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be larger than " + MAX_PAGE_SIZE + "!");
        }

        if (callerId == null) {
            throw new BadRequestException("'caller' parameter is missing.");
        }

        Participant caller = participantService.getParticipant(callerId);

        if (caller == null) {
            throw new BadRequestException("The caller specified does not exist.");
        }

        Conversation conversation = conversationService.getConversation(id);

        if (!conversation.getParticipants().contains(caller)) {
            throw new ForbiddenException("Participant " + callerId + " has no access to conversation " + id);
        }

        List<Message> messages = messageService.getMessagesFromConversation(conversation, page, size);
        return objectFactory.getInstance(GetConversationDto.class, conversation, messages, caller);
    }

    @GetMapping(value="/conversations", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getConversationsByParticipant(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam("participant") Long participantId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        authHelper.authenticate(token);

        if (size > MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be larger than " + MAX_PAGE_SIZE + "!");
        }

        Participant participant = participantService.getParticipant(participantId);
        List<Conversation> conversations = conversationService.getConversationsByParticipant(participant, page, size);
        GetConversationsDto result = objectFactory.getInstance(GetConversationsDto.class, conversations, participant);

        int conversationsCount = result.getConversations().size();
        HttpHeaders headers = objectFactory.getInstance(HttpHeaders.class);
        headers.add(COUNT_HEADER, String.valueOf(conversationsCount));

        return ResponseEntity.status(HttpStatus.OK.value()).headers(headers).body(result);
    }

    @RequestMapping(value="/conversations", method = HEAD)
    public ResponseEntity countConversations(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam("participant") Long participantId) {
        authHelper.authenticate(token);
        Participant participant = participantService.getParticipant(participantId);

        if (participant == null) {
            throw new BadRequestException("No participant with id " + participantId + " found.");
        }

        long result = conversationService.countUpdatedConversationsPerParticipant(participant);
        HttpHeaders headers = objectFactory.getInstance(HttpHeaders.class);
        headers.add(COUNT_HEADER, String.valueOf(result));

        return ResponseEntity.ok().headers(headers).build();
    }

    @PostMapping(value="/conversations/{conversationId}/messages", consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity addMessage(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable("conversationId") Long conversationId,
            @RequestBody AddMessageDto addMessageDto) {
        authHelper.authenticate(token);
        addMessageDto.validate();
        Long authorId = addMessageDto.getAuthor().getId();
        String text = addMessageDto.getText();

        Conversation conversation = conversationService.getConversation(conversationId);

        if (conversation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Conversation with id " + conversationId + " doesn't exist.");
        }

        Participant author = participantService.getParticipant(authorId);
        Set<Participant> receivers = new HashSet<>(conversation.getParticipants());
        receivers.remove(author);
        Message message = messageService.createMessage(author, receivers, text);
        Conversation updatedConversation = conversationService.addMessage(conversation, message);
        List<Message> messages = messageService.getMessagesFromConversation(updatedConversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        GetConversationDto getConversationDto = objectFactory.getInstance(
                GetConversationDto.class, updatedConversation, messages, author);

        return ResponseEntity.ok().body(getConversationDto);
    }

    @PatchMapping(value="/conversations/{conversationId}/participants", consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public GetConversationDto updateParticipants(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable("conversationId") Long conversationId,
            @RequestBody UpdateParticipantsDto updateParticipantsDto) {
        authHelper.authenticate(token);
        updateParticipantsDto.validate();
        Conversation conversation = conversationService.getConversation(conversationId);

        if (conversation == null) {
            throw new NotFoundException("Conversation with id '" + conversationId + "' has not been found.");
        }

        Long initiatorId = updateParticipantsDto.getInitiator().getId();
        Participant initiator = participantService.getParticipant(initiatorId);

        if (initiator == null) {
            throw new BadRequestException("Participant with id " + initiatorId + " does not exist");
        }

        Long subjectId = updateParticipantsDto.getSubject().getId();
        Participant subject = participantService.getParticipant(subjectId);

        if (subject == null) {
            throw new BadRequestException("Participant with id " + subjectId + " does not exist");
        }

        Conversation result;
        UpdateParticipantsDto.Action action = updateParticipantsDto.getAction();

        if (action == UpdateParticipantsDto.Action.ADD) {
            result = conversationService.addParticipant(conversation, initiator, subject);
        } else if (action == UpdateParticipantsDto.Action.REMOVE) {
            result = conversationService.removeParticipant(conversation, initiator, subject);
        } else {
            throw new RuntimeException("Action '" + action + "' is not supported.");
        }

        List<Message> messages = messageService.getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        return objectFactory.getInstance(GetConversationDto.class, result, messages, initiator);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String processValidationError(Exception ex) {
        return ex.getMessage();
    }

    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String returnBadRequest(Exception ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String returnNotFound(NotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String returnUnauthorized(UnauthorizedException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String returnForbidden(ForbiddenException ex) {
        return ex.getMessage();
    }
}
