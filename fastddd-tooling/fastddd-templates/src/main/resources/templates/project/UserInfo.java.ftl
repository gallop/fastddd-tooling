package ${packageName}.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ${msg.proj.userInfo.classDesc}
 *
 * @author ${author}
 * @date ${date}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    /**
     * ${msg.proj.userInfo.idLabel}
     */
    private Long id;

    /**
     * ${msg.proj.userInfo.usernameLabel}
     */
    private String username;

    /**
     * ${msg.proj.userInfo.realNameLabel}
     */
    private String realName;

    /**
     * ${msg.proj.userInfo.emailLabel}
     */
    private String email;

    /**
     * ${msg.proj.userInfo.phoneLabel}
     */
    private String phone;

    /**
     * ${msg.proj.userInfo.avatarLabel}
     */
    private String avatar;

    /**
     * ${msg.proj.userInfo.statusLabel}
     */
    private Boolean status;
}
