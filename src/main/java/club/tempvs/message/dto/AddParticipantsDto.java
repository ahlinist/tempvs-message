package club.tempvs.message.dto;

import lombok.Data;

import java.util.Set;

@Data
public class AddParticipantsDto {
    private Set<Long> participants;
}
