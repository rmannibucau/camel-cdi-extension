package com.github.rmannibucau.cdi.camel.event;

import org.apache.camel.management.event.ExchangeCreatedEvent;

import javax.enterprise.event.Observes;

public class EventObserver {
    public static int newExchanges = 0;

    public void newExchange(@Observes final ExchangeCreatedEvent event) {
        newExchanges++;
    }
}
