package com.github.rmannibucau.cdi.camel;

import com.github.rmannibucau.cdi.camel.builder.Builder1;
import org.apache.camel.CamelContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class CamelContextExtensionTest {
    @Inject
    @CamelContextId("context1")
    private CamelContext ctx1;

    @Inject
    @CamelContextId("context2")
    private CamelContext ctx2;

    @Deployment
    public static JavaArchive jar() {
        return ShrinkWrap.create(JavaArchive.class)
                .addPackage(CamelContextExtension.class.getPackage())
                .addAsServiceProvider(Extension.class, CamelContextExtension.class)
                .addPackage(Builder1.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @Test
    public void injectionsNotNull() {
        assertNotNull(ctx1);
        assertNotNull(ctx2);
    }

    @Test
    public void routeNumber() {
        assertEquals(2, ctx1.getRoutes().size());
        assertEquals(1, ctx2.getRoutes().size());
    }
}
