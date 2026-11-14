package ${packageName}.mapper;

import ${packageName}.entity.SysRole;
import com.gallop.fastddd.crud.repository.BaseRepository;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * ${msg.proj.security.sysrolemapper.mapperDesc}
 *
 * @author ${author}
 * @since ${date}
 */
@Mapper
public interface SysRoleMapper extends BaseRepository<SysRole> {

    @Select("SELECT * FROM sys_role WHERE role_code = ${r"#{roleCode}"} AND deleted = 0")
    SysRole selectByRoleCode(@Param("roleCode") String roleCode);

    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = ${r"#{userId}"} AND r.deleted = 0 AND ur.deleted = 0")
    List<SysRole> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM sys_role WHERE status = ${r"#{status}"} AND deleted = 0")
    List<SysRole> selectByStatus(@Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM sys_role_permission WHERE role_id = ${r"#{roleId}"} AND deleted = 0")
    int countPermissionsByRoleId(@Param("roleId") Long roleId);

    default boolean existsByRoleCode(String roleCode) {
        return selectByRoleCode(roleCode) != null;
    }
}
