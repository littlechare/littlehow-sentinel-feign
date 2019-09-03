package com.littlehow.tool.sentinel.annotation;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Flow {
    int grade() default RuleConstant.FLOW_GRADE_QPS;
    double count() default 500;
}
