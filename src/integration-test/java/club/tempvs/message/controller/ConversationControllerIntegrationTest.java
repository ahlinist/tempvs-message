package club.tempvs.message.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static java.util.stream.Collectors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.*;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.*;
import club.tempvs.message.util.EntityHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableSet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.LongStream;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class ConversationControllerIntegrationTest {

    private static final String USER_INFO_HEADER = "User-Info";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN = "df41895b9f26094d0b1d39b7bdd9849e"; //security_token as MD5
    private static final String CONFERENCE = Conversation.Type.CONFERENCE.toString();
    private static final String DIALOGUE = Conversation.Type.DIALOGUE.toString();
    private static final String COUNT_HEADER = "X-Total-Count";
    private static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mvc;
    @Autowired
    private EntityHelper entityHelper;

    @BeforeClass
    public static void setupSpec() {
        mapper.registerModule(new JavaTimeModule());
    }
    
    @Test
    public void testCreateConversation() throws Exception {
        Long authorId = 4L;
        String message = "myMessage";
        String name = "conversation name";

        entityHelper.createParticipant(authorId, "name1", "USER", "");
        entityHelper.createParticipant(2L, "name2", "USER", "");
        entityHelper.createParticipant(3L, "name3", "USER", "");
        entityHelper.createParticipant(1L, "name4", "USER", "");

        Set<Long> receiverIds = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        String createConversationJson = getCreateConversationDtoJson(receiverIds, message, name);
        String userInfoValue = buildUserInfoValue(authorId);

        mvc.perform(post("/api/conversations")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson)
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("participants", hasSize(4)))
                    .andExpect(jsonPath("admin.id", is(authorId.intValue())))
                    .andExpect(jsonPath("messages", hasSize(1)))
                    .andExpect(jsonPath("messages[0].text", is(message)))
                    .andExpect(jsonPath("messages[0].author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[0].subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("messages[0].unread", is(false)))
                    .andExpect(jsonPath("messages[0].system", is(false)))
                    .andExpect(jsonPath("type", is(CONFERENCE)));
    }

    @Test
    public void testCreateConversationForExistentDialogue() throws Exception {
        Long authorId = 1L;
        Long receiverId = 2L;
        String oldMessage = "my old message";
        String newMessage = "my new message";
        String name = null;

        Participant author = entityHelper.createParticipant(authorId, "name", "CLUB", "ANTIQUITY");
        Set<Participant> receivers = ImmutableSet.of(
                entityHelper.createParticipant(receiverId, "name", "CLUB", "ANTIQUITY")
        );

        entityHelper.createConversation(author, receivers, oldMessage, name);
        Set<Long> receiverIds = ImmutableSet.of(receiverId);
        String createConversationJson = getCreateConversationDtoJson(receiverIds, newMessage, name);
        String userInfoValue = buildUserInfoValue(authorId);

        mvc.perform(post("/api/conversations")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson)
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("participants", hasSize(2)))
                    .andExpect(jsonPath("admin", isEmptyOrNullString()))
                    .andExpect(jsonPath("messages", hasSize(2)))
                    .andExpect(jsonPath("messages[0].text", is(oldMessage)))
                    .andExpect(jsonPath("messages[0].author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[0].subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("messages[0].unread", is(false)))
                    .andExpect(jsonPath("messages[0].system", is(false)))
                    .andExpect(jsonPath("messages[1].text", is(newMessage)))
                    .andExpect(jsonPath("messages[1].author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[1].subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("messages[1].unread", is(false)))
                    .andExpect(jsonPath("messages[1].system", is(false)))
                    .andExpect(jsonPath("type", is(DIALOGUE)));
    }

    @Test
    public void testCreateConversationWithNoMessage() throws Exception {
        Long authorId = 4L;
        String name = "conversation name";

        entityHelper.createParticipant(authorId, "name", "CLUB", "ANTIQUITY");
        entityHelper.createParticipant(1L, "name", "CLUB", "ANTIQUITY");
        entityHelper.createParticipant(2L, "name", "CLUB", "ANTIQUITY");
        entityHelper.createParticipant(3L, "name", "CLUB", "ANTIQUITY");

        Set<Long> receiverIds = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        String createConversationJson = getCreateConversationDtoJson(receiverIds, null, name);
        String userInfoValue = buildUserInfoValue(authorId);

        mvc.perform(post("/api/conversations")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson)
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("errors.text", is("Please type your message")));
    }

    @Test
    public void testCreateConversationWithNoReceivers() throws Exception {
        Long authorId = 4L;
        Set<Long> receiverIds = new HashSet<>();
        String message = "myMessage";
        String name = "conversation name";

        entityHelper.createParticipant(authorId, "name", "CLUB", "ANTIQUITY");
        String createConversationJson = getCreateConversationDtoJson(receiverIds, message, name);
        String userInfoValue = buildUserInfoValue(authorId);

        mvc.perform(post("/api/conversations")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson)
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("errors.participants", is("Conversation may contain from 2 to 20 participants")));
    }

    @Test
    public void testGetConversation() throws Exception {
        Long authorId = 1L;
        String text = "text";
        String name = "name";

        Participant author = entityHelper.createParticipant(authorId, "name", "CLUB", "ANTIQUITY");
        Set<Participant> receivers = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(4L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(2L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(3L, "name", "CLUB", "ANTIQUITY")
        ));

        Conversation conversation = entityHelper.createConversation(author, receivers, text, name);
        Long conversationId = conversation.getId();
        List<Message> messages = conversation.getMessages();
        int messagesSize = messages.size();
        Long messageId = messages.get(0).getId();
        Boolean isSystem = messages.get(0).getIsSystem();
        String userInfoValue = buildUserInfoValue(authorId);

        mvc.perform(get("/api/conversations/" + conversationId + "?caller=" + authorId)
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("id", is(conversationId.intValue())))
                    .andExpect(jsonPath("admin.id", is(authorId.intValue())))
                    .andExpect(jsonPath("participants", hasSize(4)))
                    .andExpect(jsonPath("messages", hasSize(messagesSize)))
                    .andExpect(jsonPath("messages[0].id", is(messageId.intValue())))
                    .andExpect(jsonPath("messages[0].text", is(text)))
                    .andExpect(jsonPath("messages[0].author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[0].subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("messages[0].unread", is(false)))
                    .andExpect(jsonPath("messages[0].system", is(isSystem)))
                    .andExpect(jsonPath("type", is(CONFERENCE)));
    }

    @Test
    public void testGetConversationForWrongCaller() throws Exception {
        Long authorId = 1L;
        Long wrongCallerId = 5L;
        String text = "text";
        String name = "name";

        Participant author = entityHelper.createParticipant(authorId, "name", "CLUB", "ANTIQUITY");
        Set<Participant> receivers = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(4L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(2L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(3L, "name", "CLUB", "ANTIQUITY")
        ));

        entityHelper.createParticipant(wrongCallerId, "name", "USER", "");
        Conversation conversation = entityHelper.createConversation(author, receivers, text, name);
        Long conversationId = conversation.getId();
        String userInfoValue = buildUserInfoValue(wrongCallerId);

        mvc.perform(get("/api/conversations/" + conversationId + "?page=0&size=20")
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string("Participant " + wrongCallerId + " has no access to conversation " + conversationId));
    }

    @Test
    public void testGetConversationsByParticipant() throws Exception {
        Long callerId = 3L;
        String text = "text";
        String name = "name";

        Participant caller = entityHelper.createParticipant(callerId, "name", "CLUB", "ANTIQUITY");
        Participant author1 = entityHelper.createParticipant(10L, "name", "CLUB", "ANTIQUITY");
        Participant author2 = entityHelper.createParticipant(15L, "name", "CLUB", "ANTIQUITY");

        Set<Participant> receivers1 = ImmutableSet.of(author2, caller);
        Set<Participant> receivers2 = ImmutableSet.of(caller);

        entityHelper.createConversation(author1, receivers1, text, name);
        entityHelper.createConversation(author2, receivers2, text, name);
        String userInfoValue = buildUserInfoValue(callerId);

        mvc.perform(get("/api/conversations?page=0&size=10")
            .header(USER_INFO_HEADER, userInfoValue)
            .header(AUTHORIZATION_HEADER, TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("conversations", hasSize(2)))
                .andExpect(jsonPath("conversations[0].name", is(name)))
                .andExpect(jsonPath("conversations[0].lastMessage.text", is(text)))
                .andExpect(jsonPath("conversations[0].lastMessage.subject", isEmptyOrNullString()))
                .andExpect(jsonPath("conversations[0].lastMessage.unread", is(true)))
                .andExpect(jsonPath("conversations[0].lastMessage.system", is(false)))
                .andExpect(jsonPath("conversations[0].type", is(DIALOGUE)))
                .andExpect(jsonPath("conversations[0].conversant", is("name")))
                .andExpect(jsonPath("conversations[0].unreadMessagesCount", is(1)))
                .andExpect(jsonPath("conversations[1].name", is(name)))
                .andExpect(jsonPath("conversations[1].lastMessage.text", is(text)))
                .andExpect(jsonPath("conversations[1].lastMessage.subject", isEmptyOrNullString()))
                .andExpect(jsonPath("conversations[1].lastMessage.unread", is(true)))
                .andExpect(jsonPath("conversations[1].lastMessage.system", is(false)))
                .andExpect(jsonPath("conversations[1].type", is(CONFERENCE)))
                .andExpect(jsonPath("conversations[1].conversant", is("name, name")))
                .andExpect(jsonPath("conversations[1].unreadMessagesCount", is(1)));
    }

    @Test
    public void testGetConversationsByParticipantWithMultipleConversants() throws Exception {
        Long authorId = 10L;
        String text = "text";
        String name = "name";

        Participant author = entityHelper.createParticipant(authorId, "name", "CLUB", "ANTIQUITY");
        Set<Participant> receivers = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(4L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(2L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(3L, "name", "CLUB", "ANTIQUITY")
        ));

        Conversation conversation = entityHelper.createConversation(author, receivers, text, name);
        Long conversationId = conversation.getId();
        List<Message> messages = conversation.getMessages();
        Boolean isSystem = messages.get(0).getIsSystem();
        String userInfoValue = buildUserInfoValue(authorId);

        mvc.perform(get("/api/conversations?page=0&size=10")
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("conversations", hasSize(1)))
                    .andExpect(jsonPath("conversations[0].id", is(conversationId.intValue())))
                    .andExpect(jsonPath("conversations[0].name", is(name)))
                    .andExpect(jsonPath("conversations[0].lastMessage.text", is(text)))
                    .andExpect(jsonPath("conversations[0].lastMessage.author.name", is(name)))
                    .andExpect(jsonPath("conversations[0].lastMessage.subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("conversations[0].lastMessage.unread", is(false)))
                    .andExpect(jsonPath("conversations[0].lastMessage.system", is(isSystem)))
                    .andExpect(jsonPath("conversations[0].type", is(CONFERENCE)))
                    .andExpect(jsonPath("conversations[0].conversant", is("name, name, name")));
    }

    @Test
    public void testGetConversationsByParticipantForInvalidInput() throws Exception {
        Long callerId = 1L;
        Long notExistentParticipant = 2L;
        entityHelper.createParticipant(callerId, "name", "USER", "");
        String userInfoValue = buildUserInfoValue(callerId);
        String wrongUserInfoValue = buildUserInfoValue(notExistentParticipant);

        mvc.perform(get("/api/conversations?page=0&size=-1")
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(equalTo("Page size must not be less than one!")));

        mvc.perform(get("/api/conversations?page=0&size=0")
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(equalTo("Page size must not be less than one!")));

        mvc.perform(get("/api/conversations?page=-1&size=20")
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(equalTo("Page index must not be less than zero!")));

        mvc.perform(get("/api/conversations?page=0&size=50")
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(equalTo("Page size must not be larger than 40!")));

        mvc.perform(get("/api/conversations?page=0&size=20")
                .header(USER_INFO_HEADER, wrongUserInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string(equalTo("No participant with id 2 found in the db")));
    }

    @Test
    public void testAddMessage() throws Exception {
        Long authorId = 1L;
        String text = "text";
        String name = "name";
        String newMessageText = "new message text";

        Participant author = entityHelper.createParticipant(authorId, "name", "CLUB", "ANTIQUITY");
        Set<Participant> receivers = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(4L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(2L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(3L, "name", "CLUB", "ANTIQUITY")
        ));

        Conversation conversation = entityHelper.createConversation(author, receivers, text, name);
        Long conversationId = conversation.getId();
        List<Message> messages = conversation.getMessages();
        int initialMessagesSize = messages.size();
        Long messageId = messages.get(0).getId();
        String addMessageJson = getAddMessageDtoJson(newMessageText);
        String userInfoValue = buildUserInfoValue(authorId);

        mvc.perform(post("/api/conversations/" + conversationId + "/messages")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addMessageJson)
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("id", is(conversationId.intValue())))
                    .andExpect(jsonPath("admin.id", is(authorId.intValue())))
                    .andExpect(jsonPath("participants", hasSize(4)))
                    .andExpect(jsonPath("messages", hasSize(initialMessagesSize + 1)))
                    .andExpect(jsonPath("messages[0].id", is(messageId.intValue())))
                    .andExpect(jsonPath("messages[0].text", is(text)))
                    .andExpect(jsonPath("messages[0].author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[0].subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("messages[0].unread", is(false)))
                    .andExpect(jsonPath("messages[0].system", is(false)))
                    .andExpect(jsonPath("messages[1].id", is(messageId.intValue() + 1)))
                    .andExpect(jsonPath("messages[1].text", is(newMessageText)))
                    .andExpect(jsonPath("messages[1].author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[1].subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("messages[1].unread", is(false)))
                    .andExpect(jsonPath("messages[1].system", is(false)))
                    .andExpect(jsonPath("type", is(CONFERENCE)));
    }

    @Test
    public void testAddMessageForMissingConversationInDB() throws Exception {
        Long authorId = 1L;
        String newMessageText = "new message text";
        int missingConversationId = 2;

        String addMessageJson = getAddMessageDtoJson(newMessageText);
        String userInfoValue = buildUserInfoValue(authorId);

        mvc.perform(post("/api/conversations/" + missingConversationId + "/messages")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addMessageJson)
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(equalTo("No conversation with id 2 found.")));
    }

    @Test
    public void testAddParticipantToDialogue() throws Exception {
        Long authorId = 1L;
        Long receiverId = 2L;
        String text = "an initial text";
        Long addedParticipantId = 3L;

        Participant author = entityHelper.createParticipant(authorId, "name", "USER", "");
        Set<Participant> receivers = ImmutableSet.of(
                entityHelper.createParticipant(receiverId, "name", "USER", "")
        );

        Conversation conversation = entityHelper.createConversation(author, receivers, text, "");
        Long initialConversationId = conversation.getId();
        String conferenceCreatedMessage = "created a conference";

        entityHelper.createParticipant(addedParticipantId, "name", "USER", "");

        AddParticipantsDto addParticipantsDto = new AddParticipantsDto();
        addParticipantsDto.setParticipants(ImmutableSet.of(addedParticipantId));

        String addParticipantJson = mapper.writeValueAsString(addParticipantsDto);
        String userInfoValue = buildUserInfoValue(authorId);

        mvc.perform(post("/api/conversations/" + initialConversationId + "/participants")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addParticipantJson)
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("id", not(initialConversationId.intValue())))
                    .andExpect(jsonPath("participants", hasSize(3)))
                    .andExpect(jsonPath("messages", hasSize(1)))
                    .andExpect(jsonPath("messages[0].text", is(conferenceCreatedMessage)))
                    .andExpect(jsonPath("messages[0].author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[0].unread", is(false)))
                    .andExpect(jsonPath("messages[0].system", is(true)))
                    .andExpect(jsonPath("type", is(CONFERENCE)));
    }

    @Test
    public void testAddParticipantToDialogueForExistentOne() throws Exception {
        Long authorId = 1L;
        Long receiverId = 2L;
        Long addedParticipantId = 2L;
        String text = "text";

        Participant author = entityHelper.createParticipant(authorId, "name", "USER", "");
        Set<Participant> receivers = ImmutableSet.of(
                entityHelper.createParticipant(receiverId, "name", "USER", "")
        );

        Conversation conversation = entityHelper.createConversation(author, receivers, text, "");
        Long initialConversationId = conversation.getId();

        entityHelper.createParticipant(addedParticipantId, "name", "USER", "");

        AddParticipantsDto addParticipantsDto = new AddParticipantsDto();
        addParticipantsDto.setParticipants(ImmutableSet.of(addedParticipantId));

        String addParticipantJson = mapper.writeValueAsString(addParticipantsDto);
        String userInfoValue = buildUserInfoValue(authorId);

        mvc.perform(post("/api/conversations/" + initialConversationId + "/participants")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addParticipantJson)
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("An existent member is being added to a conversation."));
    }

    @Test
    public void testAddParticipantToDialogueForMismatchingPeriods() throws Exception {
        Long authorId = 1L;
        Long addedParticipantId = 33L;

        Set<Participant> receivers = LongStream.rangeClosed(2L, 20L).boxed()
                .map(i -> entityHelper.createParticipant(i, "name", "CLUB", "EARLY_MIDDLE_AGES"))
                .collect(toSet());

        Participant author = entityHelper.createParticipant(authorId, "name", "CLUB", "EARLY_MIDDLE_AGES");

        Conversation conversation = entityHelper.createConversation(author, receivers, "an initial text", "");
        Long conversationId = conversation.getId();

        entityHelper.createParticipant(addedParticipantId, "name", "CLUB", "LATE_MIDDLE_AGES");

        AddParticipantsDto addParticipantsDto = new AddParticipantsDto();
        addParticipantsDto.setParticipants(ImmutableSet.of(addedParticipantId));

        String addParticipantJson = mapper.writeValueAsString(addParticipantsDto);
        String userInfoValue = buildUserInfoValue(authorId);

        mvc.perform(post("/api/conversations/" + conversationId + "/participants")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addParticipantJson)
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("errors.participants",
                            is("Conversation may contain from 2 to 20 participants" +
                                    "\nConversation can contain only participants of the same period")));
    }

    @Test
    public void testAddParticipantToConference() throws Exception {
        Long authorId = 1L;
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(2L, 3L, 4L));
        String text = "text";
        String name = "name";
        Long addedParticipantId = 5L;
        Set<Long> participantIds = new HashSet<>(receiverIds);
        participantIds.add(authorId);
        participantIds.add(addedParticipantId);

        Participant author = entityHelper.createParticipant(1L, "name", "USER", "");
        Set<Participant> participants = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(2L, "name", "USER", ""),
                entityHelper.createParticipant(3L, "name", "USER", ""),
                entityHelper.createParticipant(4L, "name", "USER", "")
        ));

        Conversation conversation = entityHelper.createConversation(author, participants, text, name);
        Long conversationId = conversation.getId();
        int messagesInitialSize = conversation.getMessages().size();
        String participantAddedMessage = "added";

        entityHelper.createParticipant(5L, "name", "USER", "");

        AddParticipantsDto addParticipantsDto = new AddParticipantsDto();
        addParticipantsDto.setParticipants(ImmutableSet.of(addedParticipantId));

        String addParticipantJson = mapper.writeValueAsString(addParticipantsDto);
        String userInfoValue = buildUserInfoValue(authorId);

        mvc.perform(post("/api/conversations/" + conversationId + "/participants")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addParticipantJson)
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("id", is(conversationId.intValue())))
                    .andExpect(jsonPath("admin.id", is(authorId.intValue())))
                    .andExpect(jsonPath("participants", hasSize(participantIds.size())))
                    .andExpect(jsonPath("messages", hasSize(messagesInitialSize + 1)))
                    .andExpect(jsonPath("messages[0].text", is(text)))
                    .andExpect(jsonPath("messages[0].author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[0].subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("messages[0].unread", is(false)))
                    .andExpect(jsonPath("messages[0].system", is(false)))
                    .andExpect(jsonPath("messages[1].text", is(participantAddedMessage)))
                    .andExpect(jsonPath("messages[1].author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[1].subject.id", is(addedParticipantId.intValue())))
                    .andExpect(jsonPath("messages[1].unread", is(false)))
                    .andExpect(jsonPath("messages[1].system", is(true)))
                    .andExpect(jsonPath("type", is(CONFERENCE)));
    }

    @Test
    public void testAddParticipantWithNotExistentSubject() throws Exception {
        Long authorId = 1L;
        Long addedParticipantId = 5L;

        Participant author = entityHelper.createParticipant(authorId, "name", "USER", "");
        Participant receiver = entityHelper.createParticipant(2L, "name", "USER", "");
        Set<Participant> receivers = ImmutableSet.of(receiver);

        Conversation conversation = entityHelper.createConversation(author, receivers, "text", "name");
        Long conversationId = conversation.getId();

        AddParticipantsDto addParticipantsDto = new AddParticipantsDto();
        addParticipantsDto.setParticipants(ImmutableSet.of(addedParticipantId));

        String addParticipantJson = mapper.writeValueAsString(addParticipantsDto);
        String userInfoValue = buildUserInfoValue(authorId);

        mvc.perform(post("/api/conversations/" + conversationId + "/participants")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addParticipantJson)
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string(equalTo("No participants with given ids found in database")));
    }

    @Test
    public void testRemoveParticipant() throws Exception {
        Long authorId = 1L;
        String text = "text";
        String name = "name";
        int removedParticipantId = 4;

        Participant author = entityHelper.createParticipant(authorId, "name", "CLUB", "ANTIQUITY");
        Set<Participant> receivers = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(4L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(2L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(3L, "name", "CLUB", "ANTIQUITY")
        ));

        Conversation conversation = entityHelper.createConversation(author, receivers, text, name);
        Long conversationId = conversation.getId();
        int messagesInitialSize = conversation.getMessages().size();
        String participantRemovedMessage = "removed";
        String url = "/api/conversations/" + conversationId + "/participants/" + removedParticipantId;
        String userInfoValue = buildUserInfoValue(authorId);

        mvc.perform(delete(url)
                .contentType(APPLICATION_JSON_VALUE)
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("id", is(conversationId.intValue())))
                    .andExpect(jsonPath("admin.id", is(authorId.intValue())))
                    .andExpect(jsonPath("participants", hasSize(3)))
                    .andExpect(jsonPath("messages", hasSize(messagesInitialSize + 1)))
                    .andExpect(jsonPath("messages[0].text", is(text)))
                    .andExpect(jsonPath("messages[0].author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[0].subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("messages[0].unread", is(false)))
                    .andExpect(jsonPath("messages[0].system", is(false)))
                    .andExpect(jsonPath("messages[1].text", is(participantRemovedMessage)))
                    .andExpect(jsonPath("messages[1].author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[1].subject.id", is(removedParticipantId)))
                    .andExpect(jsonPath("messages[1].unread", is(false)))
                    .andExpect(jsonPath("messages[1].system", is(true)))
                    .andExpect(jsonPath("type", is(CONFERENCE)));
    }

    @Test
    public void testRemoveParticipantFromNonExistentConversation() throws Exception {
        Long authorId = 1L;
        int removedParticipantId = 4;
        int nonExistentConversationId = 444;
        entityHelper.createParticipant(1L, "name", "CLUB", "ANTIQUITY");
        entityHelper.createParticipant(4L, "name", "CLUB", "ANTIQUITY");
        String url = "/api/conversations/" + nonExistentConversationId + "/participants/" + removedParticipantId;
        String userInfoValue = buildUserInfoValue(authorId);

        mvc.perform(delete(url)
                .contentType(APPLICATION_JSON_VALUE)
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(equalTo("No conversation with id 444 found.")));
    }

    @Test
    public void testRemoveParticipantFromConversationOf2() throws Exception {
        Long authorId = 1L;
        int removedParticipantId = 4;

        Participant author = entityHelper.createParticipant(authorId, "name", "CLUB", "ANTIQUITY");
        Set<Participant> receivers = ImmutableSet.of(
                entityHelper.createParticipant(4L, "name", "CLUB", "ANTIQUITY")
        );

        Conversation conversation = entityHelper.createConversation(author, receivers, "text", "");
        String url = "/api/conversations/" + conversation.getId() + "/participants/" + removedParticipantId;
        String userInfoValue = buildUserInfoValue(authorId);

        mvc.perform(delete(url)
                .contentType(APPLICATION_JSON_VALUE)
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("errors.participants", is("Conversation may contain from 2 to 20 participants")));
    }

    @Test
    public void testCountNewConversations() throws Exception {
        Long authorId = 1L;
        Long receiverId = 2L;
        String text = "text";
        String name = "name";

        Participant author = entityHelper.createParticipant(authorId, "name", "CLUB", "ANTIQUITY");
        Set<Participant> receivers = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(receiverId, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(3L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(4L, "name", "CLUB", "ANTIQUITY")
        ));

        entityHelper.createConversation(author, receivers, text, name);
        entityHelper.createConversation(author, receivers, text, name);
        entityHelper.createConversation(author, receivers, text, name);
        String userInfoValue = buildUserInfoValue(receiverId);

        mvc.perform(head("/api/conversations")
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(header().string(COUNT_HEADER, String.valueOf(3)));
    }

    @Test
    public void testCountNewConversationsWhenRemovingTheUserFromConversation() throws Exception {
        Long authorId = 1L;
        Long receiverId = 2L;
        String text = "text";
        String name = "name";

        Participant author = entityHelper.createParticipant(authorId, "name", "CLUB", "ANTIQUITY");
        Set<Participant> receivers = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(receiverId, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(3L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(4L, "name", "CLUB", "ANTIQUITY")
        ));

        Conversation conversation = entityHelper.createConversation(author, receivers, text, name);
        entityHelper.createConversation(author, receivers, text, name);
        entityHelper.createConversation(author, receivers, text, name);
        String userInfoValue = buildUserInfoValue(receiverId);

        //removing the receiver from the first conversation
        mvc.perform(delete("/api/conversations/" + conversation.getId() + "/participants/" + receiverId)
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN));

        //verifying if only 2 conversations of 3 found
        mvc.perform(head("/api/conversations")
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                .andExpect(status().isOk())
                .andExpect(header().string(COUNT_HEADER, String.valueOf(2)));
    }

    @Test
    public void testUpdateConversationName() throws Exception {
        Long authorId = 1L;
        String text = "text";
        String name = "name";
        String newName = "new name";
        String conversationRenamedMessage = "renamed the conversation to \"" + newName + "\"";

        Participant author = entityHelper.createParticipant(authorId, "name", "CLUB", "ANTIQUITY");
        Set<Participant> receivers = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(2L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(3L, "name", "CLUB", "ANTIQUITY")
        ));

        Conversation conversation = entityHelper.createConversation(author, receivers, text, name);
        Long conversationId = conversation.getId();

        UpdateConversationNameDto updateConversationNameDto = new UpdateConversationNameDto();
        updateConversationNameDto.setName(newName);
        String updateConversationNameJson = mapper.writeValueAsString(updateConversationNameDto);
        String userInfoValue = buildUserInfoValue(authorId);

        mvc.perform(patch("/api/conversations/" + conversationId + "/name")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(updateConversationNameJson)
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("id", is(conversationId.intValue())))
                    .andExpect(jsonPath("admin.id", is(authorId.intValue())))
                    .andExpect(jsonPath("name", is(newName)))
                    .andExpect(jsonPath("participants", hasSize(3)))
                    .andExpect(jsonPath("messages", hasSize(2)))
                    .andExpect(jsonPath("messages[0].text", is(text)))
                    .andExpect(jsonPath("messages[0].author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[0].subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("messages[0].unread", is(false)))
                    .andExpect(jsonPath("messages[0].system", is(false)))
                    .andExpect(jsonPath("messages[1].text", is(conversationRenamedMessage)))
                    .andExpect(jsonPath("messages[1].author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[1].unread", is(false)))
                    .andExpect(jsonPath("messages[1].system", is(true)))
                    .andExpect(jsonPath("type", is(CONFERENCE)));
    }

    @Test
    public void testReadMessages() throws Exception {
        Long authorId = 1L;
        Long receiver1Id = 2L;
        String text = "text";
        String name = "name";

        Participant author = entityHelper.createParticipant(authorId, "name", "CLUB", "ANTIQUITY");
        Set<Participant> receivers = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(4L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(2L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(3L, "name", "CLUB", "ANTIQUITY")
        ));

        Conversation conversation = entityHelper.createConversation(author, receivers, text, name);
        Long conversationId = conversation.getId();
        List<Message> messages = conversation.getMessages();
        List<Long> messagesIds = messages.stream().map(Message::getId).collect(toList());

        ReadMessagesDto readMessagesDto = new ReadMessagesDto();
        readMessagesDto.setMessages(messagesIds);
        String readMessagesJson = mapper.writeValueAsString(readMessagesDto);
        String userInfoValue = buildUserInfoValue(receiver1Id);

        mvc.perform(post("/api/conversations/" + conversationId + "/read")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(readMessagesJson)
                .header(USER_INFO_HEADER, userInfoValue)
                .header(AUTHORIZATION_HEADER, TOKEN))
                    .andExpect(status().isOk());
    }

    private String getCreateConversationDtoJson(Set<Long> receivers, String text, String name) throws Exception {
        CreateConversationDto createConversationDto = new CreateConversationDto();
        createConversationDto.setReceivers(receivers);
        createConversationDto.setText(text);
        createConversationDto.setName(name);

        return mapper.writeValueAsString(createConversationDto);
    }

    private String getAddMessageDtoJson(String text) throws Exception {
        AddMessageDto addMessageDto = new AddMessageDto();
        addMessageDto.setText(text);
        return mapper.writeValueAsString(addMessageDto);
    }

    private String buildUserInfoValue(Long profileId) throws Exception {
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setProfileId(profileId);
        userInfoDto.setLang("en");
        userInfoDto.setTimezone("UTC");
        return mapper.writeValueAsString(userInfoDto);
    }
}
