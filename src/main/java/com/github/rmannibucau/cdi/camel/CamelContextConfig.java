package com.github.rmannibucau.cdi.camel;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface CamelContextConfig { // shouldn't be a qualifier
    String contextName() default "";
}
