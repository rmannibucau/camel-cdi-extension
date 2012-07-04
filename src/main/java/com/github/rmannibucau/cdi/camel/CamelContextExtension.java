package com.github.rmannibucau.cdi.camel;

import org.apache.camel.builder.RouteBuilder;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CamelContextExtension implements Extension {
    private final Map<String, List<Class<?>>> contextConfigs = new HashMap<String, List<Class<?>>>();

    protected void readConfig(@Observes final ProcessAnnotatedType<?> pat) {
        final AnnotatedType<?> at = pat.getAnnotatedType();
        if (at.isAnnotationPresent(CamelContextConfig.class)
                && RouteBuilder.class.isAssignableFrom(at.getJavaClass())) {
            final CamelContextConfig conf = at.getAnnotation(CamelContextConfig.class);
            final String id = conf.contextName();
            if (!contextConfigs.containsKey(id)) {
                contextConfigs.put(id, new ArrayList<Class<?>>());
            }
            contextConfigs.get(id).add(at.getJavaClass());
        }
    }

    protected void addCamelContexts(@Observes final AfterBeanDiscovery abd, final BeanManager bm) {
        for (Map.Entry<String, List<Class<?>>> entry : contextConfigs.entrySet()) {
            abd.addBean(new CamelContextBean(bm, entry.getKey(), entry.getValue()));
        }
        contextConfigs.clear();
    }
}
