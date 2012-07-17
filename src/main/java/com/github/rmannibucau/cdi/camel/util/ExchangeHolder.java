package com.github.rmannibucau.cdi.camel.util;

import org.apache.camel.Exchange;

public final class ExchangeHolder {
    private static ThreadLocal<Exchange> EXCHANGE = new InheritableThreadLocal<Exchange>();

    public static void set(final Exchange exchange) {
        EXCHANGE.set(exchange);
    }

    public static void remove() {
        EXCHANGE.remove();
    }

    public static Exchange get() {
        final Exchange ex = EXCHANGE.get();
        try {
            return ex;
        } finally { // force cleanup
            if (ex == null) {
                EXCHANGE.remove();
            }
        }
    }
}
