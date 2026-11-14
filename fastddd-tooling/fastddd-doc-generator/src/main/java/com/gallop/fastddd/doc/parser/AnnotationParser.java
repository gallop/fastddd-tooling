package com.gallop.fastddd.doc.parser;

import com.gallop.fastddd.doc.model.ApiParameter;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Spring Web注解解析器
 * 解析Spring Web注解（@RestController, @RequestMapping, @GetMapping等）并提取相关信息
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
@Slf4j
@Component
public class AnnotationParser {

    private final TypeExtractor typeExtractor;
    private final DescriptionExtractor descriptionExtractor;

    public AnnotationParser(TypeExtractor typeExtractor, DescriptionExtractor descriptionExtractor) {
        this.typeExtractor = typeExtractor;
        this.descriptionExtractor = descriptionExtractor;
    }

    private static final String REST_CONTROLLER = "RestController";
    private static final String CONTROLLER = "Controller";
    private static final String REQUEST_MAPPING = "RequestMapping";
    private static final String GET_MAPPING = "GetMapping";
    private static final String POST_MAPPING = "PostMapping";
    private static final String PUT_MAPPING = "PutMapping";
    private static final String DELETE_MAPPING = "DeleteMapping";
    private static final String PATCH_MAPPING = "PatchMapping";

    private static final String PATH_VARIABLE = "PathVariable";
    private static final String REQUEST_PARAM = "RequestParam";
    private static final String REQUEST_HEADER = "RequestHeader";
    private static final String COOKIE_VALUE = "CookieValue";
    private static final String REQUEST_BODY = "RequestBody";

    /**
     * 解析Java文件
     */
    public CompilationUnit parseFile(File javaFile) throws IOException {
        JavaParser parser = new JavaParser();
        // 必须经 Path 解析：CompilationUnit 依赖 storage（文件名）判定主类型（getPrimaryType），
        // parse(String) 无 storage，isRestController 将恒为 false，源码扫描会得到 0 个控制器。
        // 默认字符集即 UTF-8，显式声明以保持与既有读取行为一致。
        parser.getParserConfiguration().setCharacterEncoding(StandardCharsets.UTF_8);
        var parseResult = parser.parse(javaFile.toPath());
        if (parseResult.isSuccessful()) {
            return parseResult.getResult().orElseThrow();
        }
        throw new IOException("Failed to parse Java file: " + javaFile.getName() +
                ", errors: " + parseResult.getProblems());
    }

    /**
     * 检查是否是REST控制器
     */
    public boolean isRestController(CompilationUnit cu) {
        var primaryType = cu.getPrimaryType();
        if (primaryType.isEmpty()) {
            return false;
        }

        TypeDeclaration<?> typeDeclaration = primaryType.get();
        return hasAnnotation(typeDeclaration, REST_CONTROLLER) ||
               (hasAnnotation(typeDeclaration, CONTROLLER) && hasResponseBodyAnnotation(typeDeclaration));
    }

    /**
     * 检查类型是否有指定注解
     */
    public boolean hasAnnotation(TypeDeclaration<?> typeDeclaration, String annotationName) {
        return typeDeclaration.getAnnotations().stream()
                .anyMatch(ann -> ann.getNameAsString().equals(annotationName));
    }

    /**
     * 检查是否有@ResponseBody注解
     */
    private boolean hasResponseBodyAnnotation(TypeDeclaration<?> typeDeclaration) {
        return typeDeclaration.getAnnotations().stream()
                .anyMatch(ann -> ann.getNameAsString().equals("ResponseBody"));
    }

    /**
     * 检查方法是否是API方法
     */
    public boolean isApiMethod(MethodDeclaration methodDeclaration) {
        List<String> mappingAnnotations = List.of(
                REQUEST_MAPPING, GET_MAPPING, POST_MAPPING,
                PUT_MAPPING, DELETE_MAPPING, PATCH_MAPPING
        );

        return methodDeclaration.getAnnotations().stream()
                .anyMatch(ann -> mappingAnnotations.contains(ann.getNameAsString()));
    }

    /**
     * 提取控制器的基础路径
     */
    public String extractBasePath(TypeDeclaration<?> typeDeclaration) {
        Optional<AnnotationExpr> mappingAnnotation = typeDeclaration.getAnnotations().stream()
                .filter(ann -> ann.getNameAsString().equals(REQUEST_MAPPING))
                .findFirst();

        if (mappingAnnotation.isPresent()) {
            return extractPathValue(mappingAnnotation.get());
        }

        return "";
    }

    /**
     * 提取请求映射信息
     */
    public RequestMappingInfo extractRequestMapping(MethodDeclaration methodDeclaration) {
        // 查找具体的映射注解
        List<String> mappingAnnotations = List.of(
                GET_MAPPING, POST_MAPPING, PUT_MAPPING, DELETE_MAPPING, PATCH_MAPPING, REQUEST_MAPPING
        );

        for (String annotationName : mappingAnnotations) {
            Optional<AnnotationExpr> annotation = methodDeclaration.getAnnotations().stream()
                    .filter(ann -> ann.getNameAsString().equals(annotationName))
                    .findFirst();

            if (annotation.isPresent()) {
                String httpMethod = extractHttpMethod(annotationName);
                String path = extractPathValue(annotation.get());
                return new RequestMappingInfo(httpMethod, path);
            }
        }

        return null;
    }

    /**
     * 从注解名称提取HTTP方法
     */
    private String extractHttpMethod(String annotationName) {
        return switch (annotationName) {
            case GET_MAPPING -> "GET";
            case POST_MAPPING -> "POST";
            case PUT_MAPPING -> "PUT";
            case DELETE_MAPPING -> "DELETE";
            case PATCH_MAPPING -> "PATCH";
            case REQUEST_MAPPING -> {
                // @RequestMapping需要检查method属性
                yield "GET"; // 默认返回GET，实际应该从method属性读取
            }
            default -> "GET";
        };
    }

    /**
     * 提取注解中的path值
     */
    public String extractPathValue(AnnotationExpr annotation) {
        // 处理简写形式：@GetMapping("/path")
        if (annotation.isSingleMemberAnnotationExpr()) {
            var value = annotation.asSingleMemberAnnotationExpr().getMemberValue();
            if (value.isStringLiteralExpr()) {
                return value.asStringLiteralExpr().getValue();
            }
        }

        // 处理完整形式：@GetMapping(path = "/path") 或 @GetMapping(value = "/path")
        if (annotation.isNormalAnnotationExpr()) {
            var normalAnnotation = annotation.asNormalAnnotationExpr();

            // 尝试获取path属性
            for (var pair : normalAnnotation.getPairs()) {
                String name = pair.getNameAsString();
                if ("path".equals(name) || "value".equals(name)) {
                    if (pair.getValue().isStringLiteralExpr()) {
                        return pair.getValue().asStringLiteralExpr().getValue();
                    }
                }
            }
        }

        return "";
    }

    /**
     * 提取方法参数信息
     */
    public List<ApiParameter> extractParameters(MethodDeclaration methodDeclaration) {
        List<ApiParameter> parameters = new ArrayList<>();

        for (Parameter parameter : methodDeclaration.getParameters()) {
            ApiParameter apiParameter = extractParameterInfo(parameter, methodDeclaration);
            if (apiParameter != null) {
                parameters.add(apiParameter);
            }
        }

        return parameters;
    }

    /**
     * 提取单个参数信息
     */
    private ApiParameter extractParameterInfo(Parameter parameter, MethodDeclaration methodDeclaration) {
        ApiParameter apiParameter = new ApiParameter();

        // 提取参数名
        apiParameter.setName(parameter.getNameAsString());

        // 使用TypeExtractor提取详细类型信息
        var typeInfo = typeExtractor.extractTypeInfo(parameter.getType());
        apiParameter.setType(typeInfo.getDisplayName());

        // 如果是集合类型，添加额外信息
        if (typeInfo.isCollectionType()) {
            // 对于集合类型，我们在type中已经包含了泛型信息
            // 例如: List<String> 而不是 List
        }

        // 确定参数位置
        ApiParameter.ParameterLocation location = determineParameterLocation(parameter);
        apiParameter.setLocation(location);

        // 提取注解中的属性
        extractParameterAttributes(parameter, apiParameter);

        // 提取参数描述（使用DescriptionExtractor，优先使用@ApiDescription注解）
        String description = descriptionExtractor.extractParameterDescription(methodDeclaration, parameter);
        apiParameter.setDescription(description);

        // 提取示例值
        String example = descriptionExtractor.extractParameterExample(parameter);
        if (example != null && !example.isEmpty()) {
            apiParameter.setExample(example);
        }

        // 如果是RequestBody，标记为BODY位置
        if (hasAnnotation(parameter, REQUEST_BODY)) {
            apiParameter.setLocation(ApiParameter.ParameterLocation.BODY);
        }

        return apiParameter;
    }

    /**
     * 确定参数位置
     */
    private ApiParameter.ParameterLocation determineParameterLocation(Parameter parameter) {
        if (hasAnnotation(parameter, PATH_VARIABLE)) {
            return ApiParameter.ParameterLocation.PATH;
        } else if (hasAnnotation(parameter, REQUEST_PARAM)) {
            return ApiParameter.ParameterLocation.QUERY;
        } else if (hasAnnotation(parameter, REQUEST_HEADER)) {
            return ApiParameter.ParameterLocation.HEADER;
        } else if (hasAnnotation(parameter, COOKIE_VALUE)) {
            return ApiParameter.ParameterLocation.COOKIE;
        } else if (hasAnnotation(parameter, REQUEST_BODY)) {
            return ApiParameter.ParameterLocation.BODY;
        }
        return ApiParameter.ParameterLocation.UNKNOWN;
    }

    /**
     * 检查参数是否有指定注解
     */
    private boolean hasAnnotation(Parameter parameter, String annotationName) {
        return parameter.getAnnotations().stream()
                .anyMatch(ann -> ann.getNameAsString().equals(annotationName));
    }

    /**
     * 提取参数注解属性（如required, defaultValue等）
     */
    private void extractParameterAttributes(Parameter parameter, ApiParameter apiParameter) {
        for (AnnotationExpr annotation : parameter.getAnnotations()) {
            String annotationName = annotation.getNameAsString();

            if (!List.of(PATH_VARIABLE, REQUEST_PARAM, REQUEST_HEADER, COOKIE_VALUE, REQUEST_BODY).contains(annotationName)) {
                continue;
            }

            if (annotation.isNormalAnnotationExpr()) {
                NormalAnnotationExpr normalAnnotation = annotation.asNormalAnnotationExpr();

                for (MemberValuePair pair : normalAnnotation.getPairs()) {
                    String name = pair.getNameAsString();

                    switch (name) {
                        case "name", "value" -> {
                            if (pair.getValue().isStringLiteralExpr()) {
                                String paramName = pair.getValue().asStringLiteralExpr().getValue();
                                if (!paramName.isEmpty()) {
                                    apiParameter.setName(paramName);
                                }
                            }
                        }
                        case "required" -> {
                            if (pair.getValue().isBooleanLiteralExpr()) {
                                apiParameter.setRequired(pair.getValue().asBooleanLiteralExpr().getValue());
                            } else if (pair.getValue().isStringLiteralExpr()) {
                                String value = pair.getValue().asStringLiteralExpr().getValue();
                                apiParameter.setRequired(Boolean.parseBoolean(value));
                            }
                        }
                        case "defaultValue" -> {
                            if (pair.getValue().isStringLiteralExpr()) {
                                apiParameter.setDefaultValue(pair.getValue().asStringLiteralExpr().getValue());
                            }
                        }
                    }
                }
            } else if (annotation.isSingleMemberAnnotationExpr()) {
                // 简写形式：@PathVariable("id") 或 @RequestParam("name")
                var value = annotation.asSingleMemberAnnotationExpr().getMemberValue();
                if (value.isStringLiteralExpr()) {
                    String paramName = value.asStringLiteralExpr().getValue();
                    if (!paramName.isEmpty()) {
                        apiParameter.setName(paramName);
                    }
                }
            }

            // 默认情况下，@PathVariable和@RequestParam的required默认为true
            if (List.of(PATH_VARIABLE, REQUEST_PARAM).contains(annotationName)) {
                // 检查是否显式设置了required
                boolean hasRequired = annotation.isNormalAnnotationExpr() &&
                        annotation.asNormalAnnotationExpr().getPairs().stream()
                                .anyMatch(pair -> pair.getNameAsString().equals("required"));

                if (!hasRequired) {
                    apiParameter.setRequired(true);
                }
            }
        }
    }

    /**
     * 请求映射信息记录
     */
    public record RequestMappingInfo(String httpMethod, String path) {}
}
