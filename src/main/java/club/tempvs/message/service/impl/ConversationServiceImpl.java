package club.tempvs.message.service.impl;

import club.tempvs.message.api.ForbiddenException;
import club.tempvs.message.dao.ConversationRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.ErrorsDto;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.service.MessageService;
import club.tempvs.message.util.ObjectFactory;
import club.tempvs.message.util.ValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.*;

@Service
public class ConversationServiceImpl implements ConversationService {

    private static final String PARTICIPANT_ADDED_MESSAGE = "conversation.add.participant";
    private static final String PARTICIPANT_REMOVED_MESSAGE = "conversation.remove.participant";
    private static final String PARTICIPANT_SELFREMOVED_MESSAGE = "conversation.selfremove.participant";
    private static final String CONFERENCE_CREATED = "conversation.conference.created";
    private static final String CONVERSATION_RENAMED = "conversation.update.name";
    private static final String PARTICIPANTS_FIELD = "participants";
    private static final String CANT_DELETE_PARTICIPANT = "conversation.participant.cant.delete";
    private static final String PARTICIPANTS_EMPTY = "conversation.participant.empty";
    private static final String PARTICIPANTS_WRONG_SIZE = "conversation.participant.wrong.size";
    private static final String TYPE_MISMATCH = "conversation.participant.type.mismatch";
    private static final String PERIOD_MISMATCH = "conversation.participant.period.mismatch";

    private final ObjectFactory objectFactory;
    private final ConversationRepository conversationRepository;
    private final MessageService messageService;
    private final MessageSource messageSource;
    private final ValidationHelper validationHelper;

    @Autowired
    public ConversationServiceImpl(ObjectFactory objectFactory,
                                   MessageService messageService,
                                   ConversationRepository conversationRepository,
                                   MessageSource messageSource,
                                   ValidationHelper validationHelper) {
        this.objectFactory = objectFactory;
        this.messageService = messageService;
        this.conversationRepository = conversationRepository;
        this.messageSource = messageSource;
        this.validationHelper = validationHelper;
    }

    public Conversation createConversation(
            Participant author, Set<Participant> receivers, String name, Message message) {
        Conversation conversation = objectFactory.getInstance(Conversation.class);
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

    public Conversation getConversation(Long id) {
        return conversationRepository.findById(id).orElse(null);
    }

    public Conversation addMessage(Conversation conversation, Message message) {
        conversation.addMessage(message);
        conversation.setLastMessage(message);
        message.setConversation(conversation);
        return conversationRepository.save(conversation);
    }

    private Conversation addMessages(Conversation conversation, List<Message> messages) {
        conversation.setLastMessage(messages.get(messages.size() - 1));

        messages.stream().forEach(message -> {
            conversation.addMessage(message);
            message.setConversation(conversation);
        });

        return conversationRepository.save(conversation);
    }

    public List<Conversation> getConversationsByParticipant(Participant participant, Locale locale, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "lastMessage.createdDate");
        List<Conversation> conversations = conversationRepository.findByParticipantsIn(participant, pageable);
        List<Object[]> unreadMessagesPerConversation = conversationRepository.countUnreadMessages(conversations, participant);
        Map<Conversation, Long> unreadMessagesCountMap = unreadMessagesPerConversation.stream()
                .collect(toMap(entry -> (Conversation) entry[0], entry -> (Long) entry[1]));

        return conversations.stream()
            .map(conversation -> {
                Message lastMessage = conversation.getLastMessage();
                conversation.setUnreadMessagesCount(unreadMessagesCountMap.get(conversation));

                if (lastMessage.getSystem()) {
                    String text = lastMessage.getText();
                    String[] args = new String[0];
                    String argsString = lastMessage.getSystemArgs();

                    if (argsString != null) {
                        args = argsString.split(",");
                    }

                    lastMessage.setText(
                            messageSource.getMessage(text, args, text, locale));
                    conversation.setLastMessage(lastMessage);
                }

                return conversation;
            }).collect(toList());
    }

    public Conversation addParticipants(Conversation conversation, Participant adder, Set<Participant> added) {
        Locale locale = LocaleContextHolder.getLocale();
        ErrorsDto errorsDto = validationHelper.getErrors();

        if (added == null || added.isEmpty()) {
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, PARTICIPANTS_EMPTY, null, locale);
        }

        Set<Participant> initialParticipants = conversation.getParticipants();

        if (initialParticipants.size() + added.size() > 20) {
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, PARTICIPANTS_WRONG_SIZE, null, locale);
        }

        String type = initialParticipants.stream().map(Participant::getType).findAny().get();
        String period = initialParticipants.stream().map(Participant::getPeriod).findAny().get();

        if (added.stream().anyMatch(subject -> !subject.getType().equals(type))) {
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, TYPE_MISMATCH, null, locale);
        } else if (added.stream().anyMatch(subject -> !subject.getPeriod().equals(period))) {
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, PERIOD_MISMATCH, null, locale);
        }

        validationHelper.processErrors(errorsDto);

        Message message;
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> receivers = new LinkedHashSet<>(initialParticipants);
        receivers.remove(adder);

        if (conversation.getType() == Conversation.Type.DIALOGUE && initialParticipants.size() == 2) {
            receivers.addAll(added);
            message = messageService.createMessage(adder, receivers, CONFERENCE_CREATED, isSystem, null);
            return createConversation(adder, receivers, null, message);
        } else {
            List<Message> messages = new ArrayList<>();

            for (Participant participant : added) {
                receivers.add(participant);
                conversation.addParticipant(participant);
                message = messageService.createMessage(adder, receivers, PARTICIPANT_ADDED_MESSAGE, isSystem, null, participant);
                messages.add(message);
            }

            return addMessages(conversation, messages);
        }
    }

    public Conversation removeParticipant(Conversation conversation, Participant remover, Participant removed) {
        Set<Participant> participants = conversation.getParticipants();

        if (participants.size() <= 2) {
            ErrorsDto errorsDto = validationHelper.getErrors();
            Locale locale = LocaleContextHolder.getLocale();
            validationHelper.addError(errorsDto, PARTICIPANTS_FIELD, CANT_DELETE_PARTICIPANT, null, locale);
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
            message = messageService.createMessage(remover, receivers, PARTICIPANT_SELFREMOVED_MESSAGE, isSystem, null);

        } else {
            message = messageService.createMessage(remover, receivers, PARTICIPANT_REMOVED_MESSAGE, isSystem, null, removed);
        }

        return addMessage(conversation, message);
    }

    public Conversation findDialogue(Participant author, Participant receiver) {
        Set<Participant> authorSet = new LinkedHashSet<>();
        authorSet.add(author);
        Set<Participant> receiverSet = new LinkedHashSet<>();
        receiverSet.add(receiver);

        return conversationRepository
                .findOneByTypeAndParticipantsContainsAndParticipantsContains(Conversation.Type.DIALOGUE, authorSet, receiverSet);
    }

    public long countUpdatedConversationsPerParticipant(Participant participant) {
        return conversationRepository.countByNewMessagesPerParticipant(participant);
    }

    public Conversation updateName(Conversation conversation, Participant initiator, String name) {
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> receivers = new LinkedHashSet<>(conversation.getParticipants());
        receivers.remove(initiator);
        Message message = messageService.createMessage(initiator, receivers, CONVERSATION_RENAMED, isSystem, name);
        conversation.setName(name);
        return addMessage(conversation, message);
    }
}
