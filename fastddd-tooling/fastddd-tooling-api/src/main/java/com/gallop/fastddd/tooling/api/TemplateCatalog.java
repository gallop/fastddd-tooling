package com.gallop.fastddd.tooling.api;

import java.util.Set;

/**
 * 模板目录只读契约（SPLIT-003）。
 *
 * <p>供消费者查询可用模板（不耦合 FreeMarker {@code Configuration}）。最小契约：列举模板逻辑名、
 * 判定存在性、按分类列举。具体模板渲染（{@code TemplateRenderer}）仍属 Tooling 实现层，未来可经
 * 适配暴露为更丰富的 API，本期不预设计复杂扩展协议（SPLIT-003 禁止项）。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
public interface TemplateCatalog {

    /** 全部模板逻辑名（如 {@code "anemic/Entity.ftl"}）。 */
    Set<String> listTemplateNames();

    /** 模板是否存在。 */
    boolean hasTemplate(String templateName);
}
