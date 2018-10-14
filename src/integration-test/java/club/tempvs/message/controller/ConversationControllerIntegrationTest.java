package club.tempvs.message.controller;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.dto.AddMessageDto;
import club.tempvs.message.dto.CreateConversationDto;
import club.tempvs.message.dto.UpdateParticipantsDto;
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

        entityHelper.createParticipant(1L, "");
        entityHelper.createParticipant(2L, "");
        entityHelper.createParticipant(3L, "");
        entityHelper.createParticipant(4L, "");

        mvc.perform(post("/api/conversations")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson)
                .header("Authorization",TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("participants", is(Arrays.asList(1, 2, 3, 4))))
                    .andExpect(jsonPath("admin", is(authorId.intValue())))
                    .andExpect(jsonPath("messages", hasSize(1)))
                    .andExpect(jsonPath("messages[0].text", is(message)))
                    .andExpect(jsonPath("messages[0].author", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[0].subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("messages[0].newFor", is(Arrays.asList(1, 2, 3, 4))))
                    .andExpect(jsonPath("messages[0].system", is(false)))
                    .andExpect(jsonPath("lastMessage.text", is(message)))
                    .andExpect(jsonPath("lastMessage.author", is(authorId.intValue())))
                    .andExpect(jsonPath("lastMessage.newFor", is(Arrays.asList(1, 2, 3, 4))))
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
                .andExpect(jsonPath("participants", is(Arrays.asList(authorId.intValue(), receiverId.intValue()))))
                .andExpect(jsonPath("admin", isEmptyOrNullString()))
                .andExpect(jsonPath("messages", hasSize(2)))
                .andExpect(jsonPath("messages[0].text", is(oldMessage)))
                .andExpect(jsonPath("messages[0].author", is(authorId.intValue())))
                .andExpect(jsonPath("messages[0].subject", isEmptyOrNullString()))
                .andExpect(jsonPath("messages[0].newFor", is(Arrays.asList(authorId.intValue(), receiverId.intValue()))))
                .andExpect(jsonPath("messages[0].system", is(false)))
                .andExpect(jsonPath("messages[1].text", is(newMessage)))
                .andExpect(jsonPath("messages[1].author", is(authorId.intValue())))
                .andExpect(jsonPath("messages[1].subject", isEmptyOrNullString()))
                .andExpect(jsonPath("messages[1].newFor", is(Arrays.asList(receiverId.intValue()))))
                .andExpect(jsonPath("messages[1].system", is(false)))
                .andExpect(jsonPath("lastMessage.text", is(newMessage)))
                .andExpect(jsonPath("lastMessage.author", is(authorId.intValue())))
                .andExpect(jsonPath("lastMessage.newFor", is(Arrays.asList(receiverId.intValue()))))
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
                    .andExpect(content().string(equalTo("Author id is missing.")));
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
        List<Integer> participantIds = Arrays.asList(1, 2, 3, 4);

        Conversation conversation = entityHelper.createConversation(authorId, receiverIds, text, name);
        Long conversationId = conversation.getId();
        List<Message> messages = conversation.getMessages();
        int messagesSize = messages.size();
        Long messageId = messages.get(0).getId();
        Boolean isSystem = messages.get(0).getSystem();

        mvc.perform(get("/api/conversations/" + conversationId)
                .header("Authorization",TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("id", is(conversationId.intValue())))
                    .andExpect(jsonPath("admin", is(authorId.intValue())))
                    .andExpect(jsonPath("participants", is(participantIds)))
                    .andExpect(jsonPath("messages", hasSize(messagesSize)))
                    .andExpect(jsonPath("messages[0].id", is(messageId.intValue())))
                    .andExpect(jsonPath("messages[0].text", is(text)))
                    .andExpect(jsonPath("messages[0].author", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[0].subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("messages[0].newFor", is(participantIds)))
                    .andExpect(jsonPath("messages[0].system", is(isSystem)))
                    .andExpect(jsonPath("lastMessage.id", is(messageId.intValue())))
                    .andExpect(jsonPath("lastMessage.text", is(text)))
                    .andExpect(jsonPath("lastMessage.author", is(authorId.intValue())))
                    .andExpect(jsonPath("lastMessage.newFor", is(participantIds)))
                    .andExpect(jsonPath("lastMessage.system", is(isSystem)))
                    .andExpect(jsonPath("type", is(CONFERENCE)));
    }

    @Test
    public void testGetConversationForInvalidInput() throws Exception {
        Long authorId = 1L;
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(2L, 3L, 4L));
        String text = "text";
        String name = "name";

        Conversation conversation = entityHelper.createConversation(authorId, receiverIds, text, name);
        Long conversationId = conversation.getId();

        mvc.perform(get("/api/conversations/" + conversationId + "?page=0&size=-1")
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest());

        mvc.perform(get("/api/conversations/" + conversationId + "?page=0&size=0")
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest());

        mvc.perform(get("/api/conversations/" + conversationId + "?page=-1&size=20")
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest());

        mvc.perform(get("/api/conversations/" + conversationId + "?page=0&size=30")
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest());
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
                .andExpect(jsonPath("conversations[0].lastMessage.author", is(authorId.intValue())))
                .andExpect(jsonPath("conversations[0].lastMessage.subject", isEmptyOrNullString()))
                .andExpect(jsonPath("conversations[0].lastMessage.newFor", hasSize(2)))
                .andExpect(jsonPath("conversations[0].lastMessage.system", is(isSystem)))
                .andExpect(jsonPath("conversations[0].type", is(DIALOGUE)))
                .andExpect(jsonPath("conversations[0].conversant", is("name")));
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
                    .andExpect(jsonPath("conversations[0].lastMessage.author", is(authorId.intValue())))
                    .andExpect(jsonPath("conversations[0].lastMessage.subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("conversations[0].lastMessage.newFor", hasSize(4)))
                    .andExpect(jsonPath("conversations[0].lastMessage.system", is(isSystem)))
                    .andExpect(jsonPath("conversations[0].type", is(CONFERENCE)))
                    .andExpect(jsonPath("conversations[0].conversant", is("name, name, name")));
    }

    @Test
    public void testGetConversationsByParticipantForInvalidInput() throws Exception {
        mvc.perform(get("/api/conversations?participant=1&page=0&size=-1")
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest());

        mvc.perform(get("/api/conversations?participant=1&page=0&size=0")
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest());

        mvc.perform(get("/api/conversations?participant=1&page=-1&size=20")
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest());

        mvc.perform(get("/api/conversations?participant=1&page=0&size=30")
                .header("Authorization",TOKEN))
                    .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddMessage() throws Exception {
        Long authorId = 1L;
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(2L, 3L, 4L));
        String text = "text";
        String name = "name";
        String newMessageText = "new message text";
        Boolean isSystem = Boolean.FALSE;

        Conversation conversation = entityHelper.createConversation(authorId, receiverIds, text, name);
        Long conversationId = conversation.getId();
        String addMessageJson = getAddMessageDtoJson(authorId, newMessageText, isSystem);

        mvc.perform(post("/api/conversations/" + conversationId + "/messages")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addMessageJson)
                .header("Authorization",TOKEN))
                    .andExpect(status().isOk());
    }

    @Test
    public void testAddMessageForMissingConversationInDB() throws Exception {
        Long authorId = 1L;
        String newMessageText = "new message text";
        Boolean isSystem = Boolean.FALSE;
        Long missingConversationId = 2L;

        String addMessageJson = getAddMessageDtoJson(authorId, newMessageText, isSystem);

        mvc.perform(post("/api/conversations/" + missingConversationId + "/messages")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addMessageJson)
                .header("Authorization",TOKEN))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(equalTo("Conversation with id 2 doesn't exist.")));
    }

    @Test
    public void testUpdateParticipantsForAdd() throws Exception {
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
        String participantAddedMessage = "conversation.add.participant";

        UpdateParticipantsDto updateParticipantsDto = new UpdateParticipantsDto();
        updateParticipantsDto.setAction(UpdateParticipantsDto.Action.ADD);
        updateParticipantsDto.setInitiator(authorId);
        updateParticipantsDto.setSubject(addedParticipantId);

        String addParticipantJson = mapper.writeValueAsString(updateParticipantsDto);

        mvc.perform(patch("/api/conversations/" + conversationId + "/participants")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addParticipantJson)
                .header("Authorization",TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("id", is(conversationId.intValue())))
                    .andExpect(jsonPath("admin", is(authorId.intValue())))
                    .andExpect(jsonPath("participants", hasSize(participantIds.size())))
                    .andExpect(jsonPath("messages", hasSize(messagesInitialSize + 1)))
                    .andExpect(jsonPath("messages[0].text", is(text)))
                    .andExpect(jsonPath("messages[0].author", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[0].subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("messages[0].newFor", hasSize(receiverIds.size() + 1)))
                    .andExpect(jsonPath("messages[0].system", is(Boolean.FALSE)))
                    .andExpect(jsonPath("messages[1].text", is(participantAddedMessage)))
                    .andExpect(jsonPath("messages[1].author", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[1].subject", is(addedParticipantId.intValue())))
                    .andExpect(jsonPath("messages[1].newFor", hasSize(participantIds.size())))
                    .andExpect(jsonPath("messages[1].system", is(Boolean.TRUE)))
                    .andExpect(jsonPath("lastMessage.text", is(participantAddedMessage)))
                    .andExpect(jsonPath("lastMessage.author", is(authorId.intValue())))
                    .andExpect(jsonPath("lastMessage.newFor", hasSize(participantIds.size())))
                    .andExpect(jsonPath("lastMessage.system", is(Boolean.TRUE)))
                    .andExpect(jsonPath("lastMessage.subject", is(addedParticipantId.intValue())))
                    .andExpect(jsonPath("type", is(CONFERENCE)));
    }

    @Test
    public void testUpdateParticipantsForAddAndNotExistentSubject() throws Exception {
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

        UpdateParticipantsDto updateParticipantsDto = new UpdateParticipantsDto();
        updateParticipantsDto.setAction(UpdateParticipantsDto.Action.ADD);
        updateParticipantsDto.setInitiator(authorId);
        updateParticipantsDto.setSubject(addedParticipantId);

        String addParticipantJson = mapper.writeValueAsString(updateParticipantsDto);

        mvc.perform(patch("/api/conversations/" + conversationId + "/participants")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addParticipantJson)
                .header("Authorization",TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Participant with id 5 does not exist")));
    }

    @Test
    public void testUpdateParticipantsForRemove() throws Exception {
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
        String participantAddedMessage = "conversation.remove.participant";

        UpdateParticipantsDto updateParticipantsDto = new UpdateParticipantsDto();
        updateParticipantsDto.setAction(UpdateParticipantsDto.Action.REMOVE);
        updateParticipantsDto.setInitiator(authorId);
        updateParticipantsDto.setSubject(removedParticipantId);

        String addParticipantJson = mapper.writeValueAsString(updateParticipantsDto);

        mvc.perform(patch("/api/conversations/" + conversationId + "/participants")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addParticipantJson)
                .header("Authorization",TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("id", is(conversationId.intValue())))
                    .andExpect(jsonPath("admin", is(authorId.intValue())))
                    .andExpect(jsonPath("participants", hasSize(participantIds.size() - 1)))
                    .andExpect(jsonPath("messages", hasSize(messagesInitialSize + 1)))
                    .andExpect(jsonPath("messages[0].text", is(text)))
                    .andExpect(jsonPath("messages[0].author", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[0].subject", isEmptyOrNullString()))
                    .andExpect(jsonPath("messages[0].newFor", hasSize(receiverIds.size())))
                    .andExpect(jsonPath("messages[0].system", is(Boolean.FALSE)))
                    .andExpect(jsonPath("messages[1].text", is(participantAddedMessage)))
                    .andExpect(jsonPath("messages[1].author", is(authorId.intValue())))
                    .andExpect(jsonPath("messages[1].subject", is(removedParticipantId.intValue())))
                    .andExpect(jsonPath("messages[1].newFor", hasSize(participantIds.size() - 1)))
                    .andExpect(jsonPath("messages[1].system", is(Boolean.TRUE)))
                    .andExpect(jsonPath("lastMessage.text", is(participantAddedMessage)))
                    .andExpect(jsonPath("lastMessage.author", is(authorId.intValue())))
                    .andExpect(jsonPath("lastMessage.newFor", hasSize(participantIds.size() - 1)))
                    .andExpect(jsonPath("lastMessage.system", is(Boolean.TRUE)))
                    .andExpect(jsonPath("lastMessage.subject", is(removedParticipantId.intValue())))
                    .andExpect(jsonPath("type", is(CONFERENCE)));
    }

    @Test
    public void testUpdateParticipantsForNonExistentConversation() throws Exception {
        Long authorId = 1L;
        Long removedParticipantId = 4L;
        Long nonExistentConversationId = 444L;

        UpdateParticipantsDto updateParticipantsDto = new UpdateParticipantsDto();
        updateParticipantsDto.setAction(UpdateParticipantsDto.Action.REMOVE);
        updateParticipantsDto.setInitiator(authorId);
        updateParticipantsDto.setSubject(removedParticipantId);

        String addParticipantJson = mapper.writeValueAsString(updateParticipantsDto);

        mvc.perform(patch("/api/conversations/" + nonExistentConversationId + "/participants")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addParticipantJson)
                .header("Authorization",TOKEN))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(equalTo("Conversation with id '444' has not been found.")));
    }

    private String getCreateConversationDtoJson(
            Long authorId, Set<Long> receivers, String text, String name) throws Exception {
        CreateConversationDto createConversationDto = new CreateConversationDto();
        createConversationDto.setAuthor(authorId);
        createConversationDto.setReceivers(receivers);
        createConversationDto.setText(text);
        createConversationDto.setName(name);

        return mapper.writeValueAsString(createConversationDto);
    }

    private String getAddMessageDtoJson(Long authorId, String text, boolean isSystem) throws Exception {
        AddMessageDto addMessageDto = new AddMessageDto();
        addMessageDto.setAuthor(authorId);
        addMessageDto.setText(text);
        addMessageDto.setSystem(isSystem);
        return mapper.writeValueAsString(addMessageDto);
    }
}
