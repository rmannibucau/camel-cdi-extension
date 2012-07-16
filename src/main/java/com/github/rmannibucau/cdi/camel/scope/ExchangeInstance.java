package com.github.rmannibucau.cdi.camel.scope;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

public class ExchangeInstance {
    private Contextual<Object> contextual;
    private CreationalContext<Object> context;
    private Object instance;

    public ExchangeInstance() {
        // no-op
    }

    public ExchangeInstance(final Contextual<Object> component, final CreationalContext<Object> creationalContext, final Object obj) {
        contextual = component;
        context = creationalContext;
        instance = obj;
    }

    public CreationalContext<?> getContext() {
        return context;
    }

    public void setContext(CreationalContext<Object> context) {
        this.context = context;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Contextual<Object> getContextual() {
        return contextual;
    }

    public void setContextual(Contextual<Object> contextual) {
        this.contextual = contextual;
    }

    public void destroy() {
        if (contextual != null) {
            contextual.destroy(instance, context);
        }
        if (context != null) {
            context.release();
        }
    }
}
