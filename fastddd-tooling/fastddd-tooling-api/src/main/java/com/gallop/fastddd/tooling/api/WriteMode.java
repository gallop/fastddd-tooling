package com.gallop.fastddd.tooling.api;

/**
 * 写端点生成模式（GEN-002）。
 *
 * <p>贫血模型默认只读（{@link #READ_ONLY}）——仅生成 GET 查询端点，不暴露未鉴权写端点、
 * 不以 PO 作外部写请求体。需要写能力时显式选择 {@link #DTO}：生成 DTO 写请求体
 * （{@code Create${Entity}Request}/{@code Update${Entity}Request}，带 {@code @NotNull} 校验）
 * 与写控制器方法（POST/PUT/DELETE），写请求体经 {@code BeanUtils} 映射为实体，避免直接吃 PO。</p>
 *
 * <p>GEN-002 验收第 ④ 条「写请求体不得为 PO」与第 ⑤ 条「校验扩展点」由 {@link #DTO} 模式落地：
 * 默认安全（只读），写为 opt-in。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
public enum WriteMode {

    /** 只读：仅生成继承 {@code BaseReadOnlyController} 的 GET 查询端点（默认安全姿态）。 */
    READ_ONLY("READ_ONLY", "只读查询端点"),

    /** DTO 写：生成 Create/Update Request 写请求体（带校验）+ POST/PUT/DELETE 写端点。 */
    DTO("DTO", "DTO 写请求体 + 写端点");

    private final String code;
    private final String description;

    WriteMode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 按代码解析；空/非法返回 {@link #READ_ONLY}（默认）。
     */
    public static WriteMode fromCode(String code) {
        if (code == null || code.isBlank()) {
            return READ_ONLY;
        }
        for (WriteMode mode : values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        return READ_ONLY;
    }
}
