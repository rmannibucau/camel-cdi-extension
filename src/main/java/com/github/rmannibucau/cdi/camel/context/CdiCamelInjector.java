package com.github.rmannibucau.cdi.camel.context;

import com.github.rmannibucau.cdi.camel.util.BeanManagerHelper;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelBeanPostProcessor;
import org.apache.camel.util.ReflectionInjector;
import org.apache.deltaspike.core.api.provider.BeanProvider;

import javax.enterprise.inject.spi.BeanManager;

public class CdiCamelInjector extends ReflectionInjector {
    private final DefaultCamelBeanPostProcessor processor;

    public CdiCamelInjector(final CamelContext context) {
        processor = new DefaultCamelBeanPostProcessor(context);
    }

    @Override
    public <T> T newInstance(final Class<T> type) {
        final BeanManager bm = BeanManagerHelper.get();
        T instance;
        if (bm != null && !type.getName().startsWith("org.apache.camel.")) { // skip camel components
            instance = BeanProvider.getContextualReference(type, true);
            if (instance != null) {
                try {
                    instance = (T) processor.postProcessAfterInitialization(instance, type.getName());
                    return (T) processor.postProcessBeforeInitialization(instance, type.getName());
                } catch (Exception e) {
                    return instance;
                }
            }
        }
        return super.newInstance(type);
    }
}
