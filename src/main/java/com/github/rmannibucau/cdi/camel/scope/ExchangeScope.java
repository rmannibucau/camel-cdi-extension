package com.github.rmannibucau.cdi.camel.scope;

import com.github.rmannibucau.cdi.camel.util.ExchangeHolder;
import org.apache.camel.Exchange;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExchangeScope implements Context {
    private static final String BEANS_KEY = ExchangeScope.class.getName();

    @Override
    public Class<? extends Annotation> getScope() {
        return ExchangeScoped.class;
    }

    @Override
    public <T> T get(final Contextual<T> component, final CreationalContext<T> creationalContext) {
        Instance instance = instance(component);
        if (instance == null) {
            synchronized (this) {
                instance = instance(component);
                if (instance == null) {
                    final Object obj = component.create(creationalContext);
                    instance = new Instance((Contextual<Object>) component, (CreationalContext<Object>) creationalContext, obj);
                    map(exchange()).put(component, instance);
                }
            }
        }
        return (T) instance.getInstance();
    }

    @Override
    public <T> T get(final Contextual<T> component) {
        final Instance instance = instance(component);
        if (instance == null) {
            return null;
        }
        return (T) instance.getInstance();
    }

    @Override
    public boolean isActive() {
        return exchange() != null;
    }

    private Exchange exchange() {
        return ExchangeHolder.get();
    }

    private Instance instance(final Contextual<?> component) {
        final Exchange exchange = exchange();
        Map<Contextual<?>, Instance> map = map(exchange);
        if (map == null) {
            synchronized (exchange) {
                map = map(exchange);
                if (map == null) {
                    map = new ConcurrentHashMap<Contextual<?>, Instance>();
                    exchange.getProperties().put(BEANS_KEY, map);
                }
            }
        }

        return map.get(component);
    }

    private Map<Contextual<?>, Instance> map(final Exchange exchange) {
        final Map<String, Object> props = exchange.getProperties();
        return  (Map<Contextual<?>, Instance>) props.get(ExchangeScope.class.getName());
    }
}
