package com.gallop.fastddd.doc.parser;

import com.gallop.fastddd.doc.annotation.ApiDescription;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 描述提取器
 * 综合提取Javadoc和@ApiDescription注解中的描述信息
 * 优先使用@ApiDescription注解，如果没有则使用Javadoc
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
@Slf4j
@Component
public class DescriptionExtractor {

    private final JavadocParser javadocParser;

    public DescriptionExtractor(JavadocParser javadocParser) {
        this.javadocParser = javadocParser;
    }

    /**
     * 提取类的描述信息
     * 优先使用@ApiDescription注解，其次使用Javadoc
     */
    public String extractClassDescription(TypeDeclaration<?> typeDeclaration) {
        // 首先检查@ApiDescription注解
        var apiDescAnnotation = typeDeclaration.getAnnotations().stream()
                .filter(ann -> ann.getNameAsString().equals("ApiDescription"))
                .findFirst();

        if (apiDescAnnotation.isPresent()) {
            return extractAnnotationDescription(apiDescAnnotation.get());
        }

        // 如果没有注解，使用Javadoc
        return javadocParser.parseClassDescription(typeDeclaration);
    }

    /**
     * 提取方法的摘要信息
     * 优先使用@ApiDescription注解，其次使用Javadoc
     */
    public String extractMethodSummary(MethodDeclaration methodDeclaration) {
        // 首先检查@ApiDescription注解
        var apiDescAnnotation = methodDeclaration.getAnnotations().stream()
                .filter(ann -> ann.getNameAsString().equals("ApiDescription"))
                .findFirst();

        if (apiDescAnnotation.isPresent()) {
            String description = extractAnnotationDescription(apiDescAnnotation.get());
            // 使用第一行作为摘要
            String[] lines = description.split("\n");
            return lines.length > 0 ? lines[0].trim() : description;
        }

        // 如果没有注解，使用Javadoc
        return javadocParser.parseMethodSummary(methodDeclaration);
    }

    /**
     * 提取方法的详细描述
     * 优先使用@ApiDescription注解，其次使用Javadoc
     */
    public String extractMethodDescription(MethodDeclaration methodDeclaration) {
        // 首先检查@ApiDescription注解
        var apiDescAnnotation = methodDeclaration.getAnnotations().stream()
                .filter(ann -> ann.getNameAsString().equals("ApiDescription"))
                .findFirst();

        if (apiDescAnnotation.isPresent()) {
            return extractAnnotationDescription(apiDescAnnotation.get());
        }

        // 如果没有注解，使用Javadoc
        return javadocParser.parseMethodDescription(methodDeclaration);
    }

    /**
     * 提取参数的描述信息
     * 优先使用参数上的@ApiDescription注解，其次使用方法Javadoc中的@param
     */
    public String extractParameterDescription(MethodDeclaration methodDeclaration, Parameter parameter) {
        String parameterName = parameter.getNameAsString();

        // 首先检查参数上的@ApiDescription注解
        var apiDescAnnotation = parameter.getAnnotations().stream()
                .filter(ann -> ann.getNameAsString().equals("ApiDescription"))
                .findFirst();

        if (apiDescAnnotation.isPresent()) {
            return extractAnnotationDescription(apiDescAnnotation.get());
        }

        // 如果没有注解，使用Javadoc中的@param
        return javadocParser.parseParameterDescription(methodDeclaration, parameterName);
    }

    /**
     * 提取返回值的描述信息
     * 使用方法Javadoc中的@return
     */
    public String extractReturnDescription(MethodDeclaration methodDeclaration) {
        // 检查方法上的@ApiDescription注解中的detail属性
        var apiDescAnnotation = methodDeclaration.getAnnotations().stream()
                .filter(ann -> ann.getNameAsString().equals("ApiDescription"))
                .findFirst();

        if (apiDescAnnotation.isPresent()) {
            String detail = extractAnnotationDetail(apiDescAnnotation.get());
            if (!detail.isEmpty()) {
                return detail;
            }
        }

        // 如果没有detail，使用Javadoc中的@return
        return javadocParser.parseReturnDescription(methodDeclaration);
    }

    /**
     * 提取参数的示例值
     * 从@ApiDescription注解的example属性中提取
     */
    public String extractParameterExample(Parameter parameter) {
        var apiDescAnnotation = parameter.getAnnotations().stream()
                .filter(ann -> ann.getNameAsString().equals("ApiDescription"))
                .findFirst();

        if (apiDescAnnotation.isPresent()) {
            return extractAnnotationExample(apiDescAnnotation.get());
        }

        return null;
    }

    /**
     * 检查方法是否已废弃
     * 从@ApiDescription注解的deprecated属性或@Deprecated注解中提取
     */
    public boolean isMethodDeprecated(MethodDeclaration methodDeclaration) {
        // 检查@ApiDescription注解
        var apiDescAnnotation = methodDeclaration.getAnnotations().stream()
                .filter(ann -> ann.getNameAsString().equals("ApiDescription"))
                .findFirst();

        if (apiDescAnnotation.isPresent()) {
            if (extractAnnotationDeprecated(apiDescAnnotation.get())) {
                return true;
            }
        }

        // 检查@Deprecated注解
        return methodDeclaration.getAnnotations().stream()
                .anyMatch(ann -> ann.getNameAsString().equals("Deprecated"));
    }

    /**
     * 从注解中提取描述值
     */
    private String extractAnnotationDescription(com.github.javaparser.ast.expr.AnnotationExpr annotation) {
        if (annotation.isSingleMemberAnnotationExpr()) {
            // 简写形式：@ApiDescription("描述")
            var value = annotation.asSingleMemberAnnotationExpr().getMemberValue();
            if (value.isStringLiteralExpr()) {
                return value.asStringLiteralExpr().getValue();
            }
        } else if (annotation.isNormalAnnotationExpr()) {
            // 完整形式：@ApiDescription(value = "描述")
            var normalAnnotation = annotation.asNormalAnnotationExpr();
            for (var pair : normalAnnotation.getPairs()) {
                if ("value".equals(pair.getNameAsString())) {
                    if (pair.getValue().isStringLiteralExpr()) {
                        return pair.getValue().asStringLiteralExpr().getValue();
                    }
                }
            }
        }
        return "";
    }

    /**
     * 从注解中提取详细描述
     */
    private String extractAnnotationDetail(com.github.javaparser.ast.expr.AnnotationExpr annotation) {
        if (annotation.isNormalAnnotationExpr()) {
            var normalAnnotation = annotation.asNormalAnnotationExpr();
            for (var pair : normalAnnotation.getPairs()) {
                if ("detail".equals(pair.getNameAsString())) {
                    if (pair.getValue().isStringLiteralExpr()) {
                        return pair.getValue().asStringLiteralExpr().getValue();
                    }
                }
            }
        }
        return "";
    }

    /**
     * 从注解中提取示例值
     */
    private String extractAnnotationExample(com.github.javaparser.ast.expr.AnnotationExpr annotation) {
        if (annotation.isNormalAnnotationExpr()) {
            var normalAnnotation = annotation.asNormalAnnotationExpr();
            for (var pair : normalAnnotation.getPairs()) {
                if ("example".equals(pair.getNameAsString())) {
                    if (pair.getValue().isStringLiteralExpr()) {
                        return pair.getValue().asStringLiteralExpr().getValue();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 从注解中提取deprecated标志
     */
    private boolean extractAnnotationDeprecated(com.github.javaparser.ast.expr.AnnotationExpr annotation) {
        if (annotation.isNormalAnnotationExpr()) {
            var normalAnnotation = annotation.asNormalAnnotationExpr();
            for (var pair : normalAnnotation.getPairs()) {
                if ("deprecated".equals(pair.getNameAsString())) {
                    if (pair.getValue().isBooleanLiteralExpr()) {
                        return pair.getValue().asBooleanLiteralExpr().getValue();
                    }
                }
            }
        }
        return false;
    }
}
