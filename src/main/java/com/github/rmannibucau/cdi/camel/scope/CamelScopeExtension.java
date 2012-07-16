package com.github.rmannibucau.cdi.camel.scope;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

public class CamelScopeExtension implements Extension {
    protected void afterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager) {
        afterBeanDiscovery.addContext(new ExchangeContext());
    }
}
