package com.github.rmannibucau.cdi.camel.context;

import com.github.rmannibucau.cdi.camel.registry.CdiRegistry;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;

public final class CdiCamelContext extends DefaultCamelContext {
    public CdiCamelContext() {
        super(new CdiRegistry());
    }

    @Override
    public void startRoute(final RouteDefinition route) throws Exception {
        route.getInterceptStrategies().add(new CdiInterceptStrategy());
        super.startRoute(route);
    }
}
