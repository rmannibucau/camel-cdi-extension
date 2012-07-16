package com.github.rmannibucau.cdi.camel.bean;

import com.github.rmannibucau.cdi.camel.scope.ExchangeScoped;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;

@ExchangeScoped
public class BeanWithInjection {
    @EndpointInject(uri = "direct:bridge")
    private ProducerTemplate tpl;

    public void enclose(final String body) {
        tpl.sendBody(body);
    }
}
