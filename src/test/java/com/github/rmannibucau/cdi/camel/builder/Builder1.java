package com.github.rmannibucau.cdi.camel.builder;

import com.github.rmannibucau.cdi.camel.CamelContextConfig;
import org.apache.camel.builder.RouteBuilder;

@CamelContextConfig(contextName = "context1")
public class Builder1 extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("timer://b1?fixedRate=true&period=60000").to("log:builder1");
    }
}
