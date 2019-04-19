package club.tempvs.message.controller;

import club.tempvs.message.api.*;
import club.tempvs.message.domain.*;
import club.tempvs.message.dto.*;
import club.tempvs.message.service.*;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ConversationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversationController.class);

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int MAX_PAGE_SIZE = 40;
    private static final String COUNT_HEADER = "X-Total-Count";
    private static final String USER_INFO_HEADER = "User-Info";
    private static final String PAGE_PARAM = "page";
    private static final String SIZE_PARAM = "size";
    private static final String DEFAULT_PAGE_VALUE = "0";
    private static final String DEFAULT_SIZE_VALUE = "40";

    private final ConversationService conversationService;
    private final ParticipantService participantService;
    private final MessageService messageService;

    @PostMapping("/conversations")
    public ResponseEntity createConversation(
            @RequestHeader(value = USER_INFO_HEADER) UserInfoDto userInfoDto,
            @RequestBody CreateConversationDto createConversationDto) {
        Long authorId = userInfoDto.getProfileId();
        Participant author = participantService.getParticipant(authorId);
        Set<Participant> receivers = new HashSet<>();
        Set<Long> receiverIds = createConversationDto.getReceivers();

        if (receiverIds != null && !receiverIds.isEmpty()) {
            receivers = participantService.getParticipants(receiverIds);
        }

        Message message = messageService.createMessage(author, receivers, createConversationDto.getText(), false, null, null);
        Conversation conversation = conversationService.createConversation(author, receivers, createConversationDto.getName(), message);
        List<Message> messages = messageService.getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        GetConversationDto result = new GetConversationDto(conversation, messages, author, userInfoDto.getTimezone());
        HttpHeaders headers = new HttpHeaders();
        return ResponseEntity.ok().headers(headers).body(result);
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity getConversation(
            @RequestHeader(value = USER_INFO_HEADER) UserInfoDto userInfoDto,
            @PathVariable("conversationId") Long conversationId,
            @RequestParam(value = PAGE_PARAM, required = false, defaultValue = DEFAULT_PAGE_VALUE) int page,
            @RequestParam(value = SIZE_PARAM, required = false, defaultValue = DEFAULT_SIZE_VALUE) int size) {
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must not be larger than " + MAX_PAGE_SIZE + "!");
        }

        Long callerId = userInfoDto.getProfileId();
        Participant caller = participantService.getParticipant(callerId);
        Conversation conversation = conversationService.getConversation(conversationId);

        if (!conversation.getParticipants().contains(caller)) {
            throw new ForbiddenException("Participant " + callerId + " has no access to conversation " + conversationId);
        }

        List<Message> messages = messageService.getMessagesFromConversation(conversation, page, size);
        GetConversationDto result = new GetConversationDto(conversation, messages, caller, userInfoDto.getTimezone());
        HttpHeaders headers = new HttpHeaders();
        return ResponseEntity.ok().headers(headers).body(result);
    }

    @GetMapping("/conversations")
    public ResponseEntity getConversationsByParticipant(
            @RequestHeader(value = USER_INFO_HEADER) UserInfoDto userInfoDto,
            @RequestParam(value = PAGE_PARAM, required = false, defaultValue = DEFAULT_PAGE_VALUE) int page,
            @RequestParam(value = SIZE_PARAM, required = false, defaultValue = DEFAULT_SIZE_VALUE) int size) {
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must not be larger than " + MAX_PAGE_SIZE + "!");
        }

        Long participantId = userInfoDto.getProfileId();
        Participant participant = participantService.getParticipant(participantId);
        List<Conversation> conversations = conversationService.getConversationsByParticipant(participant, page, size);
        GetConversationsDto result = new GetConversationsDto(conversations, participant, userInfoDto.getTimezone());

        int conversationsCount = result.getConversations().size();
        HttpHeaders headers = new HttpHeaders();
        headers.add(COUNT_HEADER, String.valueOf(conversationsCount));
        return ResponseEntity.ok().headers(headers).body(result);
    }

    @RequestMapping(value="/conversations", method = HEAD)
    public ResponseEntity countConversations(
            @RequestHeader(value = USER_INFO_HEADER) UserInfoDto userInfoDto) {
        Long participantId = userInfoDto.getProfileId();
        Participant participant = participantService.getParticipant(participantId);
        long result = conversationService.countUpdatedConversationsPerParticipant(participant);
        HttpHeaders headers = new HttpHeaders();
        headers.add(COUNT_HEADER, String.valueOf(result));
        return ResponseEntity.ok().headers(headers).build();
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity addMessage(
            @RequestHeader(value = USER_INFO_HEADER) UserInfoDto userInfoDto,
            @PathVariable("conversationId") Long conversationId,
            @RequestBody AddMessageDto addMessageDto) {
        String text = addMessageDto.getText();
        Conversation conversation = conversationService.getConversation(conversationId);

        if (conversation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Conversation with id " + conversationId + " doesn't exist.");
        }

        Long authorId = userInfoDto.getProfileId();
        Participant author = participantService.getParticipant(authorId);
        Set<Participant> receivers = new HashSet<>(conversation.getParticipants());
        receivers.remove(author);
        Message message = messageService.createMessage(author, receivers, text, false, null, null);
        Conversation updatedConversation = conversationService.addMessage(conversation, message);
        List<Message> messages = messageService.getMessagesFromConversation(updatedConversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        GetConversationDto result = new GetConversationDto(updatedConversation, messages, author, userInfoDto.getTimezone());
        HttpHeaders headers = new HttpHeaders();
        return ResponseEntity.ok().headers(headers).body(result);
    }

    @PostMapping("/conversations/{conversationId}/participants")
    public ResponseEntity addParticipants(
            @RequestHeader(value = USER_INFO_HEADER) UserInfoDto userInfoDto,
            @PathVariable("conversationId") Long conversationId,
            @RequestBody AddParticipantsDto addParticipantsDto) {
        Conversation conversation = conversationService.getConversation(conversationId);

        if (conversation == null) {
            throw new NotFoundException("Conversation with id '" + conversationId + "' has not been found.");
        }

        Long initiatorId = userInfoDto.getProfileId();
        Participant initiator = participantService.getParticipant(initiatorId);
        Set<Long> subjectIds = addParticipantsDto.getParticipants();
        Set<Participant> subjects = participantService.getParticipants(subjectIds);

        if (subjects == null || subjects.isEmpty()) {
            throw new IllegalStateException("No subjects found in database");
        }

        Set<Participant> participants = conversation.getParticipants();

        if (participants.stream().filter(subjects::contains).findAny().isPresent()) {
            throw new IllegalStateException("An existent member is being added to a conversation.");
        }

        Conversation updatedConversation = conversationService.addParticipants(conversation, initiator, subjects);
        List<Message> messages = messageService.getMessagesFromConversation(updatedConversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        GetConversationDto result = new GetConversationDto(updatedConversation, messages, initiator, userInfoDto.getTimezone());
        HttpHeaders headers = new HttpHeaders();
        return ResponseEntity.ok().headers(headers).body(result);
    }

    @DeleteMapping("/conversations/{conversationId}/participants/{subjectId}")
    public ResponseEntity removeParticipant(
            @RequestHeader(value = USER_INFO_HEADER) UserInfoDto userInfoDto,
            @PathVariable("conversationId") Long conversationId,
            @PathVariable("subjectId") Long subjectId) {
        Conversation conversation = conversationService.getConversation(conversationId);

        if (conversation == null) {
            throw new NotFoundException("Conversation with id '" + conversationId + "' has not been found.");
        }

        Long initiatorId = userInfoDto.getProfileId();
        Participant initiator = participantService.getParticipant(initiatorId);
        Participant subject = participantService.getParticipant(subjectId);
        Conversation updatedConversation = conversationService.removeParticipant(conversation, initiator, subject);
        List<Message> messages = messageService.getMessagesFromConversation(updatedConversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        GetConversationDto result = new GetConversationDto(updatedConversation, messages, initiator, userInfoDto.getTimezone());
        HttpHeaders headers = new HttpHeaders();
        return ResponseEntity.ok().headers(headers).body(result);
    }

    @PostMapping("/conversations/{conversationId}/name")
    public ResponseEntity renameConversation(
            @RequestHeader(value = USER_INFO_HEADER) UserInfoDto userInfoDto,
            @PathVariable("conversationId") Long conversationId,
            @RequestBody UpdateConversationNameDto updateConversationNameDto) {
        Long initiatorId = userInfoDto.getProfileId();
        Participant initiator = participantService.getParticipant(initiatorId);
        Conversation conversation = conversationService.getConversation(conversationId);
        Conversation updatedConversation = conversationService.rename(conversation, initiator, updateConversationNameDto.getName());

        List<Message> messages = messageService.getMessagesFromConversation(updatedConversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        GetConversationDto result = new GetConversationDto(updatedConversation, messages, initiator, userInfoDto.getTimezone());
        HttpHeaders headers = new HttpHeaders();
        return ResponseEntity.ok().headers(headers).body(result);
    }

    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity readMessages(
            @RequestHeader(value = USER_INFO_HEADER) UserInfoDto userInfoDto,
            @PathVariable("conversationId") Long conversationId,
            @RequestBody ReadMessagesDto readMessagesDto) {
        Conversation conversation = conversationService.getConversation(conversationId);

        if (conversation == null) {
            throw new NotFoundException("No conversation with id " + conversationId + " found.");
        }

        Long participantId = userInfoDto.getProfileId();
        Participant participant = participantService.getParticipant(participantId);
        List<Message> messages = messageService.findMessagesByIds(readMessagesDto.getMessages());

        if (messages.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("Some of the messages specified were not found.");
        }

        messageService.markAsRead(conversation, participant, messages);
        HttpHeaders headers = new HttpHeaders();
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

    @ExceptionHandler(HystrixRuntimeException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String returnServiceUnavailable(HystrixRuntimeException e) {
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
