package club.tempvs.message.controller;

import club.tempvs.message.api.*;
import club.tempvs.message.domain.*;
import club.tempvs.message.dto.*;
import club.tempvs.message.service.*;
import club.tempvs.message.util.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;
import static java.util.stream.Collectors.*;

@RestController
@RequestMapping("/api")
public class ConversationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversationController.class);

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int MAX_PAGE_SIZE = 40;
    private static final String COUNT_HEADER = "X-Total-Count";
    private static final String PROFILE_HEADER = "Profile";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final ObjectFactory objectFactory;
    private final ConversationService conversationService;
    private final ParticipantService participantService;
    private final MessageService messageService;
    private final AuthHelper authHelper;
    private final LocaleHelper localeHelper;

    @Autowired
    public ConversationController(ObjectFactory objectFactory,
                                  ConversationService conversationService,
                                  ParticipantService participantService,
                                  MessageService messageService,
                                  AuthHelper authHelper,
                                  LocaleHelper localeHelper) {
        this.objectFactory = objectFactory;
        this.conversationService = conversationService;
        this.participantService = participantService;
        this.messageService = messageService;
        this.authHelper = authHelper;
        this.localeHelper = localeHelper;
    }

    @GetMapping("/ping")
    public String getPong() {
        return "pong!";
    }

    @PostMapping("/conversations")
    public ResponseEntity createConversation(
            @RequestHeader(value = PROFILE_HEADER, required = false) Long authorId,
            @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String token,
            @RequestHeader(value = "Accept-Language", required = false) String lang,
            @RequestHeader(value = "Accept-Timezone", required = false, defaultValue = "UTC") String timeZone,
            @RequestBody CreateConversationDto createConversationDto) {
        authHelper.authenticate(token);
        localeHelper.getLocale(lang);

        if (authorId == null) {
            throw new IllegalStateException("Author is not specified");
        }

        Participant author = participantService.getParticipant(authorId);

        if (author == null) {
            throw new IllegalStateException("Participant with id " + authorId + " does not exist in the database");
        }

        Set<Participant> receivers = new HashSet<>();
        Set<Long> receiverIds = createConversationDto.getReceivers();

        if (receiverIds != null && !receiverIds.isEmpty()) {
            receivers = receiverIds.stream()
                    //TODO: implement and use bulk participant retrieval method
                    .map(participantService::getParticipant)
                    .collect(toSet());
        }

        Message message = messageService.createMessage(author, receivers, createConversationDto.getText(), false, null, null);
        Conversation conversation = conversationService.createConversation(author, receivers, createConversationDto.getName(), message);
        List<Message> messages = messageService.getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        GetConversationDto result = objectFactory.getInstance(GetConversationDto.class, conversation, messages, author, timeZone);
        HttpHeaders headers = objectFactory.getInstance(HttpHeaders.class);
        headers.add(PROFILE_HEADER, String.valueOf(authorId));
        return ResponseEntity.status(HttpStatus.OK.value()).headers(headers).body(result);
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity getConversation(
            @RequestHeader(value = PROFILE_HEADER, required = false) Long callerId,
            @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String token,
            @RequestHeader(value = "Accept-Language", required = false) String lang,
            @RequestHeader(value = "Accept-Timezone", required = false, defaultValue = "UTC") String timeZone,
            @PathVariable("conversationId") Long conversationId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "40") int size) {
        authHelper.authenticate(token);
        localeHelper.getLocale(lang);

        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must not be larger than " + MAX_PAGE_SIZE + "!");
        }

        if (callerId == null) {
            throw new IllegalStateException("'caller' parameter is missing.");
        }

        Participant caller = participantService.getParticipant(callerId);

        if (caller == null) {
            throw new IllegalStateException("The caller specified does not exist.");
        }

        Conversation conversation = conversationService.getConversation(conversationId);

        if (!conversation.getParticipants().contains(caller)) {
            throw new ForbiddenException("Participant " + callerId + " has no access to conversation " + conversationId);
        }

        List<Message> messages = messageService.getMessagesFromConversation(conversation, page, size);
        GetConversationDto result = objectFactory.getInstance(GetConversationDto.class, conversation, messages, caller, timeZone);
        HttpHeaders headers = objectFactory.getInstance(HttpHeaders.class);
        headers.add(PROFILE_HEADER, String.valueOf(callerId));

        return ResponseEntity.ok().headers(headers).body(result);
    }

    @GetMapping("/conversations")
    public ResponseEntity getConversationsByParticipant(
            @RequestHeader(value = PROFILE_HEADER, required = false) Long participantId,
            @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String token,
            @RequestHeader(value = "Accept-Language", required = false) String lang,
            @RequestHeader(value = "Accept-Timezone", required = false, defaultValue = "UTC") String timeZone,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "40") int size) {
        authHelper.authenticate(token);
        Locale locale = localeHelper.getLocale(lang);

        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must not be larger than " + MAX_PAGE_SIZE + "!");
        }

        Participant participant = participantService.getParticipant(participantId);

        if (participant == null) {
            throw new IllegalStateException("No participant with id " + participantId + " exist!");
        }

        List<Conversation> conversations = conversationService.getConversationsByParticipant(participant, page, size);
        GetConversationsDto result = objectFactory.getInstance(GetConversationsDto.class, conversations, participant, timeZone, locale);

        int conversationsCount = result.getConversations().size();
        HttpHeaders headers = objectFactory.getInstance(HttpHeaders.class);
        headers.add(COUNT_HEADER, String.valueOf(conversationsCount));
        headers.add(PROFILE_HEADER, String.valueOf(participantId));

        return ResponseEntity.status(HttpStatus.OK.value()).headers(headers).body(result);
    }

    @RequestMapping(value="/conversations", method = HEAD)
    public ResponseEntity countConversations(
            @RequestHeader(value = PROFILE_HEADER, required = false) Long participantId,
            @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String token) {
        authHelper.authenticate(token);
        Participant participant = participantService.getParticipant(participantId);

        if (participant == null) {
            throw new IllegalStateException("No participant with id " + participantId + " found.");
        }

        long result = conversationService.countUpdatedConversationsPerParticipant(participant);
        HttpHeaders headers = objectFactory.getInstance(HttpHeaders.class);
        headers.add(COUNT_HEADER, String.valueOf(result));
        headers.add(PROFILE_HEADER, String.valueOf(participantId));

        return ResponseEntity.ok().headers(headers).build();
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity addMessage(
            @RequestHeader(value = PROFILE_HEADER, required = false) Long authorId,
            @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String token,
            @RequestHeader(value = "Accept-Language", required = false) String lang,
            @RequestHeader(value = "Accept-Timezone", required = false, defaultValue = "UTC") String timeZone,
            @PathVariable("conversationId") Long conversationId,
            @RequestBody AddMessageDto addMessageDto) {
        authHelper.authenticate(token);
        localeHelper.getLocale(lang);
        String text = addMessageDto.getText();

        Conversation conversation = conversationService.getConversation(conversationId);

        if (conversation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Conversation with id " + conversationId + " doesn't exist.");
        }

        Participant author = participantService.getParticipant(authorId);
        Set<Participant> receivers = new HashSet<>(conversation.getParticipants());
        receivers.remove(author);
        Message message = messageService.createMessage(author, receivers, text, false, null, null);
        Conversation updatedConversation = conversationService.addMessage(conversation, message);
        List<Message> messages = messageService.getMessagesFromConversation(updatedConversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        GetConversationDto result = objectFactory.getInstance(
                GetConversationDto.class, updatedConversation, messages, author, timeZone);
        HttpHeaders headers = objectFactory.getInstance(HttpHeaders.class);
        headers.add(PROFILE_HEADER, String.valueOf(authorId));

        return ResponseEntity.ok().headers(headers).body(result);
    }

    @PostMapping("/conversations/{conversationId}/participants")
    public ResponseEntity addParticipants(
            @RequestHeader(value = PROFILE_HEADER, required = false) Long initiatorId,
            @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String token,
            @RequestHeader(value = "Accept-Language", required = false) String lang,
            @RequestHeader(value = "Accept-Timezone", required = false, defaultValue = "UTC") String timeZone,
            @PathVariable("conversationId") Long conversationId,
            @RequestBody AddParticipantsDto addParticipantsDto) {
        authHelper.authenticate(token);
        localeHelper.getLocale(lang);
        Conversation conversation = conversationService.getConversation(conversationId);

        if (conversation == null) {
            throw new NotFoundException("Conversation with id '" + conversationId + "' has not been found.");
        }

        Set<ParticipantDto> subjectDtos = addParticipantsDto.getSubjects();

        if (initiatorId == null) {
            throw new IllegalStateException("Initiator is not specified");
        }

        Participant initiator = participantService.getParticipant(initiatorId);

        if (initiator == null) {
            throw new IllegalStateException("Participant with id " + initiatorId + " does not exist");
        }

        Set<Participant> subjects = subjectDtos.stream()
                //TODO: implement "participantService#getParticipants()" for bulk retrieval
                .map(dto -> participantService.getParticipant(dto.getId()))
                .filter(Objects::nonNull)
                .collect(toSet());

        if (subjects == null || subjects.isEmpty()) {
            throw new IllegalStateException("No subjects found in database");
        }

        Set<Participant> participants = conversation.getParticipants();

        if (participants.stream().filter(subjects::contains).findAny().isPresent()) {
            throw new IllegalStateException("An existent member is being added to a conversation.");
        }

        Conversation updatedConversation = conversationService.addParticipants(conversation, initiator, subjects);
        List<Message> messages = messageService.getMessagesFromConversation(updatedConversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        GetConversationDto result = objectFactory.getInstance(GetConversationDto.class, updatedConversation, messages, initiator, timeZone);
        HttpHeaders headers = objectFactory.getInstance(HttpHeaders.class);
        headers.add(PROFILE_HEADER, String.valueOf(initiatorId));
        return ResponseEntity.ok().headers(headers).body(result);
    }

    @DeleteMapping("/conversations/{conversationId}/participants/{subjectId}")
    public GetConversationDto removeParticipant(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "Accept-Language", required = false) String lang,
            @RequestHeader(value = "Accept-Timezone", required = false, defaultValue = "UTC") String timeZone,
            @PathVariable("conversationId") Long conversationId,
            @PathVariable("subjectId") Long subjectId,
            @RequestParam("initiator") Long initiatorId) {
        authHelper.authenticate(token);
        Locale locale = localeHelper.getLocale(lang);
        Conversation conversation = conversationService.getConversation(conversationId);

        if (conversation == null) {
            throw new NotFoundException("Conversation with id '" + conversationId + "' has not been found.");
        }

        Participant initiator = participantService.getParticipant(initiatorId);

        if (initiator == null) {
            throw new IllegalStateException("Participant with id " + initiatorId + " does not exist");
        }

        Participant subject = participantService.getParticipant(subjectId);

        if (subject == null) {
            throw new IllegalStateException("Participant with id " + subjectId + " does not exist");
        }

        Conversation result = conversationService.removeParticipant(conversation, initiator, subject);
        List<Message> messages = messageService.getMessagesFromConversation(result, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        return objectFactory.getInstance(GetConversationDto.class, result, messages, initiator, timeZone);
    }

    @PostMapping("/conversations/{conversationId}/name")
    public GetConversationDto updateConversationName(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "Accept-Language", required = false) String lang,
            @RequestHeader(value = "Accept-Timezone", required = false, defaultValue = "UTC") String timeZone,
            @PathVariable("conversationId") Long conversationId,
            @RequestBody UpdateConversationNameDto updateConversationNameDto) {
        authHelper.authenticate(token);
        updateConversationNameDto.validate();
        Locale locale = localeHelper.getLocale(lang);
        ParticipantDto participantDto = updateConversationNameDto.getInitiator();
        Participant initiator = participantService.getParticipant(participantDto.getId());
        Conversation conversation = conversationService.getConversation(conversationId);
        Conversation result = conversationService.updateName(conversation, initiator, updateConversationNameDto.getName());

        List<Message> messages = messageService.getMessagesFromConversation(result, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        return objectFactory.getInstance(GetConversationDto.class, result, messages, initiator, timeZone);
    }

    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity readMessages(
            @PathVariable("conversationId") Long conversationId,
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody ReadMessagesDto readMessagesDto) {
        authHelper.authenticate(token);
        readMessagesDto.validate();
        Conversation conversation = conversationService.getConversation(conversationId);

        if (conversation == null) {
            throw new NotFoundException("No conversation with id " + conversationId + " found.");
        }

        ParticipantDto participantDto = readMessagesDto.getParticipant();
        Long participantId = participantDto.getId();
        Participant participant = participantService.getParticipant(participantId);

        if (participant == null) {
            throw new IllegalStateException("No participant with id " + participantId + " found.");
        }

        List<Message> messages = messageService.findMessagesByIds(readMessagesDto.getMessageIds());

        if (messages.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("Some of the messages specified were not found.");
        }

        messageService.markAsRead(conversation, participant, messages);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String returnInternalError(Exception e) {
        return processException(e);
    }

    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String returnBadRequest(Exception e) {
        return processException(e);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String returnNotFound(NotFoundException e) {
        return processException(e);
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String returnUnauthorized(UnauthorizedException e) {
        return processException(e);
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String returnForbidden(ForbiddenException e) {
        return processException(e);
    }

    private String processException(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTraceString = sw.toString();
        LOGGER.error(stackTraceString);
        return e.getMessage();
    }
}
