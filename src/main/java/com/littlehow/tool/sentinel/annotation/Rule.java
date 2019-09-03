package com.littlehow.tool.sentinel.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Rule {
    Degrade[] degrades() default {};
    Flow[] flows() default {};
}
