package ${packageName}.service;

import ${packageName}.entity.SysUser;
import ${packageName}.entity.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Optional;

/**
 * ${msg.proj.security.sysuserservice.serviceDesc}
 *
 * @author ${author}
 * @since ${date}
 */
public interface SysUserService extends IService<SysUser> {

    Optional<SysUser> getByUsername(String username);

    List<SysRole> getRolesByUserId(Long userId);

    List<String> getPermissionsByUserId(Long userId);

    void updateLastLoginInfo(Long userId, String ip);
}
