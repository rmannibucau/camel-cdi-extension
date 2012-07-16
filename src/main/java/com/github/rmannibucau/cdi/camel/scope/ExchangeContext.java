package com.github.rmannibucau.cdi.camel.scope;

import com.github.rmannibucau.cdi.camel.util.ExchangeHolder;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelBeanPostProcessor;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExchangeContext extends EventNotifierSupport implements Context {
    private static final Logger LOGGER = Logger.getLogger(ExchangeContext.class.getName());

    private static final String BEANS_KEY = ExchangeContext.class.getName();

    @Override
    public Class<? extends Annotation> getScope() {
        return ExchangeScoped.class;
    }

    @Override
    public <T> T get(final Contextual<T> component, final CreationalContext<T> creationalContext) {
        ExchangeInstance instance = instance(component);
        if (instance == null) {
            synchronized (this) {
                instance = instance(component);
                if (instance == null) {
                    final Exchange exchange = exchange();
                    Object obj = component.create(creationalContext);
                    try {
                        obj = new DefaultCamelBeanPostProcessor(exchange.getContext()).postProcessBeforeInitialization(obj, null);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "can't inject camel field in '" + obj + "'", e);
                    }

                    instance = new ExchangeInstance((Contextual<Object>) component, (CreationalContext<Object>) creationalContext, obj);
                    map(exchange).put(component, instance);
                }
            }
        }
        return (T) instance.getInstance();
    }

    @Override
    public <T> T get(final Contextual<T> component) {
        final ExchangeInstance instance = instance(component);
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

    private ExchangeInstance instance(final Contextual<?> component) {
        final Exchange exchange = exchange();
        Map<Contextual<?>, ExchangeInstance> map = map(exchange);
        if (map == null) {
            synchronized (exchange) {
                map = map(exchange);
                if (map == null) {
                    map = new ConcurrentHashMap<Contextual<?>, ExchangeInstance>();
                    exchange.getProperties().put(BEANS_KEY, map);
                }
            }
        }

        return map.get(component);
    }

    private Map<Contextual<?>, ExchangeInstance> map(final Exchange exchange) {
        final Map<String, Object> props = exchange.getProperties();
        return  (Map<Contextual<?>, ExchangeInstance>) props.get(ExchangeContext.class.getName());
    }

    @Override
    public void notify(final EventObject event) throws Exception {
        final Exchange doneExchange = ((AbstractExchangeEvent) event).getExchange();
        final Map<Contextual<?>, ExchangeInstance> map = map(doneExchange);
        if (map != null) {
            for (Map.Entry<Contextual<?>, ExchangeInstance> entry : map.entrySet()) {
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
