package club.tempvs.message;

import club.tempvs.message.dto.CreateConversationDto;
import club.tempvs.message.dto.GetConversationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static java.util.stream.Collectors.*;

@RestController
@RequestMapping("/api")
public class ConversationController {

    private final ConversationService conversationService;
    private final ParticipantService participantService;

    @Autowired
    public ConversationController(ConversationService conversationService, ParticipantService participantService) {
        this.conversationService = conversationService;
        this.participantService = participantService;
    }

    @RequestMapping(value="/conversation", method = POST,
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    GetConversationDto createConversation(@RequestBody CreateConversationDto createConversationDto) {
        Participant sender = participantService.getParticipant(createConversationDto.getSender());
        Set<Participant> receivers = createConversationDto.getReceivers().stream()
                .map(participantService::getParticipant).collect(toSet());
        String text = createConversationDto.getText();
        String name = createConversationDto.getName();
        Conversation conversation = conversationService.createConversation(sender, receivers, text, name);
        return new GetConversationDto(conversation);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String processValidationError(Exception ex) {
        return ex.getMessage();
    }
}
