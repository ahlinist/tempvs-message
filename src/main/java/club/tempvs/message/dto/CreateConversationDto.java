package club.tempvs.message.dto;

import lombok.Data;

import java.util.*;

@Data
public class CreateConversationDto {
    private Set<Long> receivers = new LinkedHashSet<>();
    private String text;
    private String name;
}
