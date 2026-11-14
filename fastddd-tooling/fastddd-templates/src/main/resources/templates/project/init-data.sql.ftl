-- ============================================
-- FastDDD ${msg.proj.initDataSql.scriptTitle}
-- ============================================
-- ${msg.proj.initDataSql.genTime}: ${date}
-- ${msg.proj.initDataSql.projectName}: ${artifactId}
-- ${msg.proj.initDataSql.dbType}: ${databaseDisplayName}
-- ============================================
<#assign tableSuffix = "">
<#assign pkType = "BIGINT PRIMARY KEY AUTO_INCREMENT">
<#assign tinyintType = "TINYINT">
<#assign booleanType = "TINYINT(1)">
<#assign deletedType = "TINYINT NOT NULL DEFAULT 0">
<#assign nowFunction = "NOW()">
<#assign clobType = "TEXT">
<#if databaseType == "MYSQL">
  <#assign tableSuffix = " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci">
<#elseif databaseType == "POSTGRESQL">
  <#assign pkType = "BIGSERIAL PRIMARY KEY">
  <#assign tinyintType = "SMALLINT">
  <#assign booleanType = "SMALLINT">
  <#assign deletedType = "SMALLINT NOT NULL DEFAULT 0">
<#elseif databaseType == "ORACLE">
  <#assign pkType = "NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY">
  <#assign tinyintType = "NUMBER(3)">
  <#assign booleanType = "NUMBER(1)">
  <#assign deletedType = "NUMBER(1) DEFAULT 0 NOT NULL">
  <#assign nowFunction = "SYSTIMESTAMP">
  <#assign clobType = "CLOB">
<#elseif databaseType == "SQLSERVER">
  <#assign pkType = "BIGINT IDENTITY(1,1) PRIMARY KEY">
  <#assign tinyintType = "SMALLINT">
  <#assign booleanType = "BIT">
  <#assign deletedType = "BIT NOT NULL DEFAULT 0">
  <#assign nowFunction = "GETDATE()">
  <#assign clobType = "NVARCHAR(MAX)">
</#if>

-- ============================================
-- DDL: ${msg.proj.initDataSql.ddlTitle}
-- ============================================

-- ${msg.proj.initDataSql.userTable}
CREATE TABLE IF NOT EXISTS sys_user (
    id ${pkType},
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    real_name VARCHAR(50),
    avatar VARCHAR(255),
    status ${tinyintType} NOT NULL DEFAULT 1,
    dept_id BIGINT,
    user_type ${tinyintType} NOT NULL DEFAULT 2,
    last_login_time TIMESTAMP NULL,
    last_login_ip VARCHAR(50),
    remark VARCHAR(500),
    deleted ${deletedType},
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP<#if databaseType == "MYSQL"> ON UPDATE CURRENT_TIMESTAMP</#if>,
    create_user VARCHAR(50),
    update_user VARCHAR(50)
)${tableSuffix};

-- ${msg.proj.initDataSql.roleTable}
CREATE TABLE IF NOT EXISTS sys_role (
    id ${pkType},
    role_name VARCHAR(50) NOT NULL UNIQUE,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    status ${tinyintType} NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    deleted ${deletedType},
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP<#if databaseType == "MYSQL"> ON UPDATE CURRENT_TIMESTAMP</#if>,
    create_user VARCHAR(50),
    update_user VARCHAR(50)
)${tableSuffix};

-- ${msg.proj.initDataSql.permissionTable}
CREATE TABLE IF NOT EXISTS sys_permission (
    id ${pkType},
    permission_name VARCHAR(50) NOT NULL,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    resource_type ${tinyintType} NOT NULL,
    path VARCHAR(200),
    method VARCHAR(10),
    parent_id BIGINT NOT NULL DEFAULT 0,
    status ${tinyintType} NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    deleted ${deletedType},
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP<#if databaseType == "MYSQL"> ON UPDATE CURRENT_TIMESTAMP</#if>,
    create_user VARCHAR(50),
    update_user VARCHAR(50)
)${tableSuffix};

-- ${msg.proj.initDataSql.userRoleTable}
CREATE TABLE IF NOT EXISTS sys_user_role (
    id ${pkType},
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    deleted ${deletedType},
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP<#if databaseType == "MYSQL"> ON UPDATE CURRENT_TIMESTAMP</#if>,
    create_user VARCHAR(50),
    update_user VARCHAR(50),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
)${tableSuffix};

-- ${msg.proj.initDataSql.rolePermissionTable}
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id ${pkType},
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    deleted ${deletedType},
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP<#if databaseType == "MYSQL"> ON UPDATE CURRENT_TIMESTAMP</#if>,
    create_user VARCHAR(50),
    update_user VARCHAR(50),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
)${tableSuffix};

-- ${msg.proj.initDataSql.auditLogTable}
CREATE TABLE IF NOT EXISTS sys_audit_log (
    id ${pkType},
    username VARCHAR(50),
    operation VARCHAR(200),
    operation_type VARCHAR(50),
    method VARCHAR(10),
    request_url VARCHAR(500),
    request_params ${clobType},
    result ${clobType},
    client_ip VARCHAR(50),
    user_agent VARCHAR(500),
    execution_time BIGINT,
    success ${booleanType} DEFAULT 1,
    error_message ${clobType},
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_create_time (create_time),
    INDEX idx_operation_type (operation_type)
)${tableSuffix};

-- ${msg.proj.initDataSql.menuTable}
CREATE TABLE IF NOT EXISTS sys_menu (
    id ${pkType},
    menu_code VARCHAR(50) NOT NULL UNIQUE,
    menu_name VARCHAR(50) NOT NULL,
    menu_type VARCHAR(20) NOT NULL,
    parent_id BIGINT NOT NULL DEFAULT 0,
    menu_path VARCHAR(200),
    component_path VARCHAR(200),
    icon VARCHAR(100),
    sort_order INT NOT NULL DEFAULT 0,
    status ${tinyintType} NOT NULL DEFAULT 1,
    deleted ${deletedType},
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP<#if databaseType == "MYSQL"> ON UPDATE CURRENT_TIMESTAMP</#if>,
    create_user VARCHAR(50),
    update_user VARCHAR(50)
)${tableSuffix};

-- ============================================
-- ${msg.proj.initDataSql.initDataSection}
-- ============================================

-- ${msg.proj.initDataSql.adminNoteTitle}
-- ${msg.proj.initDataSql.adminNote1}
-- ${msg.proj.initDataSql.adminNote2}
-- INSERT INTO sys_user (username, password, real_name, email, phone, status, create_time, update_time)
-- VALUES ('admin', '<${msg.proj.initDataSql.yourBcryptHash}>', '${msg.proj.initDataSql.sysAdminName}', 'admin@example.com', '13800138000', 1, ${nowFunction}, ${nowFunction});
-- ${msg.proj.initDataSql.adminNote3}
-- ${msg.proj.initDataSql.adminNote4}

-- ${msg.proj.initDataSql.initRole}
INSERT INTO sys_role (role_code, role_name, description, sort_order, status, create_time, update_time)
VALUES ('ADMIN', '${msg.proj.initDataSql.adminRoleName}', '${msg.proj.initDataSql.adminRoleDesc}', 1, 1, ${nowFunction}, ${nowFunction})
<#if databaseType == "MYSQL">ON DUPLICATE KEY UPDATE role_code = role_code</#if>;

-- ${msg.proj.initDataSql.userRoleNote1}
-- ${msg.proj.initDataSql.userRoleNote2}
-- INSERT INTO sys_user_role (user_id, role_id, create_time, update_time)
-- SELECT u.id, r.id, ${nowFunction}, ${nowFunction}
-- FROM sys_user u, sys_role r
-- WHERE u.username = '<${msg.proj.initDataSql.yourAdminUsername}>' AND r.role_code = 'ADMIN'
-- AND NOT EXISTS (
--     SELECT 1 FROM sys_user_role sur
--     WHERE sur.user_id = u.id AND sur.role_id = r.id
-- );

-- ${msg.proj.initDataSql.initPermissions}
INSERT INTO sys_permission (permission_code, permission_name, remark, resource_type, path, method, parent_id, sort_order, status, create_time, update_time)
VALUES
('system:user:view', '${msg.proj.initDataSql.permViewUser}', '${msg.proj.initDataSql.permViewUserDesc}', 1, '/sys-user', 'GET', 0, 1, 1, ${nowFunction}, ${nowFunction}),
('system:user:create', '${msg.proj.initDataSql.permCreateUser}', '${msg.proj.initDataSql.permCreateUserDesc}', 2, '/sys-user', 'POST', 0, 2, 1, ${nowFunction}, ${nowFunction}),
('system:user:update', '${msg.proj.initDataSql.permUpdateUser}', '${msg.proj.initDataSql.permUpdateUserDesc}', 2, '/sys-user', 'PUT', 0, 3, 1, ${nowFunction}, ${nowFunction}),
('system:user:delete', '${msg.proj.initDataSql.permDeleteUser}', '${msg.proj.initDataSql.permDeleteUser}', 2, '/sys-user', 'DELETE', 0, 4, 1, ${nowFunction}, ${nowFunction}),
('system:role:view', '${msg.proj.initDataSql.permViewRole}', '${msg.proj.initDataSql.permViewRoleDesc}', 1, '/sys-role', 'GET', 0, 5, 1, ${nowFunction}, ${nowFunction}),
('system:role:assign', '${msg.proj.initDataSql.permAssignRole}', '${msg.proj.initDataSql.permAssignRoleDesc}', 2, '/sys-role', 'POST', 0, 6, 1, ${nowFunction}, ${nowFunction}),
('system:permission:view', '${msg.proj.initDataSql.permViewPermission}', '${msg.proj.initDataSql.permViewPermissionDesc}', 1, '/sys-permission', 'GET', 0, 7, 1, ${nowFunction}, ${nowFunction}),
('system:menu:view', '${msg.proj.initDataSql.permViewMenu}', '${msg.proj.initDataSql.permViewMenuDesc}', 1, '/sys-menu', 'GET', 0, 8, 1, ${nowFunction}, ${nowFunction})
<#if databaseType == "MYSQL">ON DUPLICATE KEY UPDATE permission_code = permission_code</#if>;

-- ${msg.proj.initDataSql.initRolePerm}
INSERT INTO sys_role_permission (role_id, permission_id, create_time, update_time)
SELECT r.id, p.id, ${nowFunction}, ${nowFunction}
FROM sys_role r, sys_permission p
WHERE r.role_code = 'ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission srp
    WHERE srp.role_id = r.id AND srp.permission_id = p.id
);

-- ${msg.proj.initDataSql.initMenu}
INSERT INTO sys_menu (menu_code, menu_name, menu_type, parent_id, menu_path, component_path, icon, sort_order, status, create_time, update_time)
VALUES
('system', '${msg.proj.initDataSql.menuSystem}', 'directory', 0, '/system', NULL, 'Setting', 1, 1, ${nowFunction}, ${nowFunction})
<#if databaseType == "MYSQL">ON DUPLICATE KEY UPDATE menu_code = menu_code</#if>;

<#-- Create the parent first; INSERT ... SELECT avoids MySQL's target-table subquery restriction. -->
<#list [
    {"code":"user", "name":msg.proj.initDataSql.menuUser, "icon":"User"},
    {"code":"role", "name":msg.proj.initDataSql.menuRole, "icon":"UserFilled"},
    {"code":"permission", "name":msg.proj.initDataSql.menuPermission, "icon":"Lock"},
    {"code":"menu", "name":msg.proj.initDataSql.menuMenu, "icon":"Menu"}
] as menu>
INSERT INTO sys_menu (menu_code, menu_name, menu_type, parent_id, menu_path, component_path, icon, sort_order, status, create_time, update_time)
SELECT 'system:${menu.code}', '${menu.name}', 'menu', parent.id, '/system/${menu.code}', 'sys-${menu.code}/index', '${menu.icon}', ${menu?counter}, 1, ${nowFunction}, ${nowFunction}
FROM sys_menu parent WHERE parent.menu_code = 'system'
<#if databaseType == "MYSQL">ON DUPLICATE KEY UPDATE menu_code = sys_menu.menu_code</#if>;
</#list>

-- ============================================
-- ${msg.proj.initDataSql.usageSection}
-- ============================================
-- ${msg.proj.initDataSql.deployNote}
--
-- 1. ${msg.proj.initDataSql.usage1}
--    new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("${msg.proj.initDataSql.yourStrongPassword}")
--
-- 2. ${msg.proj.initDataSql.usage2}
--
-- 3. ${msg.proj.initDataSql.usage3}
-- ============================================
