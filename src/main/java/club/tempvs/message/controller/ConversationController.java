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
            @PathVariable("conversationId") Long conversationId,
            @RequestParam(value = PAGE_PARAM, required = false, defaultValue = DEFAULT_PAGE_VALUE) int page,
            @RequestParam(value = SIZE_PARAM, required = false, defaultValue = DEFAULT_SIZE_VALUE) int size) {
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must not be larger than " + MAX_PAGE_SIZE + "!");
        }

        return conversationService.getConversation(conversationId, page, size);
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
    public ResponseEntity countConversations() {
        long result = conversationService.countUpdatedConversationsPerParticipant();
        HttpHeaders headers = new HttpHeaders();
        headers.add(COUNT_HEADER, String.valueOf(result));
        return ResponseEntity.ok().headers(headers).build();
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public GetConversationDto addMessage(
            @PathVariable("conversationId") Long conversationId,
            @RequestBody AddMessageDto addMessageDto) {
        String text = addMessageDto.getText();
        return conversationService.addMessage(conversationId, text);
    }

    @PostMapping("/conversations/{conversationId}/participants")
    public GetConversationDto addParticipants(
            @PathVariable("conversationId") Long conversationId,
            @RequestBody AddParticipantsDto addParticipantsDto) {
        Set<Long> subjectIds = addParticipantsDto.getParticipants();

        return conversationService.addParticipants(conversationId, subjectIds);
    }

    @DeleteMapping("/conversations/{conversationId}/participants/{subjectId}")
    public GetConversationDto removeParticipant(
            @PathVariable("conversationId") Long conversationId,
            @PathVariable("subjectId") Long subjectId) {
        return conversationService.removeParticipant(conversationId, subjectId);
    }

    @PostMapping("/conversations/{conversationId}/name")
    public GetConversationDto renameConversation(
            @PathVariable("conversationId") Long conversationId,
            @RequestBody UpdateConversationNameDto updateConversationNameDto) {
        return conversationService.rename(conversationId, updateConversationNameDto.getName());
    }

    @PostMapping("/conversations/{conversationId}/read")
    public void readMessages(
            @RequestHeader(value = USER_INFO_HEADER) UserInfoDto userInfoDto,
            @PathVariable("conversationId") Long conversationId,
            @RequestBody ReadMessagesDto readMessagesDto) {
        Conversation conversation = conversationService.findOne(conversationId);
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
