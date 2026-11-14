package ${packageName}.security;

import ${packageName}.entity.SysUser;
import ${packageName}.mapper.SysUserMapper;
import com.gallop.fastddd.security.spi.SecurityIdentityService;
import com.gallop.fastddd.security.spi.SecurityUserDetails;
import com.gallop.fastddd.security.spi.SecurityUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RbacIdentityServiceImpl implements SecurityIdentityService {

    private final SysUserMapper sysUserMapper;

    @Override
    public SecurityUserDetails loadUserByUsername(String username) {
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user == null) {
            return null;
        }
        return SecurityUserDetails.builder()
                .userId(String.valueOf(user.getId()))
                .username(user.getUsername())
                .password(user.getPassword())
                .enabled(user.getStatus() != null && user.getStatus() != 0)
                .build();
    }

    @Override
    public SecurityUserInfo getUserInfo(String userId) {
        SysUser user = sysUserMapper.selectById(Long.valueOf(userId));
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
                .enabled(user.getStatus() != null && user.getStatus() != 0)
                .userType(user.getUserType())
                .status(user.getStatus())
                .build();
    }
}
