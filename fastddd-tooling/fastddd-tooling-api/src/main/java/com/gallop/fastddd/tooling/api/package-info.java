/**
 * 稳定 Tooling 契约层（SPLIT-003）。
 *
 * <p>本包定义 Studio、Maven Plugin 与未来模板扩展消费的纯 Java 契约，使生成器内部实现
 * （{@code fastddd-code-generator}）可自由演进而不破坏下游编译：</p>
 *
 * <ul>
 *   <li>{@link com.gallop.fastddd.tooling.api.ProjectGenerator} / {@link com.gallop.fastddd.tooling.api.GenerationRequest}
 *       / {@link com.gallop.fastddd.tooling.api.GenerationResult} —— 代码生成主入口与结构化 I/O。</li>
 *   <li>{@link com.gallop.fastddd.tooling.api.DatabaseIntrospector} —— 数据库元数据内省契约。</li>
 *   <li>{@link com.gallop.fastddd.tooling.api.TemplateCatalog} —— 模板目录只读视图。</li>
 *   <li>{@link com.gallop.fastddd.tooling.api.GenerationProgressListener} —— 生成进度回调。</li>
 *   <li>{@link com.gallop.fastddd.tooling.api.ModelType} / {@link com.gallop.fastddd.tooling.api.IdStrategy}
 *       —— 跨产品共享枚举的规范归属（取代历史 3-4 处重复定义）。</li>
 * </ul>
 *
 * <p><b>二进制面约束</b>（构建期 enforcer 门禁强制）：本模块禁止依赖 FreeMarker、MyBatis-Plus、
 * Spring MVC、数据库驱动或连接池；仅依赖 {@code fast-ddd-common} 共享元数据类型。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
package com.gallop.fastddd.tooling.api;
