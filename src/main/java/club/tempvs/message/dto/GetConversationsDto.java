package club.tempvs.message.dto;

import club.tempvs.message.domain.Conversation;

import java.util.ArrayList;
import java.util.List;
import static java.util.stream.Collectors.*;

public class GetConversationsDto {

    private List<ConversationDto> conversations;

    public GetConversationsDto() {

    }

    public GetConversationsDto(ArrayList<Conversation> conversations) {
        this.conversations = conversations.stream().map(ConversationDto::new).collect(toList());
    }

    public List<ConversationDto> getConversations() {
        return conversations;
    }

    public void setConversations(List<ConversationDto> conversations) {
        this.conversations = conversations;
    }
}
