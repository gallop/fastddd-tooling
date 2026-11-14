package ${packageName}.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ${msg.proj.loginRequest.classDesc}
 *
 * @author ${author}
 * @date ${date}
 */
@Data
public class LoginRequest {

    /**
     * ${msg.proj.loginRequest.usernameLabel}
     */
    @NotBlank(message = "${msg.proj.loginRequest.usernameLabel}${msg.common.validation.notBlank}")
    private String username;

    /**
     * ${msg.proj.loginRequest.passwordLabel}
     */
    @NotBlank(message = "${msg.proj.loginRequest.passwordLabel}${msg.common.validation.notBlank}")
    private String password;

    /**
     * ${msg.proj.loginRequest.captchaLabel}
     */
    private String captcha;

    /**
     * ${msg.proj.loginRequest.rememberMeLabel}
     */
    private Boolean rememberMe = false;
}
