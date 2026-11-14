package com.gallop.fastddd.fastdoc.web;

import com.gallop.fastddd.fastdoc.FastDocExportService;
import com.gallop.fastddd.fastdoc.FastDocService;
import com.gallop.fastddd.fastdoc.FastDocTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * FastDoc HTTP 适配自动装配（SPLIT-002 自 fast-ddd-web 上移）。
 *
 * <p>当 classpath 存在 {@link FastDocService} 且容器中存在 {@link DataSource} 时，装配
 * FastDoc 三件套服务（FastDocService / FastDocTemplateService / FastDocExportService）与
 * {@link FastDocController}，暴露 {@code /api/fastdoc/*} 端点。
 *
 * <p>归属 Tooling 层（ADR-005）。依赖方向：Tooling → Framework（经 fast-ddd-common 传递），
 * 消除了原 fast-ddd-web → fastddd-fastdoc-core 的 Framework → Tooling 反向依赖。
 *
 * <p>{@code @AutoConfigureAfter(DataSourceAutoConfiguration.class)} 是必需的：fastDocService
 * 的 {@code @ConditionalOnBean(DataSource.class)} 引用另一自动配置装配的 bean，必须保证
 * DataSource 已注册后再求值，否则条件被跳过（原 web 嵌套配置从未在真实消费者中激活，此排序
 * 缺陷为潜在存量，本次一并修正）。
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
@Slf4j
@AutoConfiguration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@ConditionalOnClass(FastDocService.class)
@EnableConfigurationProperties(FastDocExportProperties.class)
public class FastDocWebAutoConfiguration {

    public FastDocWebAutoConfiguration() {
        log.info("=== FastDocWebAutoConfiguration initialized (Tooling layer) ===");
    }

    @Bean
    @ConditionalOnBean(DataSource.class)
    public FastDocService fastDocService(DataSource dataSource) {
        return new FastDocService(dataSource);
    }

    @Bean
    @ConditionalOnBean(FastDocService.class)
    public FastDocTemplateService fastDocTemplateService() {
        return new FastDocTemplateService();
    }

    @Bean
    @ConditionalOnBean(FastDocService.class)
    public FastDocExportService fastDocExportService(FastDocService fastDocService,
                                                     FastDocTemplateService fastDocTemplateService) {
        return new FastDocExportService(fastDocService, fastDocTemplateService);
    }

    /**
     * HTTP 导出可信根防护（ECO-RELEASE-04/S3）：/api/fastdoc 三个导出端点的 exportDir/tableName
     * 在任何创建/写入前经此校验，写入被钉死在 fastddd.fastdoc.export.root-dir 之内。
     */
    @Bean
    public FastDocExportPathGuard fastDocExportPathGuard(FastDocExportProperties properties) {
        return new FastDocExportPathGuard(properties.getRootDir());
    }

    @Bean
    @ConditionalOnBean(FastDocService.class)
    public FastDocController fastDocController(FastDocService fastDocService,
                                               FastDocTemplateService templateService,
                                               FastDocExportService exportService,
                                               FastDocExportPathGuard exportPathGuard) {
        return new FastDocController(fastDocService, templateService, exportService, exportPathGuard);
    }
}
