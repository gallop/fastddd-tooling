package com.gallop.fastddd.doc.parser;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Javadoc解析器
 * 解析Javadoc注释并提取描述信息
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
@Slf4j
@Component
public class JavadocParser {

    /**
     * 解析类的描述信息
     */
    public String parseClassDescription(TypeDeclaration<?> typeDeclaration) {
        return typeDeclaration.getJavadocComment()
                .map(JavadocComment::parse)
                .map(javadoc -> {
                    // 尝试获取描述
                    String description = javadoc.getDescription().toText();
                    return cleanDescription(description);
                })
                .orElse("");
    }

    /**
     * 解析方法摘要（第一行描述）
     */
    public String parseMethodSummary(MethodDeclaration methodDeclaration) {
        return methodDeclaration.getJavadocComment()
                .map(JavadocComment::parse)
                .map(javadoc -> {
                    // 获取第一行作为摘要
                    String description = javadoc.getDescription().toText();
                    String[] lines = description.split("\n");
                    if (lines.length > 0) {
                        return cleanDescription(lines[0]);
                    }
                    return "";
                })
                .orElse("");
    }

    /**
     * 解析方法的完整描述
     */
    public String parseMethodDescription(MethodDeclaration methodDeclaration) {
        return methodDeclaration.getJavadocComment()
                .map(JavadocComment::parse)
                .map(javadoc -> {
                    String description = javadoc.getDescription().toText();
                    return cleanDescription(description);
                })
                .orElse("");
    }

    /**
     * 解析参数的描述信息
     */
    public String parseParameterDescription(MethodDeclaration methodDeclaration, String parameterName) {
        return methodDeclaration.getJavadocComment()
                .map(JavadocComment::parse)
                .map(javadoc -> {
                    // 查找@param标签
                    return javadoc.getBlockTags().stream()
                            .filter(tag -> tag.getTagName().equals("@param"))
                            .filter(tag -> {
                                String content = tag.getContent().toText();
                                return content.startsWith(parameterName + " ") ||
                                       content.startsWith(parameterName + "\t");
                            })
                            .map(tag -> {
                                String content = tag.getContent().toText();
                                // 移除参数名，只保留描述
                                if (content.startsWith(parameterName)) {
                                    content = content.substring(parameterName.length());
                                }
                                return cleanDescription(content.trim());
                            })
                            .findFirst()
                            .orElse("");
                })
                .orElse("");
    }

    /**
     * 解析返回值的描述
     */
    public String parseReturnDescription(MethodDeclaration methodDeclaration) {
        return methodDeclaration.getJavadocComment()
                .map(JavadocComment::parse)
                .map(javadoc -> {
                    // 查找@return标签
                    return javadoc.getBlockTags().stream()
                            .filter(tag -> tag.getTagName().equals("@return"))
                            .map(tag -> cleanDescription(tag.getContent().toText()))
                            .findFirst()
                            .orElse("");
                })
                .orElse("");
    }

    /**
     * 清理描述文本
     * 移除多余的空格、换行符等
     */
    private String cleanDescription(String description) {
        if (description == null || description.isEmpty()) {
            return "";
        }

        // 移除HTML标签
        description = description.replaceAll("<[^>]+>", "");

        // 替换多个空白为单个空格
        description = description.replaceAll("\\s+", " ");

        // 移除首尾空格
        description = description.trim();

        return description;
    }
}
