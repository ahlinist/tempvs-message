package club.tempvs.message.controller;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.CreateConversationDto;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.service.ParticipantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
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
@Transactional(propagation=Propagation.REQUIRED)
public class ConversationControllerIntegrationTest {

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MockMvc mvc;

    private static ObjectMapper mapper = new ObjectMapper();

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
                .andExpect(jsonPath("messages[0].newFor", is(Arrays.asList(1, 2, 3, 4))));
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
                .andExpect(status().isInternalServerError());
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
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetConversation() throws Exception {
        Long senderId = 1L;
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(2L, 3L, 4L));
        String text = "text";
        String name = "name";
        List<Integer> participantIds = Arrays.asList(1, 2, 3, 4);

        Conversation conversation = createConversation(senderId, receiverIds, text, name);
        Long conversationId = conversation.getId();
        int messagesSize = conversation.getMessages().size();

        mvc.perform(get("/api/conversation/" + conversationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(conversationId.intValue())))
                .andExpect(jsonPath("admin", is(senderId.intValue())))
                .andExpect(jsonPath("participants", is(participantIds)))
                .andExpect(jsonPath("messages", hasSize(messagesSize)))
                .andExpect(jsonPath("messages[0].text", is(text)))
                .andExpect(jsonPath("messages[0].author", is(senderId.intValue())))
                .andExpect(jsonPath("messages[0].newFor", is(participantIds)));
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

    private Conversation createConversation(Long senderId, Set<Long> receiverIds, String text, String name) {
        Participant sender = participantService.createParticipant(senderId);
        Set<Participant> receivers = receiverIds.stream().map(participantService::createParticipant).collect(toSet());
        return conversationService.createConversation(sender, receivers, text, name);
    }
}
