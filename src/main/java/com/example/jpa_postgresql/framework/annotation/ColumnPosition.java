package com.example.jpa_postgresql.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * com.example.jpa_postgresql.annotation
 * └ ColumnPosition
 *
 *
 * </pre>
 *
 * @author : hycho
 * @date : 2022-12-01
 **/
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnPosition {
    int value();
}
