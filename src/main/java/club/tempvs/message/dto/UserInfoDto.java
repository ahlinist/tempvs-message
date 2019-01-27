package club.tempvs.message.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserInfoDto {
    private Long userId;
    private Long profileId;
    private Long userProfileId;
    private String userName;
    private String timezone;
    private String lang;
    private List<String> roles;
}
