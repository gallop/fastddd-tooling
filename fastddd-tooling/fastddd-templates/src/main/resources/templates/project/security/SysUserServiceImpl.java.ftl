package ${packageName}.service.impl;

import ${packageName}.entity.SysRole;
import ${packageName}.entity.SysUser;
import ${packageName}.mapper.SysUserMapper;
import ${packageName}.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ${msg.proj.security.sysuserserviceimpl.serviceImplDesc}
 *
 * @author ${author}
 * @since ${date}
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;

    @Override
    public Optional<SysUser> getByUsername(String username) {
        SysUser user = sysUserMapper.selectByUsername(username);
        return Optional.ofNullable(user);
    }

    @Override
    public List<SysRole> getRolesByUserId(Long userId) {
        return sysUserMapper.selectRolesByUserId(userId);
    }

    @Override
    public List<String> getPermissionsByUserId(Long userId) {
        return sysUserMapper.selectPermissionsByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLastLoginInfo(Long userId, String ip) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        this.updateById(user);
    }
}
