package club.tempvs.message.controller;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.*;
import club.tempvs.message.util.EntityHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.*;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;

import static org.springframework.http.MediaType.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ConversationControllerIntegrationTest {

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
    public void testGetPong() throws Exception {
        mvc.perform(get("/api/ping").accept(TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("pong!")));
    }

    @Test
    public void testCreateConversation() throws Exception {
        Long authorId = 4L;
        String message = "myMessage";
        String name = "conversation name";

        Participant author = entityHelper.createParticipant(authorId, "name1", "USER", "");
        Set<Participant> receivers = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(2L, "name2", "USER", ""),
                entityHelper.createParticipant(3L, "name3", "USER", ""),
                entityHelper.createParticipant(1L, "name4", "USER", "")
        ));

        String createConversationJson = getCreateConversationDtoJson(author, receivers, message, name);

        mvc.perform(post("/api/conversations")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson)
                .header("Authorization",TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("participants", hasSize(4)))
                    .andExpect(jsonPath("admin.id", is(authorId.intValue())))
                    .andExpect(jsonPath("messages", hasSize(1)))
                    .andExpect(jsonPath("messages[0].text", is(message)))
                    .andExpect(jsonPath("messages[0].author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[0].subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("messages[0].unread", is(false)))
                    .andExpect(jsonPath("messages[0].system", is(false)))
                    .andExpect(jsonPath("lastMessage.text", is(message)))
                    .andExpect(jsonPath("lastMessage.author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("lastMessage.unread", is(false)))
                    .andExpect(jsonPath("lastMessage.system", is(false)))
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
        Set<Participant> receivers = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(receiverId, "name", "CLUB", "ANTIQUITY")
        ));

        entityHelper.createConversation(author, receivers, oldMessage, name);
        String createConversationJson = getCreateConversationDtoJson(author, receivers, newMessage, name);

        mvc.perform(post("/api/conversations")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson)
                .header("Authorization",TOKEN))
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
                .andExpect(jsonPath("lastMessage.text", is(newMessage)))
                .andExpect(jsonPath("lastMessage.author.id", is(authorId.intValue())))
                .andExpect(jsonPath("lastMessage.unread", is(false)))
                .andExpect(jsonPath("lastMessage.system", is(false)))
                .andExpect(jsonPath("type", is(DIALOGUE)));
    }

    @Test
    public void testCreateConversationWithNoAuthor() throws Exception {
        String message = "myMessage";
        String name = "conversation name";

        Set<Participant> receivers = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(1L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(2L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(3L, "name", "CLUB", "ANTIQUITY")
        ));

        String createConversationJson = getCreateConversationDtoJson(null, receivers, message, name);

        mvc.perform(post("/api/conversations")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson)
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Author id is missing."));
    }

    @Test
    public void testCreateConversationWithAuthorEqualReceiver() throws Exception {
        Long authorId = 1L;
        String message = "myMessage";
        String name = "conversation name";

        Participant author = entityHelper.createParticipant(authorId, "name", "CLUB", "ANTIQUITY");
        Set<Participant> receivers = new HashSet<>(Arrays.asList(author));

        String createConversationJson = getCreateConversationDtoJson(author, receivers, message, name);

        mvc.perform(post("/api/conversations")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson)
                .header("Authorization",TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Conversation must contain at least 2 participants.")));
    }

    @Test
    public void testCreateConversationWithNoMessage() throws Exception {
        Long authorId = 4L;
        String name = "conversation name";

        Participant author = entityHelper.createParticipant(authorId, "name", "CLUB", "ANTIQUITY");
        Set<Participant> receivers = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(1L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(2L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(3L, "name", "CLUB", "ANTIQUITY")
        ));

        String createConversationJson = getCreateConversationDtoJson(author, receivers, null, name);

        mvc.perform(post("/api/conversations")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson)
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(equalTo("Text is missing.")));
    }

    @Test
    public void testCreateConversationWithNoReceivers() throws Exception {
        Long authorId = 4L;
        Set<Participant> receivers = new HashSet<>();
        String message = "myMessage";
        String name = "conversation name";

        Participant author = entityHelper.createParticipant(authorId, "name", "CLUB", "ANTIQUITY");

        String createConversationJson = getCreateConversationDtoJson(author, receivers, message, name);

        mvc.perform(post("/api/conversations")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson)
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(equalTo("Receivers list is empty.")));
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
        Boolean isSystem = messages.get(0).getSystem();

        mvc.perform(get("/api/conversations/" + conversationId + "?caller=" + authorId)
                .header("Authorization",TOKEN))
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
                    .andExpect(jsonPath("lastMessage.id", is(messageId.intValue())))
                    .andExpect(jsonPath("lastMessage.text", is(text)))
                    .andExpect(jsonPath("lastMessage.author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("lastMessage.unread", is(false)))
                    .andExpect(jsonPath("lastMessage.system", is(isSystem)))
                    .andExpect(jsonPath("type", is(CONFERENCE)));
    }

    @Test
    public void testGetConversationForInvalidPaging() throws Exception {
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

        mvc.perform(get("/api/conversations/" + conversationId + "?page=0&size=-1&caller=" + authorId)
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Page size must not be less than one!"));

        mvc.perform(get("/api/conversations/" + conversationId + "?page=0&size=0&caller=" + authorId)
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Page size must not be less than one!"));

        mvc.perform(get("/api/conversations/" + conversationId + "?page=-1&size=20&caller=" + authorId)
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Page index must not be less than zero!"));

        mvc.perform(get("/api/conversations/" + conversationId + "?page=0&size=50&caller=" + authorId)
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Page size must not be larger than 40!"));
    }

    @Test
    public void testGetConversationForNoCallerSpecified() throws Exception {
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

        mvc.perform(get("/api/conversations/" + conversationId + "?page=0&size=20")
                .header("Authorization",TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("'caller' parameter is missing."));
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

        mvc.perform(get("/api/conversations/" + conversationId + "?page=0&size=20&caller=" + wrongCallerId)
                .header("Authorization", TOKEN))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Participant " + wrongCallerId + " has no access to conversation " + conversationId));
    }

    @Test
    public void testGetConversationsByParticipant() throws Exception {
        Long authorId1 = 10L;
        Long authorId2 = 15L;
        Long callerId = 3L;
        String text = "text";
        String name = "name";

        Participant author1 = entityHelper.createParticipant(authorId1, "name", "CLUB", "ANTIQUITY");
        Participant author2 = entityHelper.createParticipant(authorId2, "name", "CLUB", "ANTIQUITY");

        Set<Participant> receivers1 = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(authorId2, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(callerId, "name", "CLUB", "ANTIQUITY")
        ));

        Set<Participant> receivers2 = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(callerId, "name", "CLUB", "ANTIQUITY")
        ));

        entityHelper.createConversation(author1, receivers1, text, name);
        entityHelper.createConversation(author2, receivers2, text, name);

        mvc.perform(get("/api/conversations?participant=" + callerId + "&page=0&size=10")
                .header("Authorization",TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("conversations", hasSize(2)))
                    .andExpect(jsonPath("conversations[0].name", is(name)))
                    .andExpect(jsonPath("conversations[0].lastMessage.text", is(text)))
                    .andExpect(jsonPath("conversations[0].lastMessage.author.id", is(authorId2.intValue())))
                    .andExpect(jsonPath("conversations[0].lastMessage.subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("conversations[0].lastMessage.unread", is(true)))
                    .andExpect(jsonPath("conversations[0].lastMessage.system", is(false)))
                    .andExpect(jsonPath("conversations[0].type", is(DIALOGUE)))
                    .andExpect(jsonPath("conversations[0].conversant", is("name")))
                    .andExpect(jsonPath("conversations[0].unreadMessagesCount", is(1)))
                    .andExpect(jsonPath("conversations[1].name", is(name)))
                    .andExpect(jsonPath("conversations[1].lastMessage.text", is(text)))
                    .andExpect(jsonPath("conversations[1].lastMessage.author.id", is(authorId1.intValue())))
                    .andExpect(jsonPath("conversations[1].lastMessage.subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("conversations[1].lastMessage.unread", is(true)))
                    .andExpect(jsonPath("conversations[1].lastMessage.system", is(false)))
                    .andExpect(jsonPath("conversations[1].type", is(CONFERENCE)))
                    .andExpect(jsonPath("conversations[1].conversant", is("name, name")))
                    .andExpect(jsonPath("conversations[1].unreadMessagesCount", is(1)))
                    .andExpect(header().string(COUNT_HEADER, String.valueOf(2)));
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
        Long messageId = messages.get(0).getId();
        Boolean isSystem = messages.get(0).getSystem();

        mvc.perform(get("/api/conversations?participant=" + authorId + "&page=0&size=10")
                .header("Authorization",TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("conversations", hasSize(1)))
                    .andExpect(jsonPath("conversations[0].id", is(conversationId.intValue())))
                    .andExpect(jsonPath("conversations[0].name", is(name)))
                    .andExpect(jsonPath("conversations[0].lastMessage.id", is(messageId.intValue())))
                    .andExpect(jsonPath("conversations[0].lastMessage.text", is(text)))
                    .andExpect(jsonPath("conversations[0].lastMessage.author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("conversations[0].lastMessage.subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("conversations[0].lastMessage.unread", is(false)))
                    .andExpect(jsonPath("conversations[0].lastMessage.system", is(isSystem)))
                    .andExpect(jsonPath("conversations[0].type", is(CONFERENCE)))
                    .andExpect(jsonPath("conversations[0].conversant", is("name, name, name")))
                    .andExpect(header().string(COUNT_HEADER, String.valueOf(1)));
    }

    @Test
    public void testGetConversationsByParticipantForInvalidInput() throws Exception {
        entityHelper.createParticipant(1L, "name", "USER", "");

        mvc.perform(get("/api/conversations?participant=1&page=0&size=-1")
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(equalTo("Page size must not be less than one!")));

        mvc.perform(get("/api/conversations?participant=1&page=0&size=0")
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(equalTo("Page size must not be less than one!")));

        mvc.perform(get("/api/conversations?participant=1&page=-1&size=20")
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(equalTo("Page index must not be less than zero!")));

        mvc.perform(get("/api/conversations?participant=1&page=0&size=50")
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(equalTo("Page size must not be larger than 40!")));

        mvc.perform(get("/api/conversations?participant=2&page=0&size=20")
                .header("Authorization",TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("No participant with id 2 exist!")));
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
        String addMessageJson = getAddMessageDtoJson(authorId, newMessageText);

        mvc.perform(post("/api/conversations/" + conversationId + "/messages")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addMessageJson)
                .header("Authorization",TOKEN))
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
                    .andExpect(jsonPath("lastMessage.id", is(messageId.intValue() + 1)))
                    .andExpect(jsonPath("lastMessage.text", is(newMessageText)))
                    .andExpect(jsonPath("lastMessage.author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("lastMessage.unread", is(false)))
                    .andExpect(jsonPath("lastMessage.system", is(false)))
                    .andExpect(jsonPath("type", is(CONFERENCE)));
    }

    @Test
    public void testAddMessageForMissingConversationInDB() throws Exception {
        Long authorId = 1L;
        String newMessageText = "new message text";
        Long missingConversationId = 2L;

        String addMessageJson = getAddMessageDtoJson(authorId, newMessageText);

        mvc.perform(post("/api/conversations/" + missingConversationId + "/messages")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addMessageJson)
                .header("Authorization",TOKEN))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(equalTo("Conversation with id 2 doesn't exist.")));
    }

    @Test
    public void testAddParticipantToDialogue() throws Exception {
        Long authorId = 1L;
        Long receiverId = 2L;
        String text = "an initial text";
        Long addedParticipantId = 3L;

        Participant author = entityHelper.createParticipant(authorId, "name", "USER", "");
        Set<Participant> receivers = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(receiverId, "name", "USER", "")
        ));

        Conversation conversation = entityHelper.createConversation(author, receivers, text, "");
        Long initialConversationId = conversation.getId();
        String conferenceCreatedMessage = "created a conference";

        entityHelper.createParticipant(addedParticipantId, "name", "USER", "");

        AddParticipantsDto addParticipantsDto = new AddParticipantsDto();
        addParticipantsDto.setInitiator(new ParticipantDto(authorId, "name", "USER", ""));
        addParticipantsDto.setSubjects(new HashSet<>(
                Arrays.asList(new ParticipantDto(addedParticipantId, "name", "USER", ""))));

        String addParticipantJson = mapper.writeValueAsString(addParticipantsDto);

        mvc.perform(post("/api/conversations/" + initialConversationId + "/participants")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addParticipantJson)
                .header("Authorization",TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("id", not(initialConversationId.intValue())))
                    .andExpect(jsonPath("participants", hasSize(3)))
                    .andExpect(jsonPath("messages", hasSize(1)))
                    .andExpect(jsonPath("messages[0].text", is(conferenceCreatedMessage)))
                    .andExpect(jsonPath("messages[0].author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[0].unread", is(false)))
                    .andExpect(jsonPath("messages[0].system", is(true)))
                    .andExpect(jsonPath("lastMessage.text", is(conferenceCreatedMessage)))
                    .andExpect(jsonPath("lastMessage.author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("lastMessage.unread", is(false)))
                    .andExpect(jsonPath("lastMessage.system", is(true)))
                    .andExpect(jsonPath("type", is(CONFERENCE)));
    }

    @Test
    public void testAddParticipantToDialogueForExistentOne() throws Exception {
        Long authorId = 1L;
        Long receiverId = 2L;
        Long addedParticipantId = 2L;

        Participant author = entityHelper.createParticipant(authorId, "name", "USER", "");
        Set<Participant> receivers = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(receiverId, "name", "USER", "")
        ));

        Conversation conversation = entityHelper.createConversation(author, receivers, "", "");
        Long initialConversationId = conversation.getId();

        entityHelper.createParticipant(addedParticipantId, "name", "USER", "");

        AddParticipantsDto addParticipantsDto = new AddParticipantsDto();
        addParticipantsDto.setInitiator(new ParticipantDto(authorId, "name", "USER", ""));
        addParticipantsDto.setSubjects(new HashSet<>(
                Arrays.asList(new ParticipantDto(addedParticipantId, "name", "USER", ""))));

        String addParticipantJson = mapper.writeValueAsString(addParticipantsDto);

        mvc.perform(post("/api/conversations/" + initialConversationId + "/participants")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addParticipantJson)
                .header("Authorization",TOKEN))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An existent member is being added to a conversation."));
    }

    @Test
    public void testAddParticipantToDialogueForMismatchingPeriods() throws Exception {
        Long authorId = 1L;
        Long addedParticipantId = 33L;

        Set<Participant> receivers = LongStream.rangeClosed(2L, 22L).boxed()
                .map(i -> entityHelper.createParticipant(i, "name", "CLUB", "EARLY_MIDDLE_AGES"))
                .collect(toSet());

        Participant author = entityHelper.createParticipant(authorId, "name", "CLUB", "EARLY_MIDDLE_AGES");

        Conversation conversation = entityHelper.createConversation(author, receivers, "an initial text", "");
        Long conversationId = conversation.getId();

        entityHelper.createParticipant(addedParticipantId, "name", "CLUB", "LATE_MIDDLE_AGES");

        AddParticipantsDto addParticipantsDto = new AddParticipantsDto();
        addParticipantsDto.setInitiator(new ParticipantDto(authorId, "name", "CLUB", "EARLY_MIDDLE_AGES"));
        addParticipantsDto.setSubjects(new HashSet<>(
                Arrays.asList(new ParticipantDto(addedParticipantId, "name", "CLUB", "LATE_MIDDLE_AGES"))));

        String addParticipantJson = mapper.writeValueAsString(addParticipantsDto);

        mvc.perform(post("/api/conversations/" + conversationId + "/participants")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addParticipantJson)
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("errors.participants",
                            is("Conversation may contain 2 min and 20 max participants" +
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
        addParticipantsDto.setInitiator(new ParticipantDto(authorId, "name", "USER", ""));
        addParticipantsDto.setSubjects(new HashSet<>(
                Arrays.asList(new ParticipantDto(addedParticipantId, "name", "USER", ""))));

        String addParticipantJson = mapper.writeValueAsString(addParticipantsDto);

        mvc.perform(post("/api/conversations/" + conversationId + "/participants")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addParticipantJson)
                .header("Authorization",TOKEN))
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
                    .andExpect(jsonPath("lastMessage.text", is(participantAddedMessage)))
                    .andExpect(jsonPath("lastMessage.author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("lastMessage.unread", is(false)))
                    .andExpect(jsonPath("lastMessage.system", is(true)))
                    .andExpect(jsonPath("lastMessage.subject.id", is(addedParticipantId.intValue())))
                    .andExpect(jsonPath("type", is(CONFERENCE)));
    }

    @Test
    public void testAddParticipantWithNotExistentSubject() throws Exception {
        Long authorId = 1L;
        Long addedParticipantId = 5L;

        Participant author = entityHelper.createParticipant(authorId, "name", "USER", "");
        Participant receiver = entityHelper.createParticipant(2L, "name", "USER", "");
        Set<Participant> receivers = new HashSet<>(Arrays.asList(receiver));

        Conversation conversation = entityHelper.createConversation(author, receivers, "text", "name");
        Long conversationId = conversation.getId();

        AddParticipantsDto addParticipantsDto = new AddParticipantsDto();
        addParticipantsDto.setInitiator(new ParticipantDto(authorId, "name", "USER", null));
        addParticipantsDto.setSubjects(new HashSet<>(
                Arrays.asList(new ParticipantDto(addedParticipantId, "name", "USER", null))));

        String addParticipantJson = mapper.writeValueAsString(addParticipantsDto);

        mvc.perform(post("/api/conversations/" + conversationId + "/participants")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addParticipantJson)
                .header("Authorization",TOKEN))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(equalTo("No subjects found in database")));
    }

    @Test
    public void testRemoveParticipant() throws Exception {
        Long authorId = 1L;
        String text = "text";
        String name = "name";
        Long removedParticipantId = 4L;

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
        String url = "/api/conversations/" + conversationId + "/participants/" + removedParticipantId + "?initiator=" + authorId;

        mvc.perform(delete(url)
                .contentType(APPLICATION_JSON_VALUE)
                .header("Authorization",TOKEN))
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
                    .andExpect(jsonPath("messages[1].subject.id", is(removedParticipantId.intValue())))
                    .andExpect(jsonPath("messages[1].unread", is(false)))
                    .andExpect(jsonPath("messages[1].system", is(true)))
                    .andExpect(jsonPath("lastMessage.text", is(participantRemovedMessage)))
                    .andExpect(jsonPath("lastMessage.author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("lastMessage.unread", is(false)))
                    .andExpect(jsonPath("lastMessage.system", is(true)))
                    .andExpect(jsonPath("lastMessage.subject.id", is(removedParticipantId.intValue())))
                    .andExpect(jsonPath("type", is(CONFERENCE)));
    }

    @Test
    public void testRemoveParticipantFromNonExistentConversation() throws Exception {
        Long authorId = 1L;
        Long removedParticipantId = 4L;
        Long nonExistentConversationId = 444L;
        String url = "/api/conversations/" + nonExistentConversationId + "/participants/" +
                removedParticipantId + "?initiator=" + authorId;

        mvc.perform(delete(url)
                .contentType(APPLICATION_JSON_VALUE)
                .header("Authorization",TOKEN))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(equalTo("Conversation with id '444' has not been found.")));
    }

    @Test
    public void testRemoveParticipantFromConversationOf2() throws Exception {
        Long authorId = 1L;
        Long removedParticipantId = 4L;

        Participant author = entityHelper.createParticipant(authorId, "name", "CLUB", "ANTIQUITY");
        Set<Participant> receivers = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(4L, "name", "CLUB", "ANTIQUITY")
        ));

        Conversation conversation = entityHelper.createConversation(author, receivers, "text", "");
        String url = "/api/conversations/" + conversation.getId() + "/participants/" +
                removedParticipantId + "?initiator=" + authorId;

        mvc.perform(delete(url)
                .contentType(APPLICATION_JSON_VALUE)
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("errors.participants", is("Conversation may not contain less than 2 participants.")));
    }

    @Test
    public void testCountNewConversations() throws Exception {
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

        entityHelper.createConversation(author, receivers, text, name);
        entityHelper.createConversation(author, receivers, text, name);
        entityHelper.createConversation(author, receivers, text, name);

        mvc.perform(head("/api/conversations?participant=" + receiver1Id)
                .header("Authorization",TOKEN))
                .andExpect(status().isOk())
                .andExpect(header().string(COUNT_HEADER, String.valueOf(3)));
    }

    @Test
    public void testUpdateConversationName() throws Exception {
        Long authorId = 1L;
        String authorName = "author name";
        String text = "text";
        String name = "name";
        String newName = "new name";
        String conversationRenamedMessage = "renamed a conversation to \"" + newName + "\"";

        Participant author = entityHelper.createParticipant(authorId, "name", "CLUB", "ANTIQUITY");
        Set<Participant> receivers = new HashSet<>(Arrays.asList(
                entityHelper.createParticipant(2L, "name", "CLUB", "ANTIQUITY"),
                entityHelper.createParticipant(3L, "name", "CLUB", "ANTIQUITY")
        ));

        Conversation conversation = entityHelper.createConversation(author, receivers, text, name);
        Long conversationId = conversation.getId();

        UpdateConversationNameDto updateConversationNameDto = new UpdateConversationNameDto();
        updateConversationNameDto.setName(newName);
        updateConversationNameDto.setInitiator(new ParticipantDto(authorId, authorName, "USER", null));
        String updateConversationNameJson = mapper.writeValueAsString(updateConversationNameDto);

        mvc.perform(post("/api/conversations/" + conversationId + "/name")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(updateConversationNameJson)
                .header("Authorization",TOKEN))
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
                    .andExpect(jsonPath("lastMessage.text", is(conversationRenamedMessage)))
                    .andExpect(jsonPath("lastMessage.author.id", is(authorId.intValue())))
                    .andExpect(jsonPath("lastMessage.unread", is(false)))
                    .andExpect(jsonPath("lastMessage.system", is(true)))
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
        readMessagesDto.setParticipant(new ParticipantDto(receiver1Id, "name", "USER", null));
        readMessagesDto.setMessageIds(messagesIds);
        String readMessagesJson = mapper.writeValueAsString(readMessagesDto);

        mvc.perform(post("/api/conversations/" + conversationId + "/read")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(readMessagesJson)
                .header("Authorization",TOKEN))
                    .andExpect(status().isOk());
    }

    private String getCreateConversationDtoJson(
            Participant author, Set<Participant> receivers, String text, String name) throws Exception {
        CreateConversationDto createConversationDto = new CreateConversationDto();
        createConversationDto.setAuthor(author != null ? new ParticipantDto(author) : null);
        createConversationDto.setReceivers(receivers.stream().map(ParticipantDto::new).collect(toSet()));
        createConversationDto.setText(text);
        createConversationDto.setName(name);

        return mapper.writeValueAsString(createConversationDto);
    }

    private String getAddMessageDtoJson(Long authorId, String text) throws Exception {
        AddMessageDto addMessageDto = new AddMessageDto();
        addMessageDto.setAuthor(new ParticipantDto(authorId, "name", "USER", null));
        addMessageDto.setText(text);
        return mapper.writeValueAsString(addMessageDto);
    }
}
