package club.tempvs.message.controller;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.service.ConversationService;
import club.tempvs.message.service.ParticipantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
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
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.nio.file.Files;
import java.util.*;

import static java.util.stream.Collectors.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;

import static org.springframework.http.MediaType.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ConversationControllerIntegrationTest {

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MockMvc mvc;

    @Value("classpath:club/tempvs/message/createConversation.json")
    private Resource createConversationResource;

    @Value("classpath:club/tempvs/message/createConversationWithNoSender.json")
    private Resource createConversationWithNoSenderResource;

    @Value("classpath:club/tempvs/message/createConversationWithNoMessage.json")
    private Resource createConversationWithNoMessageResource;

    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void setupSpec() {
        mapper.registerModule(new JavaTimeModule());
    }

    @Before
    public void setup() {

    }

    @Test
    public void testCreateConversation() throws Exception {
        String createConversationJson = new String(Files.readAllBytes(createConversationResource.getFile().toPath()));

        mvc.perform(post("/api/conversation")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("participants", is(Arrays.asList(1,2,3,4))))
                .andExpect(jsonPath("messages", hasSize(1)))
                .andExpect(jsonPath("messages[0].text", is("myText")))
                .andExpect(jsonPath("messages[0].author", is(4)))
                .andExpect(jsonPath("messages[0].newFor", is(Arrays.asList(1,2,3,4))));
    }

    @Test
    public void testCreateConversationWithNoSender() throws Exception {
        String createConversationJson = new String(Files.readAllBytes(createConversationWithNoSenderResource.getFile().toPath()));

        mvc.perform(post("/api/conversation")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void testCreateConversationWithNoMessage() throws Exception {
        String createConversationJson = new String(Files.readAllBytes(createConversationWithNoMessageResource.getFile().toPath()));

        mvc.perform(post("/api/conversation")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(createConversationJson))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @Transactional
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
                .andExpect(jsonPath("participants", is(participantIds)))
                .andExpect(jsonPath("messages", hasSize(messagesSize)))
                .andExpect(jsonPath("messages[0].text", is(text)))
                .andExpect(jsonPath("messages[0].author", is(senderId.intValue())))
                .andExpect(jsonPath("messages[0].newFor", is(participantIds)));
    }

    private Conversation createConversation(Long senderId, Set<Long> receiverIds, String text, String name) {
        Participant sender = participantService.createParticipant(senderId);
        Set<Participant> receivers = receiverIds.stream().map(participantService::createParticipant).collect(toSet());
        return conversationService.createConversation(sender, receivers, text, name);
    }
}
