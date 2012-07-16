package com.github.rmannibucau.cdi.camel.scope;

import org.apache.camel.Exchange;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.management.event.ExchangeCreatedEvent;
import org.apache.camel.management.event.ExchangeFailedEvent;
import org.apache.camel.support.EventNotifierSupport;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import java.lang.annotation.Annotation;
import java.util.EventObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// TODO: remove this threadlocal is possible to allow multithreading
public class ExchangeScope extends EventNotifierSupport implements Context {
    private static final String BEANS_KEY = ExchangeScope.class.getName();

    // used to share info between the scope and the listener (notifier)
    private static InheritableThreadLocal<Exchange> EXCHANGE = new InheritableThreadLocal<Exchange>();

    @Override
    public Class<? extends Annotation> getScope() {
        return ExchangeScoped.class;
    }

    @Override
    public <T> T get(final Contextual<T> component, final CreationalContext<T> creationalContext) {
        Instance instance = instance(component);
        if (instance == null) {
            final Object obj = component.create(creationalContext);
            instance = new Instance((Contextual<Object>) component, (CreationalContext<Object>) creationalContext, obj);
            map().put(component, instance);
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
        return EXCHANGE.get();
    }

    private Instance instance(final Contextual<?> component) {
        Map<Contextual<?>, Instance> map = map();
        if (map == null) {
            map = new ConcurrentHashMap<Contextual<?>, Instance>();
            exchange().getProperties().put(BEANS_KEY, map);
        }

        return map.get(component);
    }

    private Map<Contextual<?>, Instance> map() {
        final Exchange exchange = exchange();
        final Map<String, Object> props = exchange.getProperties();
        return  (Map<Contextual<?>, Instance>) props.get(ExchangeScope.class.getName());
    }

    @Override
    public void notify(final EventObject event) throws Exception {
        final Object exchange = event.getSource();
        if (exchange instanceof Exchange) {
            final Exchange ex = (Exchange) exchange;
            if (event instanceof ExchangeCreatedEvent) {
                init(ex);
            } else {
                destroy(ex);
            }
        }
    }

    private void init(final Exchange exchange) {
        EXCHANGE.set(exchange);
    }

    private void destroy(final Exchange ex) {
        final Map<Contextual<?>, Instance> map = map();
        if (map != null) {
            for (Map.Entry<Contextual<?>, Instance> instances : map.entrySet()) {
                instances.getValue().destroy();
            }
            map.clear();
        }
        ex.getProperties().remove(BEANS_KEY);
    }

    @Override
    public boolean isEnabled(final EventObject event) {
        return ExchangeCreatedEvent.class.equals(event.getClass())
                || ExchangeFailedEvent.class.equals(event.getClass())
                || ExchangeCompletedEvent.class.equals(event.getClass());
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
