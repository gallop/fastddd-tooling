package com.gallop.fastddd.codegen.util;

/**
 * 命名工具类
 *
 * @author gallop
 * @date 2023-10-10
 */
public class NamingUtils {

    /**
     * 下划线转驼峰命名
     *
     * @param name           下划线命名
     * @param capitalizeFirst 首字母是否大写
     * @return 驼峰命名
     */
    public static String underscoreToCamel(String name, boolean capitalizeFirst) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;
        
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    result.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        
        if (capitalizeFirst && result.length() > 0) {
            char firstChar = result.charAt(0);
            result.setCharAt(0, Character.toUpperCase(firstChar));
        }
        
        return result.toString();
    }

    /**
     * 驼峰转下划线命名
     *
     * @param name 驼峰命名
     * @return 下划线命名
     */
    public static String camelToUnderscore(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
}