package ${packageName}.security;

import ${userEntityPackage}.${userEntityName};
import ${userMapperPackage}.${userMapperName};
import com.gallop.fastddd.security.spi.CodeLoginIdentityService;
import com.gallop.fastddd.security.spi.SecurityIdentityService;
import com.gallop.fastddd.security.spi.SecurityUserDetails;
import com.gallop.fastddd.security.spi.SecurityUserInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ${appLabel} ${msg.proj.security.mc.identity.serviceTitle}
 *
 * <p>${msg.proj.security.mc.identity.dependsPrefix} ${userEntityName} ${msg.proj.security.mc.identity.entityAnd} ${userMapperName}${msg.proj.security.mc.identity.sentenceEnd}</p>
 * <p>${msg.proj.security.mc.identity.userTableFields}</p>
 *
 * @author FastDDD
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppIdentityService implements SecurityIdentityService, CodeLoginIdentityService {

    private final ${userMapperName} ${userMapperFieldName};

    @Override
    public SecurityUserDetails loadUserByUsername(String username) {
        ${userEntityName} user = ${userMapperFieldName}.selectOneByField("username", username).orElse(null);
        if (user == null) {
            return null;
        }
        return toUserDetails(user);
    }

    @Override
    public SecurityUserInfo getUserInfo(String userId) {
        ${userEntityName} user = ${userMapperFieldName}.selectById(Long.valueOf(userId));
        if (user == null) {
            return null;
        }
        return SecurityUserInfo.builder()
                .id(String.valueOf(user.getId()))
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .enabled(isAccountEnabled(user))
                .userType(user.getUserType())
                .status(user.getStatus())
                .build();
    }

    /**
     * ${msg.proj.security.mc.identity.loadByPhoneDesc}
     */
    @Override
    public SecurityUserDetails loadUserByPhone(String phone) {
        ${userEntityName} user = ${userMapperFieldName}.selectOneByField("phone", phone).orElse(null);
        if (user == null) {
            return null;
        }
        return toUserDetails(user);
    }

    /**
     * ${msg.proj.security.mc.identity.loadByEmailDesc}
     */
    @Override
    public SecurityUserDetails loadUserByEmail(String email) {
        ${userEntityName} user = ${userMapperFieldName}.selectOneByField("email", email).orElse(null);
        if (user == null) {
            return null;
        }
        return toUserDetails(user);
    }

    // ==================== ${msg.proj.security.mc.identity.internalMethods} ====================

    private SecurityUserDetails toUserDetails(${userEntityName} user) {
        return SecurityUserDetails.builder()
                .userId(String.valueOf(user.getId()))
                .username(user.getUsername())
                .password(user.getPassword())
                .enabled(isAccountEnabled(user))
                .build();
    }

    private boolean isAccountEnabled(${userEntityName} user) {
        Integer status = user.getStatus();
        return status != null && status != 0;
    }
}
