package club.tempvs.message.controller;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.dto.CreateConversationDto;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;

import static org.springframework.http.MediaType.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional(propagation=Propagation.REQUIRED)
public class ConversationControllerIntegrationTest {

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
        Long senderId = 4L;
        Set<Long> receivers = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        String message = "myMessage";
        String name = "conversation name";
        String createConversationJson = getCreateConversationDtoJson(senderId, receivers, message, name);

        mvc.perform(post("/api/conversation")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("participants", is(Arrays.asList(1, 2, 3, 4))))
                .andExpect(jsonPath("admin", is(senderId.intValue())))
                .andExpect(jsonPath("messages", hasSize(1)))
                .andExpect(jsonPath("messages[0].text", is(message)))
                .andExpect(jsonPath("messages[0].author", is(senderId.intValue())))
                .andExpect(jsonPath("messages[0].subject", isEmptyOrNullString()))
                .andExpect(jsonPath("messages[0].newFor", is(Arrays.asList(1, 2, 3, 4))))
                .andExpect(jsonPath("messages[0].system", is(false)))
                .andExpect(jsonPath("lastMessage.text", is(message)))
                .andExpect(jsonPath("lastMessage.author", is(senderId.intValue())))
                .andExpect(jsonPath("lastMessage.newFor", is(Arrays.asList(1, 2, 3, 4))))
                .andExpect(jsonPath("lastMessage.system", is(false)));
    }

    @Test
    public void testCreateConversationWithNoSender() throws Exception {
        Set<Long> receivers = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        String message = "myMessage";
        String name = "conversation name";
        String createConversationJson = getCreateConversationDtoJson(null, receivers, message, name);

        mvc.perform(post("/api/conversation")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Sender id is missing.")));
    }

    @Test
    public void testCreateConversationWithNoMessage() throws Exception {
        Long senderId = 4L;
        Set<Long> receivers = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        String name = "conversation name";
        String createConversationJson = getCreateConversationDtoJson(senderId, receivers, null, name);

        mvc.perform(post("/api/conversation")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Text is missing.")));
    }

    @Test
    public void testCreateConversationWithNoReceivers() throws Exception {
        Long senderId = 4L;
        Set<Long> receivers = new HashSet<>();
        String message = "myMessage";
        String name = "conversation name";
        String createConversationJson = getCreateConversationDtoJson(senderId, receivers, message, name);

        mvc.perform(post("/api/conversation")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Receivers list is empty.")));
    }

    @Test
    public void testGetConversation() throws Exception {
        Long senderId = 1L;
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(2L, 3L, 4L));
        String text = "text";
        String name = "name";
        List<Integer> participantIds = Arrays.asList(1, 2, 3, 4);

        Conversation conversation = entityHelper.createConversation(senderId, receiverIds, text, name);
        Long conversationId = conversation.getId();
        List<Message> messages = conversation.getMessages();
        int messagesSize = messages.size();
        Long messageId = messages.get(0).getId();
        Boolean isSystem = messages.get(0).getSystem();

        mvc.perform(get("/api/conversation/" + conversationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(conversationId.intValue())))
                .andExpect(jsonPath("admin", is(senderId.intValue())))
                .andExpect(jsonPath("participants", is(participantIds)))
                .andExpect(jsonPath("messages", hasSize(messagesSize)))
                .andExpect(jsonPath("messages[0].id", is(messageId.intValue())))
                .andExpect(jsonPath("messages[0].text", is(text)))
                .andExpect(jsonPath("messages[0].author", is(senderId.intValue())))
                .andExpect(jsonPath("messages[0].subject", isEmptyOrNullString()))
                .andExpect(jsonPath("messages[0].newFor", is(participantIds)))
                .andExpect(jsonPath("messages[0].system", is(isSystem)))
                .andExpect(jsonPath("lastMessage.id", is(messageId.intValue())))
                .andExpect(jsonPath("lastMessage.text", is(text)))
                .andExpect(jsonPath("lastMessage.author", is(senderId.intValue())))
                .andExpect(jsonPath("lastMessage.newFor", is(participantIds)))
                .andExpect(jsonPath("lastMessage.system", is(isSystem)));
    }

    @Test
    public void testGetConversationForInvalidInput() throws Exception {
        Long senderId = 1L;
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(2L, 3L, 4L));
        String text = "text";
        String name = "name";

        Conversation conversation = entityHelper.createConversation(senderId, receiverIds, text, name);
        Long conversationId = conversation.getId();

        mvc.perform(get("/api/conversation/" + conversationId + "?page=0&size=-1"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/conversation/" + conversationId + "?page=0&size=0"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/conversation/" + conversationId + "?page=-1&size=20"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/conversation/" + conversationId + "?page=0&size=30"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetConversationsByParticipant() throws Exception {
        Long senderId = 1L;
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(2L, 3L, 4L));
        String text = "text";
        String name = "name";
        List<Integer> participantIds = Arrays.asList(1, 2, 3, 4);

        Conversation conversation = entityHelper.createConversation(senderId, receiverIds, text, name);
        Long conversationId = conversation.getId();
        List<Message> messages = conversation.getMessages();
        int messagesSize = messages.size();
        Long messageId = messages.get(0).getId();
        Boolean isSystem = messages.get(0).getSystem();

        mvc.perform(get("/api/conversation?participant=1&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("conversations", hasSize(messagesSize)))
                .andExpect(jsonPath("conversations[0].id", is(conversationId.intValue())))
                .andExpect(jsonPath("conversations[0].name", is(name)))
                .andExpect(jsonPath("conversations[0].lastMessage.id", is(messageId.intValue())))
                .andExpect(jsonPath("conversations[0].lastMessage.text", is(text)))
                .andExpect(jsonPath("conversations[0].lastMessage.author", is(senderId.intValue())))
                .andExpect(jsonPath("conversations[0].lastMessage.subject", isEmptyOrNullString()))
                .andExpect(jsonPath("conversations[0].lastMessage.newFor", is(participantIds)))
                .andExpect(jsonPath("conversations[0].lastMessage.system", is(isSystem)));
    }

    @Test
    public void testGetConversationsByParticipantForInvalidInput() throws Exception {
        mvc.perform(get("/api/conversation?participant=1&page=0&size=-1"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/conversation?participant=1&page=0&size=0"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/conversation?participant=1&page=-1&size=20"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/conversation?participant=1&page=0&size=30"))
                .andExpect(status().isBadRequest());
    }

    private String getCreateConversationDtoJson(
            Long senderId, Set<Long> receivers, String text, String name) throws Exception {
        CreateConversationDto createConversationDto = new CreateConversationDto();
        createConversationDto.setSender(senderId);
        createConversationDto.setReceivers(receivers);
        createConversationDto.setText(text);
        createConversationDto.setName(name);

        return mapper.writeValueAsString(createConversationDto);
    }
}
