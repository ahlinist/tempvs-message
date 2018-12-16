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
    private static final String PARTICIPANTS_FIELD = "participants";
    private static final String PARTICIPANTS_EMPTY = "conversation.participant.empty";
    private static final String CANT_DELETE_PARTICIPANT = "conversation.participant.cant.delete";
    private static final String PARTICIPANTS_WRONG_SIZE = "conversation.participant.wrong.size";
    private static final String TYPE_MISMATCH = "conversation.participant.type.mismatch";
    private static final String PERIOD_MISMATCH = "conversation.participant.period.mismatch";

    private final ObjectFactory objectFactory;
    private final ConversationService conversationService;
    private final ParticipantService participantService;
    private final MessageService messageService;
    private final AuthHelper authHelper;
    private final LocaleHelper localeHelper;
    private final ValidationHelper validationHelper;

    @Autowired
    public ConversationController(ObjectFactory objectFactory,
                                  ConversationService conversationService,
                                  ParticipantService participantService,
                                  MessageService messageService,
                                  AuthHelper authHelper,
                                  LocaleHelper localeHelper,
                                  ValidationHelper validationHelper) {
        this.objectFactory = objectFactory;
        this.conversationService = conversationService;
        this.participantService = participantService;
        this.messageService = messageService;
        this.authHelper = authHelper;
        this.localeHelper = localeHelper;
        this.validationHelper = validationHelper;
    }

    @GetMapping("/ping")
    public String getPong() {
        return "pong!";
    }

    @PostMapping("/conversations")
    public GetConversationDto createConversation(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "Accept-Language", required = false) String lang,
            @RequestHeader(value = "Accept-Timezone", required = false, defaultValue = "UTC") String timeZone,
            @RequestBody CreateConversationDto createConversationDto) {
        authHelper.authenticate(token);
        createConversationDto.validate();
        Locale locale = localeHelper.getLocale(lang);
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
                messages = messageService.getMessagesFromConversation(conversation, locale, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
            }
        }

        if (conversation == null) {
            conversation = conversationService.createConversation(author, receivers, name, message);
            messages = messageService.getMessagesFromConversation(conversation, locale, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        }

        return objectFactory.getInstance(GetConversationDto.class, conversation, messages, author, timeZone, locale);
    }

    @GetMapping("/conversations/{conversationId}")
    public GetConversationDto getConversation(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "Accept-Language", required = false) String lang,
            @RequestHeader(value = "Accept-Timezone", required = false, defaultValue = "UTC") String timeZone,
            @PathVariable("conversationId") Long conversationId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "40") int size,
            @RequestParam(value = "caller", required = false) Long callerId) {
        authHelper.authenticate(token);
        Locale locale = localeHelper.getLocale(lang);

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

        Conversation conversation = conversationService.getConversation(conversationId);

        if (!conversation.getParticipants().contains(caller)) {
            throw new ForbiddenException("Participant " + callerId + " has no access to conversation " + conversationId);
        }

        List<Message> messages = messageService.getMessagesFromConversation(conversation, locale, page, size);
        return objectFactory.getInstance(GetConversationDto.class, conversation, messages, caller, timeZone, locale);
    }

    @GetMapping("/conversations")
    public ResponseEntity getConversationsByParticipant(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "Accept-Language", required = false) String lang,
            @RequestHeader(value = "Accept-Timezone", required = false, defaultValue = "UTC") String timeZone,
            @RequestParam("participant") Long participantId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "40") int size) {
        authHelper.authenticate(token);
        Locale locale = localeHelper.getLocale(lang);

        if (size > MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be larger than " + MAX_PAGE_SIZE + "!");
        }

        Participant participant = participantService.getParticipant(participantId);

        if (participant == null) {
            throw new BadRequestException("No participant with id " + participantId + " exist!");
        }

        List<Conversation> conversations = conversationService.getConversationsByParticipant(participant, locale, page, size);
        GetConversationsDto result = objectFactory.getInstance(GetConversationsDto.class, conversations, participant, timeZone, locale);

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

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity addMessage(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "Accept-Language", required = false) String lang,
            @RequestHeader(value = "Accept-Timezone", required = false, defaultValue = "UTC") String timeZone,
            @PathVariable("conversationId") Long conversationId,
            @RequestBody AddMessageDto addMessageDto) {
        authHelper.authenticate(token);
        addMessageDto.validate();
        Locale locale = localeHelper.getLocale(lang);
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
        List<Message> messages = messageService.getMessagesFromConversation(updatedConversation, locale, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        GetConversationDto getConversationDto = objectFactory.getInstance(
                GetConversationDto.class, updatedConversation, messages, author, timeZone, locale);

        return ResponseEntity.ok().body(getConversationDto);
    }

    @PostMapping("/conversations/{conversationId}/participants")
    public GetConversationDto addParticipants(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "Accept-Language", required = false) String lang,
            @RequestHeader(value = "Accept-Timezone", required = false, defaultValue = "UTC") String timeZone,
            @PathVariable("conversationId") Long conversationId,
            @RequestBody AddParticipantsDto addParticipantsDto) {
        ErrorsDto errorsDto = validationHelper.getErrors();
        authHelper.authenticate(token);
        Locale locale = localeHelper.getLocale(lang);
        ParticipantDto initiatorDto = addParticipantsDto.getInitiator();
        Set<ParticipantDto> subjectDtos = addParticipantsDto.getSubjects();

        if (initiatorDto == null) {
            throw new IllegalStateException("Initiator is not specified");
        }

        if (subjectDtos == null || subjectDtos.isEmpty()) {
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, PARTICIPANTS_EMPTY, null, locale);
            validationHelper.processErrors(errorsDto);
        }

        Conversation conversation = conversationService.getConversation(conversationId);

        if (conversation == null) {
            throw new NotFoundException("Conversation with id '" + conversationId + "' has not been found.");
        }

        Long initiatorId = initiatorDto.getId();
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

        if (participants.size() + subjects.size() > 20 || participants.size() + subjects.size() < 2) {
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, PARTICIPANTS_WRONG_SIZE, null, locale);
            validationHelper.processErrors(errorsDto);
        }

        if (participants.stream().filter(subjects::contains).findAny().isPresent()) {
            throw new IllegalStateException("An existent member is being added to a conversation.");
        }

        Participant aParticipant = participants.iterator().next();
        String type = aParticipant.getType();
        String period = aParticipant.getPeriod();

        if (subjects.stream().anyMatch(subject -> subject.getType() !=type)) {
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, TYPE_MISMATCH, null, locale);
        } else if (subjects.stream().anyMatch(subject -> subject.getPeriod() != period)) {
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, PERIOD_MISMATCH, null, locale);
        }

        validationHelper.processErrors(errorsDto);
        Conversation result = conversationService.addParticipants(conversation, initiator, subjects);
        List<Message> messages = messageService.getMessagesFromConversation(result, locale, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        return objectFactory.getInstance(GetConversationDto.class, result, messages, initiator, timeZone, locale);
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
        ErrorsDto errorsDto = validationHelper.getErrors();
        Locale locale = localeHelper.getLocale(lang);
        Conversation conversation = conversationService.getConversation(conversationId);

        if (conversation == null) {
            throw new NotFoundException("Conversation with id '" + conversationId + "' has not been found.");
        }

        if (conversation.getParticipants().size() <= 2) {
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, CANT_DELETE_PARTICIPANT, null, locale);
            validationHelper.processErrors(errorsDto);
        }

        Participant initiator = participantService.getParticipant(initiatorId);

        if (initiator == null) {
            throw new IllegalStateException("Participant with id " + initiatorId + " does not exist");
        }

        Participant admin = conversation.getAdmin();
        Participant subject = participantService.getParticipant(subjectId);

        if ((admin == null || !admin.equals(initiator)) && !admin.equals(subject)) {
            throw new ForbiddenException("Only admin user can remove participants from a conversation");
        }

        if (subject == null) {
            throw new IllegalStateException("Participant with id " + subjectId + " does not exist");
        }

        Conversation result = conversationService.removeParticipant(conversation, initiator, subject);
        List<Message> messages = messageService.getMessagesFromConversation(result, locale, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        return objectFactory.getInstance(GetConversationDto.class, result, messages, initiator, timeZone, locale);
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

        List<Message> messages = messageService.getMessagesFromConversation(result, locale, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        return objectFactory.getInstance(GetConversationDto.class, result, messages, initiator, timeZone, locale);
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
            throw new BadRequestException("No conversation with id " + conversationId + " found.");
        }

        ParticipantDto participantDto = readMessagesDto.getParticipant();
        Long participantId = participantDto.getId();
        Participant participant = participantService.getParticipant(participantId);

        if (participant == null) {
            throw new BadRequestException("No participant with id " + participantId + " found.");
        }

        List<Message> messages = messageService.findMessagesByIds(readMessagesDto.getMessageIds());

        if (messages.stream().anyMatch(Objects::isNull)) {
            throw new BadRequestException("Some of the messages specified were not found.");
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
