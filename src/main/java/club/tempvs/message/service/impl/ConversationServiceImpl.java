package club.tempvs.message.service.impl;

import club.tempvs.message.dao.ConversationRepository;
import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.service.MessageService;
import club.tempvs.message.util.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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

    private final ObjectFactory objectFactory;
    private final ConversationRepository conversationRepository;
    private final MessageService messageService;
    private final MessageSource messageSource;

    @Autowired
    public ConversationServiceImpl(ObjectFactory objectFactory,
                                   MessageService messageService,
                                   ConversationRepository conversationRepository,
                                   MessageSource messageSource) {
        this.objectFactory = objectFactory;
        this.messageService = messageService;
        this.conversationRepository = conversationRepository;
        this.messageSource = messageSource;
    }

    public Conversation createConversation(
            Participant author, Set<Participant> receivers, String name, Message message) {
        Conversation conversation = objectFactory.getInstance(Conversation.class);
        conversation.setParticipants(receivers);
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
        Set<Participant> participants = new HashSet<>();
        participants.add(participant);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "lastMessage.createdDate");
        List<Conversation> conversations = conversationRepository.findByParticipantsIn(participants, pageable);
        return conversations.stream()
                .map(conversation -> {
                    Message lastMessage = conversation.getLastMessage();

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

    public Conversation addParticipants(Conversation conversation, Participant adder, List<Participant> added) {
        Set<Participant> initialParticipants = conversation.getParticipants();

        initialParticipants.stream().forEach(participant -> {
            if (added.contains(participant)) {
                throw new IllegalArgumentException("The participant being added is already present in the conversation.");
            }
        });

        if (initialParticipants.size() == 20) {
            throw new IllegalArgumentException("Conversation may have only 20 participants max.");
        }

        Participant admin = conversation.getAdmin();
        Conversation.Type type = conversation.getType();

        if (type == Conversation.Type.CONFERENCE && (admin == null || !admin.equals(adder))) {
            throw new IllegalArgumentException("Participants can be added only by admin.");
        }

        Message message;
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> receivers = new HashSet<>(initialParticipants);
        receivers.remove(adder);

        if (type == Conversation.Type.DIALOGUE && initialParticipants.size() == 2) {
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
            throw new IllegalArgumentException("Conversation has only 2 participants. One can't delete one of them.");
        }

        Participant admin = conversation.getAdmin();
        boolean isSelfRemoval = removed.equals(remover);

        if ((admin == null || !admin.equals(remover)) && !isSelfRemoval) {
            throw new IllegalArgumentException("Participants can be removed only by admin or by themselves.");
        }

        conversation.removeParticipant(removed);
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> receivers = new HashSet<>(participants);
        receivers.remove(removed);
        receivers.remove(remover);

        Message message;

        if (isSelfRemoval) {
            message = messageService.createMessage(remover, receivers, PARTICIPANT_SELFREMOVED_MESSAGE, isSystem, null);

        } else {
            message = messageService.createMessage(remover, receivers, PARTICIPANT_REMOVED_MESSAGE, isSystem, null, removed);
        }

        return addMessage(conversation, message);
    }

    public Conversation findDialogue(Participant author, Participant receiver) {
        Set<Participant> authorSet = new HashSet<>();
        authorSet.add(author);
        Set<Participant> receiverSet = new HashSet<>();
        receiverSet.add(receiver);

        return conversationRepository
                .findOneByTypeAndParticipantsContainsAndParticipantsContains(Conversation.Type.DIALOGUE, authorSet, receiverSet);
    }

    public long countUpdatedConversationsPerParticipant(Participant participant) {
        return conversationRepository.countByNewMessagesPerParticipant(participant);
    }

    public Conversation updateName(Conversation conversation, Participant initiator, String name) {
        Boolean isSystem = Boolean.TRUE;
        Set<Participant> receivers = new HashSet<>(conversation.getParticipants());
        receivers.remove(initiator);
        Message message = messageService.createMessage(initiator, receivers, CONVERSATION_RENAMED, isSystem, name);
        conversation.setName(name);
        return addMessage(conversation, message);
    }
}
