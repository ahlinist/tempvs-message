package club.tempvs.message;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.util.Arrays;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;

import static org.springframework.http.MediaType.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ConversationControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Value("classpath:club/tempvs/message/createConversation.json")
    Resource createConversationResource;

    @Value("classpath:club/tempvs/message/createConversationWithNoSender.json")
    Resource createConversationWithNoSenderResource;

    @Value("classpath:club/tempvs/message/createConversationWithNoMessage.json")
    Resource createConversationWithNoMessageResource;

    @Test
    public void testCreateConversation() throws Exception {
        String createConversationJson = new String(Files.readAllBytes(createConversationResource.getFile().toPath()));

        mvc.perform(
                MockMvcRequestBuilders.post("/api/conversation")
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

        mvc.perform(
                MockMvcRequestBuilders.post("/api/conversation")
                        .accept(APPLICATION_JSON_VALUE)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(createConversationJson))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void testCreateConversationWithNoMessage() throws Exception {
        String createConversationJson = new String(Files.readAllBytes(createConversationWithNoMessageResource.getFile().toPath()));

        mvc.perform(
                MockMvcRequestBuilders.post("/api/conversation")
                        .accept(APPLICATION_JSON_VALUE)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(createConversationJson))
                .andExpect(status().is5xxServerError());
    }
}
