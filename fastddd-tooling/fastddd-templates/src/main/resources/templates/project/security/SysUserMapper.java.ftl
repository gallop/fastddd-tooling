package ${packageName}.mapper;

import ${packageName}.entity.SysUser;
import ${packageName}.entity.SysRole;
import com.gallop.fastddd.crud.repository.BaseRepository;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * ${msg.proj.security.sysusermapper.mapperDesc}
 *
 * @author ${author}
 * @since ${date}
 */
@Mapper
public interface SysUserMapper extends BaseRepository<SysUser> {

    @Select("SELECT * FROM sys_user WHERE username = ${r"#{username}"} AND deleted = 0")
    SysUser selectByUsername(@Param("username") String username);

    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = ${r"#{userId}"} AND r.deleted = 0 AND ur.deleted = 0")
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT p.permission_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = ${r"#{userId}"} AND p.deleted = 0 AND rp.deleted = 0 AND ur.deleted = 0")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM sys_user WHERE email = ${r"#{email}"} AND deleted = 0")
    SysUser selectByEmail(@Param("email") String email);

    @Select("SELECT * FROM sys_user WHERE phone = ${r"#{phone}"} AND deleted = 0")
    SysUser selectByPhone(@Param("phone") String phone);

    @Select("SELECT * FROM sys_user WHERE status = ${r"#{status}"} AND deleted = 0")
    List<SysUser> selectByStatus(@Param("status") Integer status);

    default boolean existsByUsername(String username) {
        return selectByUsername(username) != null;
    }

    default boolean existsByEmail(String email) {
        return selectByEmail(email) != null;
    }
}
