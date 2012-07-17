package com.github.rmannibucau.cdi.camel.util;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;

import javax.enterprise.inject.spi.BeanManager;

public final class BeanManagerHelper {
    private BeanManagerHelper() {
        // no-op
    }

    public static BeanManager get() {
        try {
            return BeanManagerProvider.getInstance().getBeanManager();
        } catch (IllegalStateException ise) {
            return null;
        }
    }
}
