package club.tempvs.message.controller;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
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
        Set<Long> receivers = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        String message = "myMessage";
        String name = "conversation name";
        String createConversationJson = getCreateConversationDtoJson(authorId, receivers, message, name);

        entityHelper.createParticipant(1L, "name1");
        entityHelper.createParticipant(2L, "name2");
        entityHelper.createParticipant(3L, "name3");
        entityHelper.createParticipant(4L, "name4");

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
        Set<Long> receivers = new HashSet<>(Arrays.asList(receiverId));
        String oldMessage = "my old message";
        String newMessage = "my new message";
        String name = null;
        entityHelper.createConversation(authorId, receivers, oldMessage, name);
        String createConversationJson = getCreateConversationDtoJson(authorId, receivers, newMessage, name);

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
        Set<Long> receivers = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        String message = "myMessage";
        String name = "conversation name";
        String createConversationJson = getCreateConversationDtoJson(null, receivers, message, name);

        mvc.perform(post("/api/conversations")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson)
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Participant id is missing."));
    }

    @Test
    public void testCreateConversationWithAuthorEqualReceiver() throws Exception {
        Long author = 1L;
        Set<Long> receivers = new HashSet<>(Arrays.asList(1L));
        String message = "myMessage";
        String name = "conversation name";
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
        Set<Long> receivers = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        String name = "conversation name";
        String createConversationJson = getCreateConversationDtoJson(authorId, receivers, null, name);

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
        Set<Long> receivers = new HashSet<>();
        String message = "myMessage";
        String name = "conversation name";
        String createConversationJson = getCreateConversationDtoJson(authorId, receivers, message, name);

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
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(2L, 3L, 4L));
        String text = "text";
        String name = "name";

        Conversation conversation = entityHelper.createConversation(authorId, receiverIds, text, name);
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
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(2L, 3L, 4L));
        String text = "text";
        String name = "name";

        Conversation conversation = entityHelper.createConversation(authorId, receiverIds, text, name);
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
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(2L, 3L, 4L));
        String text = "text";
        String name = "name";

        Conversation conversation = entityHelper.createConversation(authorId, receiverIds, text, name);
        Long conversationId = conversation.getId();

        mvc.perform(get("/api/conversations/" + conversationId + "?page=0&size=20")
                .header("Authorization",TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("'caller' parameter is missing."));
    }

    @Test
    public void testGetConversationForWrongCaller() throws Exception {
        Long authorId = 1L;
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(2L, 3L, 4L));
        Long wrongCallerId = 5L;
        String text = "text";
        String name = "name";

        entityHelper.createParticipant(wrongCallerId, "name");
        Conversation conversation = entityHelper.createConversation(authorId, receiverIds, text, name);
        Long conversationId = conversation.getId();

        mvc.perform(get("/api/conversations/" + conversationId + "?page=0&size=20&caller=" + wrongCallerId)
                .header("Authorization", TOKEN))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Participant " + wrongCallerId + " has no access to conversation " + conversationId));
    }

    @Test
    public void testGetConversationsByParticipant() throws Exception {
        Long authorId = 10L;
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(2L));
        String text = "text";
        String name = "name";

        Conversation conversation = entityHelper.createConversation(authorId, receiverIds, text, name);
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
                .andExpect(jsonPath("conversations[0].type", is(DIALOGUE)))
                .andExpect(jsonPath("conversations[0].conversant", is("name")))
                .andExpect(header().string(COUNT_HEADER, String.valueOf(1)));
    }

    @Test
    public void testGetConversationsByParticipantWithMultipleConversants() throws Exception {
        Long authorId = 10L;
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(2L, 3L, 4L));
        String text = "text";
        String name = "name";

        Conversation conversation = entityHelper.createConversation(authorId, receiverIds, text, name);
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
        entityHelper.createParticipant(1L, "name");

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
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(2L, 3L, 4L));
        String text = "text";
        String name = "name";
        String newMessageText = "new message text";

        Conversation conversation = entityHelper.createConversation(authorId, receiverIds, text, name);
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
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(receiverId));
        String text = "an initial text";
        Long addedParticipantId = 3L;
        Set<Long> participantIds = new HashSet<>(receiverIds);
        participantIds.add(authorId);
        participantIds.add(addedParticipantId);

        entityHelper.createParticipant(authorId, "");
        entityHelper.createParticipant(receiverId, "");
        entityHelper.createParticipant(addedParticipantId, "");
        Conversation conversation = entityHelper.createConversation(authorId, receiverIds, text, null);
        Long initialConversationId = conversation.getId();
        String conferenceCreatedMessage = "created a conference";

        AddParticipantsDto addParticipantsDto = new AddParticipantsDto();
        addParticipantsDto.setInitiator(new ParticipantDto(authorId, "name"));
        addParticipantsDto.setSubjects(Arrays.asList(new ParticipantDto(addedParticipantId, "name")));

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
    public void testAddParticipantAlreadyPresentInDialogue() throws Exception {
        Long authorId = 1L;
        Long receiverId = 2L;
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(receiverId));
        String text = "an initial text";

        entityHelper.createParticipant(authorId, "");
        entityHelper.createParticipant(receiverId, "");
        Conversation conversation = entityHelper.createConversation(authorId, receiverIds, text, null);
        Long initialConversationId = conversation.getId();

        AddParticipantsDto addParticipantsDto = new AddParticipantsDto();
        addParticipantsDto.setInitiator(new ParticipantDto(authorId, "name"));
        addParticipantsDto.setSubjects(Arrays.asList(new ParticipantDto(receiverId, "name")));

        String addParticipantJson = mapper.writeValueAsString(addParticipantsDto);

        mvc.perform(post("/api/conversations/" + initialConversationId + "/participants")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addParticipantJson)
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("The participant being added is already present in the conversation."));
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

        entityHelper.createParticipant(1L, "");
        entityHelper.createParticipant(2L, "");
        entityHelper.createParticipant(3L, "");
        entityHelper.createParticipant(4L, "");
        entityHelper.createParticipant(5L, "");
        Conversation conversation = entityHelper.createConversation(authorId, receiverIds, text, name);
        Long conversationId = conversation.getId();
        int messagesInitialSize = conversation.getMessages().size();
        String participantAddedMessage = "added";

        AddParticipantsDto addParticipantsDto = new AddParticipantsDto();
        addParticipantsDto.setInitiator(new ParticipantDto(authorId, "name"));
        addParticipantsDto.setSubjects(Arrays.asList(new ParticipantDto(addedParticipantId, "name")));

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
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(2L, 3L, 4L));
        String text = "text";
        String name = "name";
        Long addedParticipantId = 5L;
        Set<Long> participantIds = new HashSet<>(receiverIds);
        participantIds.add(authorId);
        participantIds.add(addedParticipantId);

        Conversation conversation = entityHelper.createConversation(authorId, receiverIds, text, name);
        Long conversationId = conversation.getId();

        AddParticipantsDto addParticipantsDto = new AddParticipantsDto();
        addParticipantsDto.setInitiator(new ParticipantDto(authorId, "name"));
        addParticipantsDto.setSubjects(Arrays.asList(new ParticipantDto(addedParticipantId, "name")));

        String addParticipantJson = mapper.writeValueAsString(addParticipantsDto);

        mvc.perform(post("/api/conversations/" + conversationId + "/participants")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addParticipantJson)
                .header("Authorization",TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Non-existent participants are being added to a conversation.")));
    }

    @Test
    public void testRemoveParticipant() throws Exception {
        Long authorId = 1L;
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(2L, 3L, 4L));
        String text = "text";
        String name = "name";
        Long removedParticipantId = 4L;
        Set<Long> participantIds = new HashSet<>(receiverIds);
        participantIds.add(authorId);
        participantIds.add(removedParticipantId);

        Conversation conversation = entityHelper.createConversation(authorId, receiverIds, text, name);
        Long conversationId = conversation.getId();
        int messagesInitialSize = conversation.getMessages().size();
        String participantRemovedMessage = "removed";

        AddParticipantsDto addParticipantsDto = new AddParticipantsDto();
        addParticipantsDto.setInitiator(new ParticipantDto(authorId, "name"));
        addParticipantsDto.setSubjects(Arrays.asList(new ParticipantDto(removedParticipantId, "name")));

        String addParticipantJson = mapper.writeValueAsString(addParticipantsDto);

        String url = "/api/conversations/" + conversationId + "/participants/" + removedParticipantId + "?initiator=" + authorId;

        mvc.perform(delete(url)
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addParticipantJson)
                .header("Authorization",TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("id", is(conversationId.intValue())))
                    .andExpect(jsonPath("admin.id", is(authorId.intValue())))
                    .andExpect(jsonPath("participants", hasSize(participantIds.size() - 1)))
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

        AddParticipantsDto addParticipantsDto = new AddParticipantsDto();
        addParticipantsDto.setInitiator(new ParticipantDto(authorId, "name"));
        addParticipantsDto.setSubjects(Arrays.asList(new ParticipantDto(removedParticipantId, "name")));

        String addParticipantJson = mapper.writeValueAsString(addParticipantsDto);

        mvc.perform(post("/api/conversations/" + nonExistentConversationId + "/participants")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addParticipantJson)
                .header("Authorization",TOKEN))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(equalTo("Conversation with id '444' has not been found.")));
    }

    @Test
    public void testCountNewConversations() throws Exception {
        Long authorId = 1L;
        Long receiver1Id = 2L;
        Long receiver2Id = 3L;
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(receiver1Id, receiver2Id));
        String text = "text";
        String name = "name";

        entityHelper.createConversation(authorId, receiverIds, text, name);
        entityHelper.createConversation(authorId, receiverIds, text, name);
        entityHelper.createConversation(authorId, receiverIds, text, name);

        mvc.perform(head("/api/conversations?participant=" + receiver1Id)
                .header("Authorization",TOKEN))
                .andExpect(status().isOk())
                .andExpect(header().string(COUNT_HEADER, String.valueOf(3)));
    }

    @Test
    public void testUpdateConversationName() throws Exception {
        Long authorId = 1L;
        String authorName = "author name";
        Long receiver1Id = 2L;
        Long receiver2Id = 3L;
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(receiver1Id, receiver2Id));
        String text = "text";
        String name = "name";
        String newName = "new name";
        String conversationRenamedMessage = "renamed a conversation to \"" + newName + "\"";

        Conversation conversation = entityHelper.createConversation(authorId, receiverIds, text, name);
        Long conversationId = conversation.getId();

        UpdateConversationNameDto updateConversationNameDto = new UpdateConversationNameDto();
        updateConversationNameDto.setName(newName);
        updateConversationNameDto.setInitiator(new ParticipantDto(authorId, authorName));
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
        Long receiver2Id = 3L;
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(receiver1Id, receiver2Id));
        String text = "text";
        String name = "name";

        Conversation conversation = entityHelper.createConversation(authorId, receiverIds, text, name);
        Long conversationId = conversation.getId();
        List<Message> messages = conversation.getMessages();
        List<Long> messagesIds = messages.stream().map(Message::getId).collect(toList());

        ReadMessagesDto readMessagesDto = new ReadMessagesDto();
        readMessagesDto.setParticipant(new ParticipantDto(receiver1Id, null));
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
            Long authorId, Set<Long> receivers, String text, String name) throws Exception {
        CreateConversationDto createConversationDto = new CreateConversationDto();
        createConversationDto.setAuthor(new ParticipantDto(authorId, "name"));
        createConversationDto.setReceivers(receivers.stream().map(id -> new ParticipantDto(id, "name")).collect(toSet()));
        createConversationDto.setText(text);
        createConversationDto.setName(name);

        return mapper.writeValueAsString(createConversationDto);
    }

    private String getAddMessageDtoJson(Long authorId, String text) throws Exception {
        AddMessageDto addMessageDto = new AddMessageDto();
        addMessageDto.setAuthor(new ParticipantDto(authorId, "name"));
        addMessageDto.setText(text);
        return mapper.writeValueAsString(addMessageDto);
    }
}
