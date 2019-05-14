package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.payments.services.GoCardlessEventService;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessActionHandler;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessMandateHandler;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessPaymentHandler;

import javax.inject.Inject;
import java.util.List;

public class WebhookGoCardlessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookGoCardlessService.class);

    private final GoCardlessEventService goCardlessService;
    private final GoCardlessPaymentHandler goCardlessPaymentHandler;
    private final GoCardlessMandateHandler goCardlessMandateHandler;

    @Inject
    public WebhookGoCardlessService(GoCardlessEventService goCardlessService,
                                    GoCardlessPaymentHandler goCardlessPaymentHandler,
                                    GoCardlessMandateHandler goCardlessMandateHandler) {
        this.goCardlessService = goCardlessService;
        this.goCardlessPaymentHandler = goCardlessPaymentHandler;
        this.goCardlessMandateHandler = goCardlessMandateHandler;
    }

    public void handleEvents(List<GoCardlessEvent> events) {
        events.forEach(goCardlessService::storeEvent);
//        events.forEach(event -> {
//            GoCardlessActionHandler handler = getHandlerFor(event.getResourceType());
//            LOGGER.info("About to handle event of type: {}, action: {}, resource id: {}",
//                    event.getResourceType(),
//                    event.getAction(),
//                    event.getResourceId());
//            handler.handle(event);
//        });
    }
}
