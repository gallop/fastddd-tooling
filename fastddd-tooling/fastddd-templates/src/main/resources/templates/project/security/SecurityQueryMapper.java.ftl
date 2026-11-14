package ${packageName}.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SecurityQueryMapper {

    @Select("SELECT DISTINCT p.${permissionCodeColumn} FROM ${userTableName} u " +
            "INNER JOIN ${userRoleTableName} ur ON u.id = ur.${userRoleUserFkColumn} AND ur.deleted = 0 " +
            "INNER JOIN ${rolePermissionTableName} rp ON ur.${userRoleRoleFkColumn} = rp.${rolePermissionRoleFkColumn} AND rp.deleted = 0 " +
            "INNER JOIN ${permissionTableName} p ON rp.${rolePermissionPermFkColumn} = p.id AND p.deleted = 0 " +
            "WHERE u.id = ${r"#{userId}"} AND u.deleted = 0")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT r.${roleCodeColumn} FROM ${userTableName} u " +
            "INNER JOIN ${userRoleTableName} ur ON u.id = ur.${userRoleUserFkColumn} AND ur.deleted = 0 " +
            "INNER JOIN ${roleTableName} r ON ur.${userRoleRoleFkColumn} = r.id AND r.deleted = 0 " +
            "WHERE u.id = ${r"#{userId}"} AND u.deleted = 0")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
