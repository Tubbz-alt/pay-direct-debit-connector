package uk.gov.pay.directdebit.mandate.resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;

import javax.ws.rs.core.Response;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;
import static uk.gov.pay.directdebit.payments.model.PaymentState.AWAITING_CONFIRMATION;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class ConfirmPaymentResourceIT {

    @DropwizardTestContext
    private TestContext testContext;

    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();
    private PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture()
            .withGatewayAccountId(gatewayAccountFixture.getId());
    private TransactionFixture transactionFixture = aTransactionFixture().withPaymentRequestId(paymentRequestFixture.getId())
            .withPaymentRequestGatewayAccountId(gatewayAccountFixture.getId())
            .withState(AWAITING_CONFIRMATION);
    @Test
    public void confirm_shouldCreateAMandateAndUpdateCharge() throws Exception {
        gatewayAccountFixture.insert(testContext.getJdbi());
        paymentRequestFixture.insert(testContext.getJdbi());
        transactionFixture.insert(testContext.getJdbi());
        String paymentRequestExternalId = paymentRequestFixture.getExternalId();
        PayerFixture.aPayerFixture().withPaymentRequestId(paymentRequestFixture.getId()).insert(testContext.getJdbi());

        String requestPath = String.format("/v1/api/accounts/%s/payment-requests/%s/confirm", gatewayAccountFixture.getId().toString(), paymentRequestExternalId);
        given().port(testContext.getPort())
                .accept(APPLICATION_JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Map<String, Object> transaction = testContext.getDatabaseTestHelper().getTransactionById(transactionFixture.getId());
        assertThat(transaction.get("state"), is("PENDING_DIRECT_DEBIT_PAYMENT"));
    }

    @Test
    public void confirm_shouldFailWhenPayerDoesNotExist() throws Exception {
        gatewayAccountFixture.insert(testContext.getJdbi());
        paymentRequestFixture.insert(testContext.getJdbi());
        transactionFixture.insert(testContext.getJdbi());

        String requestPath = String.format("/v1/api/accounts/%s/payment-requests/%s/confirm", gatewayAccountFixture.getId().toString(), paymentRequestFixture.getExternalId());
        given().port(testContext.getPort())
                .accept(APPLICATION_JSON)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.CONFLICT.getStatusCode());
    }
}