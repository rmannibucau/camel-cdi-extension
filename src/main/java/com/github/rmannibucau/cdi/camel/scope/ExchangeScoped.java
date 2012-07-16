package com.github.rmannibucau.cdi.camel.scope;

import javax.enterprise.context.NormalScope;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@NormalScope
@Retention(RetentionPolicy.RUNTIME)
public @interface ExchangeScoped {
}
