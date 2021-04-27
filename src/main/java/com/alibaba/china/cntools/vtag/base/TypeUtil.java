package com.alibaba.china.cntools.vtag.base;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class TypeUtil {

    private static final Class<?>[] CLASSES = new Class<?>[] {
        Boolean.class, Byte.class, Character.class,
        String.class, Number.class, Integer.class, Long.class, Double.class, Float.class, Date.class, Object.class
    };

    private static final List<String> BUILTIN_TYPES = new ArrayList<>();

    static {
        for (Class<?> clz : CLASSES) {
            BUILTIN_TYPES.add(clz.getSimpleName().toLowerCase());
        }
    }

    public static boolean isPrimitive(String type) {
        return BUILTIN_TYPES.contains(type);
    }

    public static Class<?> getClass(String type) {
        int pos = BUILTIN_TYPES.indexOf(type);
        return pos == -1 ? null : CLASSES[pos];
    }
    
}
