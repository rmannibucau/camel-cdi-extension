package com.github.rmannibucau.cdi.camel.scope;

import com.github.rmannibucau.cdi.camel.bean.BeanWithInjection;
import com.github.rmannibucau.cdi.camel.context.CdiCamelContext;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.webbeans.config.WebBeansContext;
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

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

@RunWith(Arquillian.class)
public class CamelScopeInjectionTest {
    private static CamelContext context;
    private static ProducerTemplate template;

    @Deployment
    public static JavaArchive jar() {
        return ShrinkWrap.create(JavaArchive.class)
                .addPackage(CamelScopeExtension.class.getPackage())
                .addAsServiceProvider(Extension.class, CamelScopeExtension.class)
                .addPackage(BeanWithInjection.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @BeforeClass
    public static void startContext() throws Exception {
        context = new CdiCamelContext();
        context.addRoutes(new CamelScopeWithBridgeBuilder());
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
    public void checkInjection() {
        template.sendBody("direct:simple", "body");
        assertEquals(1, CamelScopeWithBridgeBuilder.count);
    }

    public static class CamelScopeWithBridgeBuilder extends RouteBuilder {
        private static int count = 0;

        @Override
        public void configure() throws Exception {
            from("direct:simple")
                .routeId("scope-injection-1")
                .process(new Processor() {
                    @Override
                    public void process(final Exchange exchange) throws Exception {
                        final BeanWithInjection bean = instance();
                        assertNotNull(bean);
                        bean.enclose(exchange.getIn().getBody(String.class));
                    }
                });
            from("direct:bridge")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            count++;
                            assertEquals("body", exchange.getIn().getBody(String.class));
                        }
                    });
        }

        private BeanWithInjection instance() {
            final BeanManager bm = WebBeansContext.currentInstance().getBeanManagerImpl();
            final Set<Bean<?>> beans = bm.getBeans(BeanWithInjection.class);
            final Bean<?> bean = bm.resolve(beans);
            return  (BeanWithInjection) bm.getReference(bean, BeanWithInjection.class, bm.createCreationalContext(null));
        }
    }
}
