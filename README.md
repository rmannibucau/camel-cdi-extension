# Goal of the project

Integrate Camel and CDI.

# Features
## Camel Context

Simply annotate your route builders with @CamelContextConfig and specify the context name
and automatically the context will be created and stopped. The context will be considered
as an Application Scoped bean.

Note: builders can be CDI beans.

Then the context can be injected using CamelContextId annotation.

Here a little sample of a builder and camel context injection:

    @CamelContextConfig(contextName = "my-context")
    public class Builder1 extends RouteBuilder {
        @Override
        public void configure() throws Exception {
            from("timer://b1?fixedRate=true&period=60000").to("log:builder1");
        }
    }

    public class ContextManager {
        @Inject
        @CamelContextId("my-context")
        private CamelContext context;
    }

This context come with next features automatically activated.

Note: can be used manually using CdiCamelContext instead of DefaultCamelContext:

    CamelContext context = new CdiCamelContext();

## Camel event to CDI events

Camel proposes some events out of the box. In CDI apps it is fine to get them in a CDI way (observers).

It needs to add com.github.rmannibucau.cdi.camel.event.CamelEventProducer ad notifier:

    context.getManagementStrategy().addEventNotifier(new CamelEventProducer());

But it is automatically done with the previous extension.

Then simply write CDI observer on Camel events:

    public void newExchange(@Observes final ExchangeCreatedEvent event) {
        // something clever
    }

## CDI beans as processor

CdiCamelContext provides a CdiRegistry to let you use CDI bean instead of not managed instance.

To use it directly simply provide the com.github.rmannibucau.cdi.camel.registry.CdiRegistry registry
to your camel context or use CdiCamelContext.

## Exchange Scope

Sometimes it is useful to get a single bean for a whole exchange. It is usually done storing
the bean in exchange properties.

Here a proposal is done to create a scope "exchange". Simply annotate your CDI bean @ExchangeScoped
and it will be available for the whole exchange life.

Note: currently the implementation uses a ThreadLocal so take care of asynchronous processor, prefer to delegate
to another route (new exchange) for asynch processing.

Note: these beans can get some camel specific injections line @EndpointInject

Here a little sample:

    @ExchangeScoped
    public class BeanWithInjection {
        @EndpointInject(uri = "direct:bridge")
        private ProducerTemplate tpl;

        public void enclose(final String body) {
            tpl.sendBody(body);
        }
    }

Then simply use this bean in your camel routes.
