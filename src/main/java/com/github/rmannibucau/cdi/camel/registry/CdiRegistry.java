package com.github.rmannibucau.cdi.camel.registry;

import org.apache.camel.spi.Registry;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.api.provider.BeanProvider;

import java.util.Collections;
import java.util.Map;

public class CdiRegistry implements Registry {
    @Override
    public Object lookup(final String name) {
        if (notValid()) {
            return null;
        }
        return BeanProvider.getContextualReference(name);
    }

    @Override
    public <T> T lookup(final String name, final Class<T> type) {
        if (notValid()) {
            return null;
        }
        return BeanProvider.getContextualReference(name, true, type);
    }

    private boolean notValid() {
        try {
            return BeanManagerProvider.getInstance().getBeanManager() == null;
        } catch (IllegalStateException ise) {
            return true;
        }
    }

    @Override
    public <T> Map<String, T> lookupByType(final Class<T> type) {
        // not implemented
        return Collections.emptyMap();
    }
}
