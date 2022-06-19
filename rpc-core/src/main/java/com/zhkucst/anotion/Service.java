package com.zhkucst.anotion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Auther: fyp
 * @Date: 2022/3/28
 * @Description:
 * @Package: com.zhkucst.anotion
 * @Version: 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    public String name() default "";
}
