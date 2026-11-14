package ${packageName}.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ${msg.proj.refreshRequest.classDesc}
 *
 * @author ${author}
 * @date ${date}
 */
@Data
public class RefreshRequest {

    /**
     * ${msg.proj.refreshRequest.refreshTokenLabel}
     */
    @NotBlank(message = "${msg.proj.refreshRequest.refreshTokenLabel}${msg.common.validation.notBlank}")
    private String refreshToken;
}
