package club.tempvs.message.controller;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.dto.AddMessageDto;
import club.tempvs.message.util.EntityHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional(propagation= Propagation.REQUIRED)
public class MessageControllerIntegrationTest {

    private static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mvc;
    @Autowired
    private EntityHelper entityHelper;

    @Test
    public void testGetPong() throws Exception {
        mvc.perform(get("/api/ping").accept(TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("pong!")));
    }

    @Test
    public void testAddMessage() throws Exception {
        Long senderId = 1L;
        Set<Long> receiverIds = new HashSet<>(Arrays.asList(2L, 3L, 4L));
        String text = "text";
        String name = "name";
        String newMessageText = "new message text";
        Boolean isSystem = Boolean.FALSE;

        Conversation conversation = entityHelper.createConversation(senderId, receiverIds, text, name);
        AddMessageDto addMessageDto = new AddMessageDto();
        addMessageDto.setConversation(conversation.getId());
        addMessageDto.setSender(senderId);
        addMessageDto.setText(newMessageText);
        addMessageDto.setSystem(isSystem);
        String addMessageJson = mapper.writeValueAsString(addMessageDto);

        mvc.perform(post("/api/message")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(addMessageJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("success", is(true)));
    }
}
