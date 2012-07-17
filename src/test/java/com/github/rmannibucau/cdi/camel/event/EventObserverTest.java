package com.github.rmannibucau.cdi.camel.event;

import com.github.rmannibucau.cdi.camel.bean.Bean1;
import com.github.rmannibucau.cdi.camel.context.CdiCamelContext;
import com.github.rmannibucau.cdi.camel.scope.CamelScopeExtension;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class EventObserverTest {
    private static CamelContext context;
    private static ProducerTemplate template;

    @Deployment
    public static JavaArchive jar() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(EventObserver.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @BeforeClass
    public static void startContext() throws Exception {
        context = new CdiCamelContext();
        context.addRoutes(new CamelEventRouteBuilder());
        context.start();

        template = new DefaultProducerTemplate(context);
        template.start();
    }

    @AfterClass
    public static void stopContext() throws Exception {
        template.stop();
        context.stop();
    }

    @Test
    public void checkEventReceived() {
        template.sendBody("direct:event", "useless");
        assertEquals(1, CamelEventRouteBuilder.messages);
        assertEquals(1, EventObserver.newExchanges);
    }

    public static class CamelEventRouteBuilder extends RouteBuilder {
        public static int messages = 0;

        @Override
        public void configure() throws Exception {
            from("direct:event")
                    .routeId("event-route")
                    .process(new Processor() {
                        @Override
                        public void process(final Exchange exchange) throws Exception {
                            messages++;
                        }
                    });
        }
    }
}

