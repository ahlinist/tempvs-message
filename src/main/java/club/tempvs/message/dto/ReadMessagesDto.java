package club.tempvs.message.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReadMessagesDto {
    private List<Long> messages;
}
