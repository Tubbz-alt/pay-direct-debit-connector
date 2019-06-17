package uk.gov.pay.directdebit.payments.resources;

import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.payments.api.CollectPaymentRequest;
import uk.gov.pay.directdebit.payments.api.CollectPaymentRequestValidator;
import uk.gov.pay.directdebit.payments.api.CollectPaymentResponse;
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.services.PaymentService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.created;

@Path("/")
public class PaymentResource {
    //has to be /charges unless we change public api
    public static final String CHARGE_API_PATH = "/v1/api/accounts/{accountId}/charges/{paymentExternalId}";

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentResource.class);
    
    private final PaymentService paymentService;
    private final MandateQueryService mandateQueryService;
    private final CollectPaymentRequestValidator collectPaymentRequestValidator = new CollectPaymentRequestValidator();

    @Inject
    public PaymentResource(PaymentService paymentService, MandateQueryService mandateQueryService) {
        this.paymentService = paymentService;
        this.mandateQueryService = mandateQueryService;
    }

    @GET
    @Path(CHARGE_API_PATH)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response getCharge(@PathParam("accountId") String accountExternalId, @PathParam("paymentExternalId") String transactionExternalId, @Context UriInfo uriInfo) {
        PaymentResponse response = paymentService.getPaymentWithExternalId(accountExternalId, transactionExternalId, uriInfo);
        return Response.ok(response).build();
    }
    
    @POST
    @Path("/v1/api/accounts/{accountId}/charges/collect")
    @Produces(APPLICATION_JSON)
    @Timed
    public Response collectPaymentFromMandate(@PathParam("accountId") GatewayAccount gatewayAccount, Map<String, String> collectPaymentRequestMap, @Context UriInfo uriInfo) {
        LOGGER.info("Received collect payment from mandate request");
        collectPaymentRequestValidator.validate(collectPaymentRequestMap);
        CollectPaymentRequest collectPaymentRequest = CollectPaymentRequest.of(collectPaymentRequestMap);
        Mandate mandate = mandateQueryService.findByExternalId(collectPaymentRequest.getMandateExternalId());
        Payment paymentToCollect = paymentService.createPayment(gatewayAccount, mandate, collectPaymentRequest);
        CollectPaymentResponse response = paymentService.collectPaymentResponseWithSelfLink(paymentToCollect, gatewayAccount.getExternalId(), uriInfo);
        return created(response.getLink("self")).entity(response).build();
    }
}