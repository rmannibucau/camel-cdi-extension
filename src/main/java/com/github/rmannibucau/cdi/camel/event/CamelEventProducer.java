package com.github.rmannibucau.cdi.camel.event;

import com.github.rmannibucau.cdi.camel.util.BeanManagerHelper;
import org.apache.camel.support.EventNotifierSupport;

import javax.enterprise.inject.spi.BeanManager;
import java.util.EventObject;

public final class CamelEventProducer extends EventNotifierSupport {
    @Override
    public void notify(final EventObject event) throws Exception {
        final BeanManager bm = BeanManagerHelper.get();
        if (bm != null) {
            bm.fireEvent(event);
        }
    }

    @Override
    public boolean isEnabled(final EventObject event) {
        return true;
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
