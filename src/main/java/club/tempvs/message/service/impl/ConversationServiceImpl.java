package club.tempvs.message.service.impl;

import club.tempvs.message.api.ForbiddenException;
import club.tempvs.message.dao.ConversationRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.ErrorsDto;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.service.MessageService;
import club.tempvs.message.util.LocaleHelper;
import club.tempvs.message.util.ObjectFactory;
import club.tempvs.message.util.ValidationHelper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private static final String PARTICIPANT_ADDED_MESSAGE = "conversation.add.participant";
    private static final String PARTICIPANT_REMOVED_MESSAGE = "conversation.remove.participant";
    private static final String PARTICIPANT_SELFREMOVED_MESSAGE = "conversation.selfremove.participant";
    private static final String CONFERENCE_CREATED = "conversation.conference.created";
    private static final String CONVERSATION_RENAMED = "conversation.rename";
    private static final String CONVERSATION_NAME_DROPPED = "conversation.drop.name";
    private static final String PARTICIPANTS_FIELD = "participants";
    private static final String TEXT_FIELD = "text";
    private static final String TEXT_EMPTY = "message.empty.text";
    private static final String PARTICIPANTS_EMPTY = "conversation.participant.empty";
    private static final String PARTICIPANTS_WRONG_SIZE = "conversation.participant.wrong.size";
    private static final String TYPE_MISMATCH = "conversation.participant.type.mismatch";
    private static final String PERIOD_MISMATCH = "conversation.participant.period.mismatch";

    private final ObjectFactory objectFactory;
    private final MessageService messageService;
    private final ConversationRepository conversationRepository;
    private final LocaleHelper localeHelper;
    private final ValidationHelper validationHelper;

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    public Conversation createConversation(
            Participant author, Set<Participant> receivers, String name, Message message) {
        if (receivers.size() == 1 && receivers.iterator().next().equals(author)) {
            throw new IllegalStateException("Author can't be equal the only receiver");
        }

        ErrorsDto errorsDto = validationHelper.getErrors();
        Conversation conversation;

        if (receivers == null || receivers.isEmpty() || receivers.size() > 19) {
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, PARTICIPANTS_WRONG_SIZE);
        }

        String text = message.getText();

        if (text == null || text.isEmpty()) {
            validationHelper.addError(errorsDto, TEXT_FIELD, TEXT_EMPTY);
        }

        String type = author.getType();
        String period = author.getPeriod();

        if (receivers.stream().anyMatch(subject -> !subject.getType().equals(type))) {
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, TYPE_MISMATCH);
        } else if (receivers.stream().anyMatch(subject -> !subject.getPeriod().equals(period))) {
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, PERIOD_MISMATCH);
        }

        validationHelper.processErrors(errorsDto);

        if (receivers.size() == 1) {
            conversation = this.findDialogue(author, receivers.iterator().next());

            if (conversation != null) {
                return this.addMessage(conversation, message);
            }
        }

        conversation = objectFactory.getInstance(Conversation.class);
        receivers.stream().forEach(conversation::addParticipant);
        conversation.addParticipant(author);
        conversation.setName(name);
        conversation.addMessage(message);
        conversation.setLastMessage(message);
        message.setConversation(conversation);

        if (conversation.getParticipants().size() > 2) {
            conversation.setAdmin(author);
            conversation.setType(Conversation.Type.CONFERENCE);
        } else {
            conversation.setType(Conversation.Type.DIALOGUE);
        }

        return conversationRepository.save(conversation);
    }

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    public Conversation getConversation(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No conversation with id " + id + " found."));
    }

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    public Conversation addMessage(Conversation conversation, Message message) {
        conversation.addMessage(message);
        conversation.setLastMessage(message);
        message.setConversation(conversation);
        return conversationRepository.save(conversation);
    }

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    public List<Conversation> getConversationsByParticipant(Participant participant, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Object[]> conversationsPerParticipant = conversationRepository.findConversationsPerParticipant(participant, pageable);

        return conversationsPerParticipant.stream()
            .map(entry -> {
                Conversation conversation = (Conversation) entry[0];
                Long count = (Long) entry[1];
                conversation.setUnreadMessagesCount(count);
                Message lastMessage = conversation.getLastMessage();
                Message translatedLastMessage = localeHelper.translateMessageIfSystem(lastMessage);
                conversation.setLastMessage(translatedLastMessage);
                return conversation;
            }).collect(toList());
    }

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    public Conversation addParticipants(Conversation conversation, Participant adder, Set<Participant> added) {
        ErrorsDto errorsDto = validationHelper.getErrors();

        if (added == null || added.isEmpty()) {
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, PARTICIPANTS_EMPTY);
        }

        Set<Participant> initialParticipants = conversation.getParticipants();

        if (initialParticipants.size() + added.size() > 20) {
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, PARTICIPANTS_WRONG_SIZE);
        }

        String type = adder.getType();
        String period = adder.getPeriod();

        if (added.stream().anyMatch(subject -> !subject.getType().equals(type))) {
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, TYPE_MISMATCH);
        } else if (added.stream().anyMatch(subject -> !subject.getPeriod().equals(period))) {
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, PERIOD_MISMATCH);
        }

        validationHelper.processErrors(errorsDto);

        Message message;
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> receivers = new LinkedHashSet<>(initialParticipants);
        receivers.remove(adder);

        if (conversation.getType() == Conversation.Type.DIALOGUE && initialParticipants.size() == 2) {
            receivers.addAll(added);
            message = messageService.createMessage(adder, receivers, CONFERENCE_CREATED, isSystem, null, null);
            return createConversation(adder, receivers, null, message);
        } else {
            List<Message> messages = new ArrayList<>();

            for (Participant participant : added) {
                receivers.add(participant);
                conversation.addParticipant(participant);
                message = messageService.createMessage(adder, receivers, PARTICIPANT_ADDED_MESSAGE, isSystem, null, participant);
                messages.add(message);
            }

            conversation.setLastMessage(messages.get(messages.size() - 1));

            messages.stream().forEach(m -> {
                conversation.addMessage(m);
                m.setConversation(conversation);
            });

            return conversationRepository.save(conversation);
        }
    }

    public Conversation removeParticipant(Conversation conversation, Participant remover, Participant removed) {
        Set<Participant> participants = conversation.getParticipants();

        if (participants.size() <= 2) {
            ErrorsDto errorsDto = validationHelper.getErrors();
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, PARTICIPANTS_WRONG_SIZE);
            validationHelper.processErrors(errorsDto);
        }

        Participant admin = conversation.getAdmin();

        if ((admin == null || !admin.equals(remover)) && !remover.equals(removed)) {
            throw new ForbiddenException("Only admin user can remove participants from a conversation");
        }

        conversation.removeParticipant(removed);
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> receivers = new LinkedHashSet<>(participants);
        receivers.remove(removed);
        receivers.remove(remover);

        Message message;

        if (removed.equals(remover)) {
            message = messageService.createMessage(remover, receivers, PARTICIPANT_SELFREMOVED_MESSAGE, isSystem, null, null);

        } else {
            message = messageService.createMessage(remover, receivers, PARTICIPANT_REMOVED_MESSAGE, isSystem, null, removed);
        }

        return addMessage(conversation, message);
    }

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    public Conversation findDialogue(Participant author, Participant receiver) {
        return conversationRepository.findDialogue(Conversation.Type.DIALOGUE, author, receiver);
    }

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    public long countUpdatedConversationsPerParticipant(Participant participant) {
        return conversationRepository.countByNewMessagesPerParticipant(participant);
    }

    public Conversation rename(Conversation conversation, Participant initiator, String name) {
        Boolean isSystem = Boolean.TRUE;
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
        return addMessage(conversation, message);
    }
}
