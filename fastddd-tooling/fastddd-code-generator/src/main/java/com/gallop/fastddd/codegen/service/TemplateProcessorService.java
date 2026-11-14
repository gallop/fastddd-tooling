package com.gallop.fastddd.codegen.service;

import com.gallop.fastddd.codegen.config.TemplateEngineConfig;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * 模板处理服务
 *
 * @author gallop
 * @date 2023-10-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateProcessorService {

    private final TemplateEngineConfig templateEngineConfig;

    /**
     * 处理模板
     *
     * @param templateName 模板名称
     * @param variables    变量
     * @return 处理后的内容
     */
    public String processTemplate(String templateName, Map<String, Object> variables) {
        try {
            Template template = templateEngineConfig.getConfiguration().getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(variables, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            log.error("处理模板失败: {}", templateName, e);
            throw new RuntimeException("处理模板失败: " + templateName, e);
        }
    }
}