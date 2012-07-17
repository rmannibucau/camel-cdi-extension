package com.github.rmannibucau.cdi.camel.registry;

import com.github.rmannibucau.cdi.camel.util.BeanManagerHelper;
import org.apache.camel.spi.Registry;
import org.apache.deltaspike.core.api.provider.BeanProvider;

import javax.enterprise.inject.spi.BeanManager;
import java.util.Collections;
import java.util.Map;

public class CdiRegistry implements Registry {
    @Override
    public Object lookup(final String name) {
        final BeanManager bm = BeanManagerHelper.get();
        if (bm != null) {
            return BeanProvider.getContextualReference(name);
        }
        return null;
    }

    @Override
    public <T> T lookup(final String name, final Class<T> type) {
        final BeanManager bm = BeanManagerHelper.get();
        if (bm != null) {
            return BeanProvider.getContextualReference(name, true, type);
        }
        return null;
    }

    @Override
    public <T> Map<String, T> lookupByType(final Class<T> type) {
        // not implemented
        return Collections.emptyMap();
    }
}
