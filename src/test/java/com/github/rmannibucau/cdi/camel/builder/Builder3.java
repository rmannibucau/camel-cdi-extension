package com.github.rmannibucau.cdi.camel.builder;

import com.github.rmannibucau.cdi.camel.context.CamelContextConfig;
import org.apache.camel.builder.RouteBuilder;

@CamelContextConfig(contextName = "context2")
public class Builder3 extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("timer://b3?fixedRate=true&period=60000").to("log:builder1");
    }
}
