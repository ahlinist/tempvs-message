package club.tempvs.message.service.impl;

import static java.util.stream.Collectors.*;
import static club.tempvs.message.domain.Conversation.Type.*;

import club.tempvs.message.api.ForbiddenException;
import club.tempvs.message.dao.ConversationRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.ConversationDtoBean;
import club.tempvs.message.dto.ErrorsDto;
import club.tempvs.message.dto.GetConversationDto;
import club.tempvs.message.dto.GetConversationsDto;
import club.tempvs.message.holder.UserHolder;
import club.tempvs.message.model.User;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.service.MessageService;
import club.tempvs.message.service.ParticipantService;
import club.tempvs.message.util.LocaleHelper;
import club.tempvs.message.util.ObjectFactory;
import club.tempvs.message.util.ValidationHelper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int MAX_PAGE_SIZE = 40;
    private static final String PARTICIPANT_ADDED_MESSAGE = "conversation.add.participant";
    private static final String PARTICIPANT_REMOVED_MESSAGE = "conversation.remove.participant";
    private static final String PARTICIPANT_SELFREMOVED_MESSAGE = "conversation.selfremove.participant";
    private static final String CONFERENCE_CREATED = "conversation.conference.created";
    private static final String CONVERSATION_RENAMED = "conversation.rename";
    private static final String CONVERSATION_NAME_DROPPED = "conversation.drop.name";
    private static final String PARTICIPANTS_FIELD = "participants";
    private static final String PARTICIPANTS_WRONG_SIZE = "conversation.participant.wrong.size";

    private final ObjectFactory objectFactory;
    private final MessageService messageService;
    private final ConversationRepository conversationRepository;
    private final LocaleHelper localeHelper;
    private final ValidationHelper validationHelper;
    private final ParticipantService participantService;
    private final UserHolder userHolder;

    @Override
    public GetConversationDto createConversation(Set<Long> receiverIds, String name, String text) {
        Long authorId = userHolder.getUser().getProfileId();
        Participant author = participantService.getParticipant(authorId);
        Set<Participant> receivers = participantService.getParticipants(receiverIds);
        Message message = messageService.createMessage(author, receivers, text, false, null, null);
        Conversation conversation = buildConversation(author, receivers, name, message);
        return prepareGetConversationDto(save(conversation), author);
    }

    @Override
    public GetConversationDto getConversation(Long id, int page, int size) {
        Long callerId = userHolder.getUser().getProfileId();
        Participant caller = participantService.getParticipant(callerId);
        Conversation conversation = findOne(id);

        if (!conversation.getParticipants().contains(caller)) {
            throw new ForbiddenException("Participant " + callerId + " has no access to conversation " + id);
        }

        return prepareGetConversationDto(conversation, caller);
    }

    public Conversation buildConversation(Participant author, Set<Participant> receivers, String name, Message message) {
        if (receivers.size() == 1 && receivers.iterator().next().equals(author)) {
            throw new IllegalStateException("Author can't be equal the only receiver");
        }

        validationHelper.validateConversationCreation(author, receivers, message);

        Conversation conversation;

        if (receivers.size() == 1) {
            conversation = findDialogue(author, receivers.iterator().next());

            if (conversation != null) {
                return messageService.addMessage(conversation, message, author);
            }
        }

        conversation = objectFactory.getInstance(Conversation.class);
        receivers.stream().forEach(conversation::addParticipant);
        conversation.addParticipant(author);
        conversation.setName(name);
        messageService.addMessage(conversation, message, author);

        Map<Participant, Instant> lastReadOn = conversation.getLastReadOn();
        receivers.stream()
                .forEach(receiver -> lastReadOn.put(receiver, Instant.EPOCH));

        if (conversation.getParticipants().size() > 2) {
            conversation.setAdmin(author);
            conversation.setType(CONFERENCE);
        } else {
            conversation.setType(DIALOGUE);
        }

        return conversation;
    }

    @Override
    public GetConversationDto addMessage(Long conversationId, String text) {
        Long authorId = userHolder.getUser().getProfileId();
        Conversation conversation = findOne(conversationId);
        Participant author = participantService.getParticipant(authorId);
        Set<Participant> receivers = new HashSet<>(conversation.getParticipants());
        receivers.remove(author);
        Message message = messageService.createMessage(author, receivers, text, false, null, null);
        conversation = messageService.addMessage(conversation, message, author);
        return prepareGetConversationDto(save(conversation), author);
    }

    @Override
    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    public GetConversationsDto getConversationsAttended(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User user = userHolder.getUser();
        Long participantId = user.getProfileId();
        String timeZone = user.getTimezone();
        Participant participant = participantService.getParticipant(participantId);
        List<Object[]> conversationsPerParticipant = conversationRepository
                .findConversationsPerParticipant(participant, pageable);
        List<ConversationDtoBean> conversationDtoBeans = conversationsPerParticipant.stream()
            .map(entry -> {
                Conversation conversation = (Conversation) entry[0];
                Long count = (Long) entry[1];
                conversation.setUnreadMessagesCount(count);
                Message lastMessage = conversation.getLastMessage();
                Message translatedLastMessage = localeHelper.translateMessageIfSystem(lastMessage);
                conversation.setLastMessage(translatedLastMessage);
                return new ConversationDtoBean(conversation, participant, timeZone);
            }).collect(toList());

        return new GetConversationsDto(conversationDtoBeans);
    }

    @Override
    public GetConversationDto addParticipants(Long conversationId, Set<Long> subjectIds) {
        User user = userHolder.getUser();
        Long initiatorId = user.getProfileId();
        Conversation conversation = findOne(conversationId);
        Participant initiator = participantService.getParticipant(initiatorId);
        Set<Participant> subjects = participantService.getParticipants(subjectIds);
        Set<Participant> participants = conversation.getParticipants();

        if (participants.stream().filter(subjects::contains).findAny().isPresent()) {
            throw new IllegalStateException("An existent member is being added to a conversation.");
        }

        validationHelper.validateParticipantsAddition(initiator, subjects, participants);

        Message message;
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> receivers = new LinkedHashSet<>(participants);
        receivers.remove(initiator);

        if (conversation.getType() == Conversation.Type.DIALOGUE && participants.size() == 2) {
            receivers.addAll(subjects);
            message = messageService.createMessage(initiator, receivers, CONFERENCE_CREATED, isSystem, null, null);
            Conversation updatedConversation = buildConversation(initiator, receivers, null, message);
            return prepareGetConversationDto(save(updatedConversation), initiator);
        } else {
            List<Message> messages = new ArrayList<>();

            for (Participant participant : subjects) {
                receivers.add(participant);
                conversation.addParticipant(participant);
                message = messageService.createMessage(initiator, receivers, PARTICIPANT_ADDED_MESSAGE, isSystem, null, participant);
                messages.add(message);
            }

            conversation.setLastMessage(messages.get(messages.size() - 1));
            messages.stream()
                    .forEach(m -> messageService.addMessage(conversation, m, initiator));
            return prepareGetConversationDto(save(conversation), initiator);
        }
    }

    @Override
    public GetConversationDto removeParticipant(Long conversationId, Long removedId) {
        User user = userHolder.getUser();
        Long removerId = user.getProfileId();
        Participant initiator = participantService.getParticipant(removerId);
        Participant removed = participantService.getParticipant(removedId);
        Conversation conversation = findOne(conversationId);
        Set<Participant> participants = conversation.getParticipants();

        if (participants.size() <= 2) {
            ErrorsDto errorsDto = validationHelper.getErrors();
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, PARTICIPANTS_WRONG_SIZE);
            validationHelper.processErrors(errorsDto);
        }

        Participant admin = conversation.getAdmin();

        if ((admin == null || !admin.equals(initiator)) && !initiator.equals(removed)) {
            throw new ForbiddenException("Only admin user can remove participants from a conversation");
        }

        conversation.removeParticipant(removed);
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> receivers = new LinkedHashSet<>(participants);
        receivers.remove(removed);
        receivers.remove(initiator);

        Message message;

        if (removed.equals(initiator)) {
            message = messageService.createMessage(initiator, receivers, PARTICIPANT_SELFREMOVED_MESSAGE, isSystem, null, null);

        } else {
            message = messageService.createMessage(initiator, receivers, PARTICIPANT_REMOVED_MESSAGE, isSystem, null, removed);
        }

        conversation = messageService.addMessage(conversation, message, initiator);
        return prepareGetConversationDto(save(conversation), initiator);
    }

    @Override
    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    public long countUpdatedConversationsPerParticipant() {
        Long participantId = userHolder.getUser().getProfileId();
        Participant participant = participantService.getParticipant(participantId);
        return conversationRepository.countByNewMessagesPerParticipant(participant);
    }

    @Override
    public GetConversationDto rename(Long conversationId, String name) {
        Boolean isSystem = Boolean.TRUE;
        Conversation conversation = findOne(conversationId);
        Long initiatorId = userHolder.getUser().getProfileId();
        Participant initiator = participantService.getParticipant(initiatorId);
        Set<Participant> receivers = new LinkedHashSet<>(conversation.getParticipants());
        receivers.remove(initiator);
        Message message;

        if (name == null || name.isEmpty()) {
            message = messageService.createMessage(
                    initiator, receivers, CONVERSATION_NAME_DROPPED, isSystem, null, null);
        } else {
            message = messageService.createMessage(
                    initiator, receivers, CONVERSATION_RENAMED, isSystem, name, null);
        }

        conversation.setName(name);
        conversation = messageService.addMessage(conversation, message, initiator);
        return prepareGetConversationDto(save(conversation), initiator);
    }

    @Override
    public void markMessagesAsRead(Long conversationId, List<Long> messageIds) {
        Long participantId = userHolder.getUser().getProfileId();
        Conversation conversation = findOne(conversationId);
        Participant participant = participantService.getParticipant(participantId);
        List<Message> messages = messageService.findMessagesByIds(messageIds);

        if (messages.isEmpty()) {
            throw new IllegalStateException("Empty messages list.");
        }

        if (!messages.stream().map(Message::getConversation).allMatch(conversation::equals)) {
            throw new ForbiddenException("Messages belong to different conversations.");
        }

        if (!conversation.getParticipants().contains(participant)) {
            throw new ForbiddenException("The conversation should contain the given participant.");
        }

        Instant lastMessageCreatedDate = messages.stream()
                .map(Message::getCreatedDate)
                .max(Instant::compareTo)
                .get();

        conversation.getLastReadOn()
                .put(participant, lastMessageCreatedDate);
        save(conversation);
    }

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    private Conversation findOne(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No conversation with id " + id + " found."));
    }

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    private Conversation findDialogue(Participant author, Participant receiver) {
        return conversationRepository.findDialogue(Conversation.Type.DIALOGUE, author, receiver);
    }

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    private Conversation save(Conversation conversation) {
        return conversationRepository.save(conversation);
    }

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    private GetConversationDto prepareGetConversationDto(Conversation conversation, Participant initiator) {
        String timeZone = userHolder.getUser().getTimezone();
        List<Message> messages = messageService.getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        return new GetConversationDto(conversation, messages, initiator, timeZone);
    }
}
