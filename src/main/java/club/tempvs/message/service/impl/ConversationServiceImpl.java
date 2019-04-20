package club.tempvs.message.service.impl;

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

import java.util.*;

import static java.util.stream.Collectors.*;

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

    public GetConversationDto createConversation(Set<Long> receiverIds, String name, String text) {
        User user = userHolder.getUser();
        Long authorId = user.getProfileId();
        String timeZone = user.getTimezone();
        Participant author = participantService.getParticipant(authorId);
        Set<Participant> receivers = participantService.getParticipants(receiverIds);
        Message message = messageService.createMessage(author, receivers, text, false, null, null);
        Conversation conversation = buildConversation(author, receivers, name, message);
        List<Message> messages = messageService.getMessagesFromConversation(conversation, DEFAULT_PAGE_NUMBER, MAX_PAGE_SIZE);
        return new GetConversationDto(conversation, messages, author, timeZone);
    }

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    public Conversation buildConversation(Participant author, Set<Participant> receivers, String name, Message message) {
        if (receivers.size() == 1 && receivers.iterator().next().equals(author)) {
            throw new IllegalStateException("Author can't be equal the only receiver");
        }

        validationHelper.validateConversationCreation(author, receivers, message);

        Conversation conversation;

        if (receivers.size() == 1) {
            conversation = findDialogue(author, receivers.iterator().next());

            if (conversation != null) {
                return addMessage(conversation, message);
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

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    public Conversation addParticipants(Conversation conversation, Participant adder, Set<Participant> added) {
        Set<Participant> initialParticipants = conversation.getParticipants();
        validationHelper.validateParticipantsAddition(adder, added, initialParticipants);

        Message message;
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> receivers = new LinkedHashSet<>(initialParticipants);
        receivers.remove(adder);

        if (conversation.getType() == Conversation.Type.DIALOGUE && initialParticipants.size() == 2) {
            receivers.addAll(added);
            message = messageService.createMessage(adder, receivers, CONFERENCE_CREATED, isSystem, null, null);
            return buildConversation(adder, receivers, null, message);
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
