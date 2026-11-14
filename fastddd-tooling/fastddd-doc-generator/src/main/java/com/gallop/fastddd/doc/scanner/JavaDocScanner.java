package com.gallop.fastddd.doc.scanner;

import com.gallop.fastddd.doc.model.*;
import com.gallop.fastddd.doc.parser.AnnotationParser;
import com.gallop.fastddd.doc.parser.DescriptionExtractor;
import com.gallop.fastddd.doc.parser.TypeExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Java源代码API文档扫描器实现
 * 使用JavaParser扫描Java源代码文件，提取Spring Web注解信息
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
@Slf4j
@Component
public class JavaDocScanner implements DocScanner {

    private final DescriptionExtractor descriptionExtractor;
    private final AnnotationParser annotationParser;
    private final TypeExtractor typeExtractor;

    public JavaDocScanner(DescriptionExtractor descriptionExtractor, AnnotationParser annotationParser, TypeExtractor typeExtractor) {
        this.descriptionExtractor = descriptionExtractor;
        this.annotationParser = annotationParser;
        this.typeExtractor = typeExtractor;
    }

    @Override
    public ApiDoc scan(String basePackage, String sourceRoot) {
        log.info("开始扫描API文档，基础包: {}, 源码目录: {}", basePackage, sourceRoot);

        ApiDoc apiDoc = new ApiDoc();
        apiDoc.setBasePackage(basePackage);
        apiDoc.setGeneratedAt(LocalDateTime.now());

        // 转换包路径为文件路径
        String packagePath = basePackage.replace('.', File.separatorChar);
        Path scanPath = Paths.get(sourceRoot, packagePath);

        if (!Files.exists(scanPath)) {
            log.warn("扫描路径不存在: {}", scanPath);
            return apiDoc;
        }

        log.debug("扫描目录: {}", scanPath.toAbsolutePath());

        // 扫描目录下的所有Java文件
        List<File> javaFiles = findJavaFiles(scanPath);
        log.info("找到 {} 个Java文件", javaFiles.size());

        // 解析每个Java文件
        for (File javaFile : javaFiles) {
            try {
                parseJavaFile(javaFile, basePackage, apiDoc);
            } catch (Exception e) {
                log.error("解析文件失败: {}", javaFile.getName(), e);
            }
        }

        log.info("扫描完成，发现 {} 个控制器", apiDoc.getControllers().size());
        return apiDoc;
    }

    /**
     * 解析单个Java文件
     */
    private void parseJavaFile(File javaFile, String basePackage, ApiDoc apiDoc) {
        log.debug("解析文件: {}", javaFile.getName());

        try {
            // 使用JavaParser解析Java文件
            var compilationUnit = annotationParser.parseFile(javaFile);

            // 检查是否是控制器（有@RestController注解或@Controller注解）
            if (!annotationParser.isRestController(compilationUnit)) {
                log.trace("文件 {} 不是REST控制器，跳过", javaFile.getName());
                return;
            }

            // 提取控制器信息
            ApiController controller = extractControllerInfo(compilationUnit, basePackage);
            if (controller != null) {
                apiDoc.addController(controller);
                log.debug("发现控制器: {}", controller.getClassName());
            }

        } catch (Exception e) {
            log.error("解析Java文件异常: {}", javaFile.getName(), e);
        }
    }

    /**
     * 提取控制器信息
     */
    private ApiController extractControllerInfo(com.github.javaparser.ast.CompilationUnit cu, String basePackage) {
        var classOpt = cu.getClassByName("");
        if (classOpt.isEmpty()) {
            // 获取主类（通常是第一个类）
            var classes = cu.getTypes();
            if (classes.isEmpty()) {
                return null;
            }
        }

        var typeDeclaration = cu.getPrimaryType();
        if (typeDeclaration.isEmpty()) {
            return null;
        }

        ApiController controller = new ApiController();

        // 提取类名信息
        String className = typeDeclaration.get().getNameAsString();
        controller.setClassName(className);
        controller.setSimpleClassName(className);

        // 提取完整限定名
        cu.getPackageDeclaration().ifPresent(pkg -> {
            String packageName = pkg.getNameAsString();
            controller.setQualifiedName(packageName + "." + className);
            controller.setPackageName(packageName);
        });

        // 提取描述（使用DescriptionExtractor，优先使用@ApiDescription注解）
        String description = descriptionExtractor.extractClassDescription(typeDeclaration.get());
        controller.setDescription(description);

        // 提取@RequestMapping注解中的基础路径
        String basePath = annotationParser.extractBasePath(typeDeclaration.get());
        controller.setBasePath(basePath);

        // 提取所有API方法
        List<ApiMethod> methods = extractApiMethods(typeDeclaration.get(), basePath);
        controller.getMethods().addAll(methods);

        return controller;
    }

    /**
     * 提取API端点方法
     */
    private List<ApiMethod> extractApiMethods(com.github.javaparser.ast.body.TypeDeclaration<?> typeDeclaration, String basePath) {
        List<ApiMethod> methods = new ArrayList<>();

        for (var member : typeDeclaration.getMembers()) {
            if (member.isMethodDeclaration()) {
                var methodDecl = member.asMethodDeclaration();

                // 检查是否有Spring Web注解（@GetMapping, @PostMapping等）
                if (annotationParser.isApiMethod(methodDecl)) {
                    ApiMethod apiMethod = extractMethodInfo(methodDecl, basePath);
                    if (apiMethod != null) {
                        methods.add(apiMethod);
                    }
                }
            }
        }

        return methods;
    }

    /**
     * 提取单个API方法信息
     */
    private ApiMethod extractMethodInfo(com.github.javaparser.ast.body.MethodDeclaration methodDecl, String basePath) {
        ApiMethod apiMethod = new ApiMethod();

        // 提取方法名
        String methodName = methodDecl.getNameAsString();
        apiMethod.setMethodName(methodName);

        // 提取HTTP方法和路径
        var mappingInfo = annotationParser.extractRequestMapping(methodDecl);
        if (mappingInfo == null) {
            return null;
        }

        apiMethod.setHttpMethod(ApiMethod.HttpMethod.fromString(mappingInfo.httpMethod()));
        apiMethod.setPath(mappingInfo.path());

        // 构建完整路径
        String fullPath = buildFullPath(basePath, mappingInfo.path());
        apiMethod.setFullPath(fullPath);

        // 提取描述（使用DescriptionExtractor，优先使用@ApiDescription注解）
        String summary = descriptionExtractor.extractMethodSummary(methodDecl);
        String description = descriptionExtractor.extractMethodDescription(methodDecl);
        apiMethod.setSummary(summary);
        apiMethod.setDescription(description);

        // 检查是否已废弃
        apiMethod.setDeprecated(descriptionExtractor.isMethodDeprecated(methodDecl));

        // 提取返回类型（使用TypeExtractor获取详细类型信息）
        var typeInfo = typeExtractor.extractTypeInfo(methodDecl.getType());
        String returnType = typeInfo.getDisplayName();

        // 如果是Result包装类型，尝试提取实际数据类型
        if (typeExtractor.isResultWrapperType(returnType)) {
            String actualType = typeExtractor.extractTypeFromResultWrapper(returnType);
            // 对于Result包装类型，我们显示实际类型
            if (!actualType.equals(returnType)) {
                returnType = actualType + " (wrapped in Result)";
            }
        }

        apiMethod.setReturnType(returnType);

        // 提取参数
        var parameters = annotationParser.extractParameters(methodDecl);
        apiMethod.getParameters().addAll(parameters);

        // TODO: 提取请求体和响应体信息（在后续子任务中实现）

        return apiMethod;
    }

    /**
     * 构建完整URL路径
     */
    private String buildFullPath(String basePath, String methodPath) {
        StringBuilder fullPath = new StringBuilder();

        if (basePath != null && !basePath.isEmpty()) {
            if (!basePath.startsWith("/")) {
                fullPath.append("/");
            }
            fullPath.append(basePath);
        }

        if (methodPath != null && !methodPath.isEmpty()) {
            if (!methodPath.startsWith("/")) {
                fullPath.append("/");
            }
            fullPath.append(methodPath);
        }

        if (fullPath.length() == 0) {
            return "/";
        }

        return fullPath.toString();
    }

    /**
     * 递归查找所有Java文件
     */
    private List<File> findJavaFiles(Path directory) {
        List<File> javaFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(directory)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> javaFiles.add(p.toFile()));
        } catch (IOException e) {
            log.error("遍历目录失败: {}", directory, e);
        }

        return javaFiles;
    }

    /**
     * 请求映射信息记录
     */
    private record RequestMappingInfo(String httpMethod, String path) {}
}
