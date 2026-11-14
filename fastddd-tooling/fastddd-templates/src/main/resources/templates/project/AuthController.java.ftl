package ${packageName}.controller;

import ${packageName}.dto.auth.LoginRequest;
import ${packageName}.dto.auth.LoginResponse;
import ${packageName}.dto.auth.RefreshRequest;
import ${packageName}.dto.auth.TokenResponse;
import ${packageName}.dto.auth.UserInfoResponse;
import ${packageName}.dto.auth.UserInfo;
import ${packageName}.entity.SysUser;
import ${packageName}.service.SysUserService;
import ${packageName}.mapper.SysUserMapper;
import ${packageName}.mapper.SysRoleMapper;
import ${packageName}.mapper.SysPermissionMapper;

import com.gallop.fastddd.security.audit.AuditLog;
import com.gallop.fastddd.security.jwt.JwtTokenProvider;
import com.gallop.fastddd.web.response.ApiResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ${msg.proj.authController.classDesc}
 * ${msg.proj.authController.classDetail}
 *
 * @author ${author}
 * @date ${date}
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final SysUserService sysUserService;
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;

    /**
     * ${msg.proj.authController.loginTitle}
     *
     * @param request ${msg.proj.authController.loginParamRequest}
     * @param httpRequest ${msg.proj.authController.loginParamHttpRequest}
     * @return ${msg.proj.authController.loginReturn}
     */
    @PostMapping("/login")
    @AuditLog(value = "${msg.proj.authController.loginTitle}", type = AuditLog.OperationType.LOGIN)
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletRequest httpRequest) {
        log.info("${msg.proj.authController.logLoginRequest}", request.getUsername());

        try {
            // 1. ${msg.proj.authController.stepAuthenticate}
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 2. ${msg.proj.authController.stepGenerateToken}
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            // 3. ${msg.proj.authController.stepQueryUser}
            SysUser user = sysUserService.getByUsername(request.getUsername())
                    .orElse(null);

            if (user == null) {
                return ApiResponse.error(401, "${msg.proj.authController.userNotExists}");
            }

            // 4. ${msg.proj.authController.stepQueryPermissions}
            List<String> permissions = getUserPermissions(user.getId());

            // 5. ${msg.proj.authController.stepUpdateLoginInfo}
            String ip = getClientIp(httpRequest);
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(ip);
            sysUserService.updateById(user);

            log.info("${msg.proj.authController.logLoginSuccess}", request.getUsername(), ip);

            // 6. ${msg.proj.authController.stepBuildResponse}
            UserInfo userInfo = UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .realName(user.getRealName() != null ? user.getRealName() : user.getUsername())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .avatar(user.getAvatar())
                    .status(user.getStatus() != null ? user.getStatus() : true)
                    .build();

            LoginResponse response = LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .user(userInfo)
                    .permissions(permissions)
                    .build();

            return ApiResponse.success(response, "${msg.proj.authController.loginSuccess}");

        } catch (Exception e) {
            log.error("${msg.proj.authController.logLoginFailed}", request.getUsername(), e.getMessage(), e);
            return ApiResponse.error(401, "${msg.proj.authController.badCredentials}");
        }
    }

    /**
     * ${msg.proj.authController.logoutTitle}
     *
     * @return ${msg.proj.authController.logoutReturn}
     */
    @PostMapping("/logout")
    @AuditLog(value = "${msg.proj.authController.logoutTitle}", type = AuditLog.OperationType.LOGOUT)
    public ApiResponse<String> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "unknown";

        log.info("${msg.proj.authController.logLogout}", username);

        SecurityContextHolder.clearContext();
        return ApiResponse.success(null, "${msg.proj.authController.logoutSuccess}");
    }

    /**
     * ${msg.proj.authController.refreshTitle}
     *
     * @param request ${msg.proj.authController.refreshParamRequest}
     * @return ${msg.proj.authController.refreshReturn}
     */
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        log.debug("${msg.proj.authController.logRefreshRequest}");

        // ${msg.proj.authController.verifyRefreshToken}
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ApiResponse.error(401, "${msg.proj.authController.refreshInvalid}");
        }

        try {
            // ${msg.proj.authController.getUsernameFromToken}
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

            // ${msg.proj.authController.queryUserByService}
            SysUser user = sysUserService.getByUsername(username)
                    .orElse(null);

            if (user == null) {
                return ApiResponse.error(401, "${msg.proj.authController.userNotExists}");
            }

            List<String> permissions = getUserPermissions(user.getId());

            // ${msg.proj.authController.buildAuthentication}
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    permissions.stream()
                            .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                            .collect(Collectors.toList())
            );

            String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);

            log.info("${msg.proj.authController.logRefreshSuccess}", username);

            TokenResponse response = TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .build();

            return ApiResponse.success(response, "${msg.proj.authController.refreshSuccess}");

        } catch (Exception e) {
            log.error("${msg.proj.authController.logRefreshFailed}", e.getMessage());
            return ApiResponse.error(401, "${msg.proj.authController.refreshFailed}");
        }
    }

    /**
     * ${msg.proj.authController.userinfoTitle}
     *
     * @return ${msg.proj.authController.userinfoReturn}
     */
    @GetMapping("/userinfo")
    public ApiResponse<UserInfoResponse> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.debug("${msg.proj.authController.logGetUserInfo}", username);

        SysUser user = sysUserService.getByUsername(username)
                .orElse(null);

        if (user == null) {
            return ApiResponse.error(404, "${msg.proj.authController.userNotExists}");
        }

        List<String> permissions = getUserPermissions(user.getId());

        UserInfo userInfo = UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName() != null ? user.getRealName() : user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .status(user.getStatus() != null ? user.getStatus() : true)
                .build();

        UserInfoResponse response = UserInfoResponse.builder()
                .user(userInfo)
                .permissions(permissions)
                .roles(sysRoleMapper.selectByUserId(user.getId()).stream()
                        .map(r -> r.getRoleCode())
                        .collect(Collectors.toList()))
                .build();

        return ApiResponse.success(response);
    }

    /**
     * ${msg.proj.authController.permissionsTitle}
     * ${msg.proj.authController.permissionsDetail}
     */
    private List<String> getUserPermissions(Long userId) {
        return sysPermissionMapper.selectByUserId(userId).stream()
                .map(p -> p.getPermissionCode())
                .collect(Collectors.toList());
    }

    /**
     * ${msg.proj.authController.clientIpTitle}
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // ${msg.proj.authController.multiIpNote}
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
