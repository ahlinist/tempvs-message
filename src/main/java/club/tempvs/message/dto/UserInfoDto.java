package club.tempvs.message.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserInfoDto {
    private Long userId;
    private Long profileId;
    private List<String> roles;
}
