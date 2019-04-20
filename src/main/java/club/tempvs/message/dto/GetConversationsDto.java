package club.tempvs.message.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GetConversationsDto {

    private List<ConversationDtoBean> conversations;
}
