package club.tempvs.message.controller;

import static org.springframework.web.bind.annotation.RequestMethod.*;
import static org.springframework.http.HttpStatus.*;

import club.tempvs.message.api.*;
import club.tempvs.message.domain.*;
import club.tempvs.message.dto.*;
import club.tempvs.message.service.*;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;

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
    public GetConversationDto createConversation(@RequestBody CreateConversationDto createConversationDto) {
        Set<Long> receiverIds = createConversationDto.getReceivers();
        String name = createConversationDto.getName();
        String text = createConversationDto.getText();
        return conversationService.createConversation(receiverIds, name, text);
    }

    @GetMapping("/conversations/{conversationId}")
    public GetConversationDto getConversation(
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
        return new GetConversationDto(conversation, messages, caller, userInfoDto.getTimezone());
    }

    @GetMapping("/conversations")
    public ResponseEntity getConversationsByParticipant(
            @RequestParam(value = PAGE_PARAM, required = false, defaultValue = DEFAULT_PAGE_VALUE) int page,
            @RequestParam(value = SIZE_PARAM, required = false, defaultValue = DEFAULT_SIZE_VALUE) int size) {
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must not be larger than " + MAX_PAGE_SIZE + "!");
        }

        GetConversationsDto result = conversationService.getConversationsAttended(page, size);

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
    public GetConversationDto addMessage(
            @RequestHeader(value = USER_INFO_HEADER) UserInfoDto userInfoDto,
            @PathVariable("conversationId") Long conversationId,
            @RequestBody AddMessageDto addMessageDto) {
        String text = addMessageDto.getText();
        Conversation conversation = conversationService.getConversation(conversationId);
        Long authorId = userInfoDto.getProfileId();
        Participant author = participantService.getParticipant(authorId);
        Set<Participant> receivers = new HashSet<>(conversation.getParticipants());
        receivers.remove(author);
        Message message = messageService.createMessage(author, receivers, text, false, null, null);
        Conversation updatedConversation = conversationService.addMessage(conversation, message);
        List<Message> messages = messageService.getMessagesFromConversation(updatedConversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        return new GetConversationDto(updatedConversation, messages, author, userInfoDto.getTimezone());
    }

    @PostMapping("/conversations/{conversationId}/participants")
    public GetConversationDto addParticipants(
            @RequestHeader(value = USER_INFO_HEADER) UserInfoDto userInfoDto,
            @PathVariable("conversationId") Long conversationId,
            @RequestBody AddParticipantsDto addParticipantsDto) {
        Conversation conversation = conversationService.getConversation(conversationId);
        Long initiatorId = userInfoDto.getProfileId();
        Participant initiator = participantService.getParticipant(initiatorId);
        Set<Long> subjectIds = addParticipantsDto.getParticipants();
        Set<Participant> subjects = participantService.getParticipants(subjectIds);
        Set<Participant> participants = conversation.getParticipants();

        if (participants.stream().filter(subjects::contains).findAny().isPresent()) {
            throw new IllegalStateException("An existent member is being added to a conversation.");
        }

        Conversation updatedConversation = conversationService.addParticipants(conversation, initiator, subjects);
        List<Message> messages = messageService.getMessagesFromConversation(updatedConversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        return new GetConversationDto(updatedConversation, messages, initiator, userInfoDto.getTimezone());
    }

    @DeleteMapping("/conversations/{conversationId}/participants/{subjectId}")
    public GetConversationDto removeParticipant(
            @RequestHeader(value = USER_INFO_HEADER) UserInfoDto userInfoDto,
            @PathVariable("conversationId") Long conversationId,
            @PathVariable("subjectId") Long subjectId) {
        Conversation conversation = conversationService.getConversation(conversationId);
        Long initiatorId = userInfoDto.getProfileId();
        Participant initiator = participantService.getParticipant(initiatorId);
        Participant subject = participantService.getParticipant(subjectId);
        Conversation updatedConversation = conversationService.removeParticipant(conversation, initiator, subject);
        List<Message> messages = messageService.getMessagesFromConversation(updatedConversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        return new GetConversationDto(updatedConversation, messages, initiator, userInfoDto.getTimezone());
    }

    @PostMapping("/conversations/{conversationId}/name")
    public GetConversationDto renameConversation(
            @RequestHeader(value = USER_INFO_HEADER) UserInfoDto userInfoDto,
            @PathVariable("conversationId") Long conversationId,
            @RequestBody UpdateConversationNameDto updateConversationNameDto) {
        Long initiatorId = userInfoDto.getProfileId();
        Participant initiator = participantService.getParticipant(initiatorId);
        Conversation conversation = conversationService.getConversation(conversationId);
        Conversation updatedConversation = conversationService.rename(conversation, initiator, updateConversationNameDto.getName());
        List<Message> messages = messageService.getMessagesFromConversation(updatedConversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        return new GetConversationDto(updatedConversation, messages, initiator, userInfoDto.getTimezone());
    }

    @PostMapping("/conversations/{conversationId}/read")
    public void readMessages(
            @RequestHeader(value = USER_INFO_HEADER) UserInfoDto userInfoDto,
            @PathVariable("conversationId") Long conversationId,
            @RequestBody ReadMessagesDto readMessagesDto) {
        Conversation conversation = conversationService.getConversation(conversationId);
        Long participantId = userInfoDto.getProfileId();
        Participant participant = participantService.getParticipant(participantId);
        List<Message> messages = messageService.findMessagesByIds(readMessagesDto.getMessages());

        if (messages.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("Some of the messages specified were not found.");
        }

        messageService.markAsRead(conversation, participant, messages);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public String returnInternalError(Exception e) {
        return processException(e);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(BAD_REQUEST)
    public String returnBadRequest(Exception e) {
        return processException(e);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(NOT_FOUND)
    public String returnNotFound(Exception e) {
        return processException(e);
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(UNAUTHORIZED)
    public String returnUnauthorized(Exception e) {
        return processException(e);
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(FORBIDDEN)
    public String returnForbidden(Exception e) {
        return processException(e);
    }

    @ExceptionHandler(HystrixRuntimeException.class)
    @ResponseStatus(SERVICE_UNAVAILABLE)
    public String returnServiceUnavailable(Exception e) {
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
