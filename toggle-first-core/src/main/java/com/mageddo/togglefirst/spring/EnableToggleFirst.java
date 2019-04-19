package com.mageddo.togglefirst.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(ApplicationContextProvider.class)
public @interface EnableToggleFirst {
}
