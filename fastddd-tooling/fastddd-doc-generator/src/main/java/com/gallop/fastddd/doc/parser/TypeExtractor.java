package com.gallop.fastddd.doc.parser;

import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.resolution.types.ResolvedType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Java类型提取器
 * 负责解析Java类型信息，包括泛型参数、数组类型等
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
@Slf4j
@Component
public class TypeExtractor {

    /**
     * 提取类型信息
     */
    public TypeInfo extractTypeInfo(Type type) {
        TypeInfo typeInfo = new TypeInfo();

        if (type == null) {
            return typeInfo;
        }

        // 提取基础类型名
        typeInfo.setTypeName(type.asString());

        // 检查是否是数组类型
        typeInfo.setArray(type.isArrayType());

        // 检查是否是泛型类型
        if (type.isClassOrInterfaceType()) {
            ClassOrInterfaceType classType = type.asClassOrInterfaceType();

            // 提取类型参数
            if (classType.getTypeArguments().isPresent()) {
                var typeArgs = classType.getTypeArguments().get();
                typeInfo.setGeneric(true);
                typeInfo.setGeneric(true);

                List<String> typeParameters = new ArrayList<>();
                for (Type arg : typeArgs) {
                    typeParameters.add(arg.asString());
                }
                typeInfo.setTypeParameters(typeParameters);

                // 如果是集合类型，提取元素类型
                String elementType = extractCollectionElementType(classType);
                if (elementType != null) {
                    typeInfo.setCollectionType(true);
                    typeInfo.setElementType(elementType);
                }
            }

            // 检查是否是集合类型
            typeInfo.setCollectionType(isCollectionType(type.asString()));
        }

        // 检查是否是基础类型
        typeInfo.setPrimitive(isPrimitiveType(type.asString()));

        return typeInfo;
    }

    /**
     * 提取集合的元素类型
     */
    public String extractCollectionElementType(ClassOrInterfaceType type) {
        String typeName = type.getNameAsString();

        // 处理List/Set/Collection
        if (isListOrSetType(typeName)) {
            if (type.getTypeArguments().isPresent() && !type.getTypeArguments().get().isEmpty()) {
                return type.getTypeArguments().get().get(0).asString();
            }
        }

        // 处理Map
        if (isMapType(typeName)) {
            if (type.getTypeArguments().isPresent()) {
                var args = type.getTypeArguments().get();
                if (args.size() >= 2) {
                    return "Map<" + args.get(0).asString() + ", " + args.get(1).asString() + ">";
                }
            }
        }

        return null;
    }

    /**
     * 获取类型的简单名称
     */
    public String getSimpleTypeName(Type type) {
        if (type == null) {
            return "void";
        }
        return getSimpleTypeName(type.asString());
    }

    /**
     * 从类型名字符串获取简单名称
     */
    private String getSimpleTypeName(String typeName) {
        if (typeName == null) {
            return "";
        }

        // 移除泛型参数
        int genericIndex = typeName.indexOf('<');
        if (genericIndex > 0) {
            typeName = typeName.substring(0, genericIndex);
        }

        // 移除包名
        int lastDot = typeName.lastIndexOf('.');
        if (lastDot > 0) {
            typeName = typeName.substring(lastDot + 1);
        }

        return typeName;
    }

    /**
     * 获取类型的完整名称
     */
    public String getQualifiedTypeName(Type type) {
        if (type == null) {
            return "void";
        }
        return type.asString();
    }

    /**
     * 检查是否是List或Set类型
     */
    public boolean isListOrSetType(String typeName) {
        if (typeName == null) {
            return false;
        }

        String simpleName = getSimpleTypeName(typeName);
        return List.of("List", "Set", "Collection", "ArrayList",
                "HashSet", "LinkedList").contains(simpleName);
    }

    /**
     * 检查是否是Map类型
     */
    public boolean isMapType(String typeName) {
        if (typeName == null) {
            return false;
        }

        String simpleName = getSimpleTypeName(typeName);
        return List.of("Map", "HashMap", "LinkedHashMap").contains(simpleName);
    }

    /**
     * 检查是否是集合类型
     */
    public boolean isCollectionType(String typeName) {
        return isListOrSetType(typeName) || isMapType(typeName);
    }

    /**
     * 检查是否是基础类型
     */
    public boolean isPrimitiveType(String typeName) {
        if (typeName == null) {
            return false;
        }

        String simpleName = getSimpleTypeName(typeName);
        return List.of("String", "int", "Integer", "long", "Long",
                "double", "Double", "float", "Float", "boolean", "Boolean",
                "short", "Short", "byte", "Byte", "char", "Character").contains(simpleName);
    }

    /**
     * 检查是否是void类型
     */
    public boolean isVoidType(Type type) {
        if (type == null) {
            return false;
        }
        return type.isVoidType() || type.asString().equals("void");
    }

    /**
     * 检查是否是Result包装类型
     */
    public boolean isResultWrapperType(String typeName) {
        if (typeName == null) {
            return false;
        }
        return typeName.contains("Result") || typeName.contains("Response") ||
                typeName.contains("ApiResponse") || typeName.contains("R<");
    }

    /**
     * 从Result包装类型中提取实际数据类型
     */
    public String extractTypeFromResultWrapper(String typeName) {
        if (typeName == null || !isResultWrapperType(typeName)) {
            return typeName;
        }

        // 尝试从泛型参数中提取实际类型
        int genericStart = typeName.indexOf('<');
        int genericEnd = typeName.lastIndexOf('>');

        if (genericStart > 0 && genericEnd > genericStart) {
            String innerType = typeName.substring(genericStart + 1, genericEnd);
            return innerType;
        }

        return typeName;
    }

    /**
     * 类型信息封装类
     */
    public static class TypeInfo {
        private String typeName;
        private boolean array;
        private boolean generic;
        private List<String> typeParameters = new ArrayList<>();
        private boolean collectionType;
        private String elementType;
        private boolean primitive;

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public boolean isArray() {
            return array;
        }

        public void setArray(boolean array) {
            this.array = array;
        }

        public boolean isGeneric() {
            return generic;
        }

        public void setGeneric(boolean generic) {
            this.generic = generic;
        }

        public List<String> getTypeParameters() {
            return typeParameters;
        }

        public void setTypeParameters(List<String> typeParameters) {
            this.typeParameters = typeParameters;
        }

        public boolean isCollectionType() {
            return collectionType;
        }

        public void setCollectionType(boolean collectionType) {
            this.collectionType = collectionType;
        }

        public String getElementType() {
            return elementType;
        }

        public void setElementType(String elementType) {
            this.elementType = elementType;
        }

        public boolean isPrimitive() {
            return primitive;
        }

        public void setPrimitive(boolean primitive) {
            this.primitive = primitive;
        }

        /**
         * 获取显示名称
         */
        public String getDisplayName() {
            if (collectionType && elementType != null) {
                if (typeName.toLowerCase().contains("map")) {
                    // 对于Map类型，返回 Map<K, V> 格式
                    return getSimpleTypeName(typeName) + "<" + elementType + ">";
                } else {
                    // 对于List/Set类型，返回 List<T> 格式
                    return getSimpleTypeName(typeName) + "<" + elementType + ">";
                }
            } else if (generic && !typeParameters.isEmpty()) {
                StringBuilder sb = new StringBuilder(getSimpleTypeName(typeName));
                sb.append("<");
                for (int i = 0; i < typeParameters.size(); i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(getSimpleTypeName(typeParameters.get(i)));
                }
                sb.append(">");
                return sb.toString();
            }
            return typeName;
        }

        private String getSimpleTypeName(String typeName) {
            int lastDot = typeName.lastIndexOf('.');
            if (lastDot > 0) {
                return typeName.substring(lastDot + 1);
            }
            return typeName;
        }
    }
}
