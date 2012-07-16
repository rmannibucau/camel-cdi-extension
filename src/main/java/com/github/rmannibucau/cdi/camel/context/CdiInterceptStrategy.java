package com.github.rmannibucau.cdi.camel.context;

import com.github.rmannibucau.cdi.camel.util.ExchangeHolder;
import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.DelegateAsyncProcessor;
import org.apache.camel.spi.InterceptStrategy;

public class CdiInterceptStrategy implements InterceptStrategy {
    @Override
    public Processor wrapProcessorInInterceptors(final CamelContext context, final ProcessorDefinition<?> definition, final Processor target, final Processor nextTarget) throws Exception {
        return new CdiDelegateAsyncProcessor(target);
    }

    // we can't really manage async since we don't know
    private static class CdiDelegateAsyncProcessor extends DelegateAsyncProcessor {
        public CdiDelegateAsyncProcessor(final Processor target) {
            super(target);
        }

        @Override
        public boolean process(final Exchange exchange, final AsyncCallback callback) {
            ExchangeHolder.set(exchange);
            try {
                return getProcessor().process(exchange, new AsyncCallback() {
                    @Override
                    public void done(boolean doneSync) {
                        callback.done(doneSync);
                    }
                });
            } finally { // do it synchronously since we use a threadlocal
                ExchangeHolder.remove();
            }
        }
    }
}
