package com.github.rmannibucau.cdi.camel.scope;

import com.github.rmannibucau.cdi.camel.bean.Bean1;
import com.github.rmannibucau.cdi.camel.registry.CdiRegistry;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
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
public class CamelScopeExtensionTest {
    private static CamelContext context;
    private static ProducerTemplate template;

    @Deployment
    public static JavaArchive jar() {
        return ShrinkWrap.create(JavaArchive.class)
                .addPackage(CamelScopeExtension.class.getPackage())
                .addAsServiceProvider(Extension.class, CamelScopeExtension.class)
                .addPackage(Bean1.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @BeforeClass
    public static void startContext() throws Exception {
        context = new DefaultCamelContext(new CdiRegistry());
        context.addRoutes(new CamelScopeBuilder());
        context.getManagementStrategy().addEventNotifier(new ExchangeScope());
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
    public void checkScope() {
        template.sendBody("direct:scope", "body");
        template.sendBody("direct:scope", "body");
        assertEquals(2, CamelScopeBuilder.messages);
    }

    public static class CamelScopeBuilder extends RouteBuilder {
        public static int messages = 0;

        private Bean1 firstBean = null;

        @Override
        public void configure() throws Exception {
            from("direct:scope")
                .process(new Processor() {
                    @Override
                    public void process(final Exchange exchange) throws Exception { // count mesasge + check new bean is created
                        messages++;

                        final Bean1 bean = instance();
                        if (firstBean != null) {
                            assertNotSame(firstBean, bean);
                        }
                        firstBean = bean;

                        assertNotNull(firstBean);
                        // exchange.getIn().setBody(firstBean.enclose(exchange.getIn().getBody(String.class)));
                    }
                })
                .process(new Processor() {
                    @Override
                    public void process(final Exchange exchange) throws Exception { // check 1 instance / message
                        final Bean1 second = instance();
                        assertNotNull(second);
                        assertSame(firstBean, second);
                    }
                })
                .bean(Bean1.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception { // check bean result "small business"
                        assertEquals("[body]", exchange.getIn().getBody(String.class));
                    }
                });
        }

        private Bean1 instance() {
            // easy way to get the OWB beanmanager, shouldnt be used in real app but fine for this test
            final BeanManager bm = WebBeansContext.currentInstance().getBeanManagerImpl();
            final Set<Bean<?>> beans = bm.getBeans(Bean1.class);
            final Bean<?> bean = bm.resolve(beans);
            return  (Bean1) bm.getReference(bean, Bean1.class, bm.createCreationalContext(null));
        }
    }
}
