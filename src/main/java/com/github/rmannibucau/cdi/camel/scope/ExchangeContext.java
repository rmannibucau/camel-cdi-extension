package com.github.rmannibucau.cdi.camel.scope;

import com.github.rmannibucau.cdi.camel.util.ExchangeHolder;
import org.apache.camel.Exchange;
import org.apache.camel.management.event.AbstractExchangeEvent;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.management.event.ExchangeFailedEvent;
import org.apache.camel.support.EventNotifierSupport;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import java.lang.annotation.Annotation;
import java.util.EventObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExchangeContext extends EventNotifierSupport implements Context {
    private static final String BEANS_KEY = ExchangeContext.class.getName();

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
        return  (Map<Contextual<?>, Instance>) props.get(ExchangeContext.class.getName());
    }

    @Override
    public void notify(final EventObject event) throws Exception {
        final Exchange doneExchange = ((AbstractExchangeEvent) event).getExchange();
        final Map<Contextual<?>, Instance> map = map(doneExchange);
        if (map != null) {
            for (Map.Entry<Contextual<?>, Instance> entry : map.entrySet()) {
                entry.getValue().destroy();
            }
        }
    }

    @Override
    public boolean isEnabled(final EventObject event) {
        return ExchangeCompletedEvent.class.equals(event.getClass())
                || ExchangeFailedEvent.class.equals(event.getClass());
    }

    @Override
    protected void doStart() throws Exception {
        // no-op
    }

    @Override
    protected void doStop() throws Exception {
        // no-op
    }
}
