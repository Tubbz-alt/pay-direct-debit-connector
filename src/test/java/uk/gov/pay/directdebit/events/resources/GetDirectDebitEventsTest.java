package uk.gov.pay.directdebit.events.resources;

import com.jayway.jsonassert.JsonAssert;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.converters.Nullable;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.junit.DropwizardAppWithPostgresRule;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;

import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture.aDirectDebitEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_ACKNOWLEDGED_BY_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type.MANDATE;

@RunWith(JUnitParamsRunner.class)
public class GetDirectDebitEventsTest {

    @ClassRule
    public static DropwizardAppWithPostgresRule app = new DropwizardAppWithPostgresRule();
    
    private MandateFixture testMandate;
    private TransactionFixture testTransaction;
    private TestContext testContext;

    @Before
    public void setUp() {
        testContext = app.getTestContext();
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture().insert(testContext.getJdbi());
        this.testMandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
        this.testTransaction = TransactionFixture.aTransactionFixture().withMandateFixture(testMandate).insert(testContext.getJdbi());
    }
    
    @After
    public void cleanup() {
        testContext.getJdbi().withHandle(handle -> handle.execute("DELETE FROM events"));
    }

    @Test
    public void shouldReturnAllEventsForNoSearchParameters() {
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withTransactionId(testTransaction.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .withEventDate(ZonedDateTime.now())
                .insert(testContext.getJdbi());
        
        String requestPath = "/v1/events";

        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("count", is(1))
                .body("results", hasSize(1));
    }
    
    @Test
    public void shouldReturnBadRequestIfDatesAreNotValid() {
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withTransactionId(testTransaction.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .withEventDate(ZonedDateTime.now())
                .insert(testContext.getJdbi());

        String requestPath = format("/v1/events?to_date=%s", "invalid_format");

        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
    
    @Test
    public void shouldReturnAnEventWithAllSearchParameters() {
        DirectDebitEventFixture directDebitEventFixture = aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withExternalId("externalId")
                .withTransactionId(testTransaction.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .withEventDate(ZonedDateTime.now())
                .insert(testContext.getJdbi());

        String requestPath = format("/v1/events?to_date=%s&from_date=%s&display_size=100&page=1&mandate_external_id=%s&transaction_external_id=%s",
                ZonedDateTime.now().plusMinutes(5).format(DateTimeFormatter.ISO_INSTANT),
                ZonedDateTime.now().minusMinutes(5).format(DateTimeFormatter.ISO_INSTANT),
                testMandate.getExternalId(),
                testTransaction.getExternalId());
        
        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("results", hasSize(1))
                .body("count", is(1))
                .body("results[0].event_type", is(MANDATE.toString()))
                .body("results[0].event", is(PAYMENT_ACKNOWLEDGED_BY_PROVIDER.toString()))
                .body("results[0].event_date", is(directDebitEventFixture.getEventDate().format(DateTimeFormatter.ISO_INSTANT)))
                .body("results[0].external_id", is("externalId"))
                .body("results[0].mandate_external_id", is(testMandate.getExternalId().toString()))
                .body("results[0].transaction_external_id", is(testTransaction.getExternalId()))
        ;
    }

    @Test
    public void shouldReturnNoEventsForNonExistentDates() {
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withTransactionId(testTransaction.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .withEventDate(ZonedDateTime.now())
                .insert(testContext.getJdbi());

        String requestPath = format("/v1/events?to_date=%s&from_date=%s&display_size=100&page=1&mandate_external_id=%s&transaction_external_id=%s",
                ZonedDateTime.now().minusMinutes(5).format(DateTimeFormatter.ISO_INSTANT),
                ZonedDateTime.now().minusMinutes(5).format(DateTimeFormatter.ISO_INSTANT),
                testMandate.getId().toString(),
                testTransaction.getId().toString());

        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("count", is(0))
                .body("results", hasSize(0));
    }

    @Test
    public void shouldReturnAnEventForBeforeParameter() {
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withTransactionId(testTransaction.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .withEventDate(ZonedDateTime.now())
                .insert(testContext.getJdbi());

        String requestPath = format("/v1/events?to_date=%s",
                ZonedDateTime.now().plusMinutes(5).format(DateTimeFormatter.ISO_INSTANT));

        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("count", is(1))
                .body("results", hasSize(1));
    }

    @Test
    public void shouldReturnAnEventForAfterParameter() {
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withTransactionId(testTransaction.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .withEventDate(ZonedDateTime.now())
                .insert(testContext.getJdbi());

        String requestPath = format("/v1/events?from_date=%s",
                ZonedDateTime.now().minusMinutes(5).format(DateTimeFormatter.ISO_INSTANT));

        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("count", is(1))
                .body("results", hasSize(1));
    }

    @Test
    public void shouldReturnAnEventForMandateIdParameter() {
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withTransactionId(testTransaction.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .withEventDate(ZonedDateTime.now())
                .insert(testContext.getJdbi());

        String requestPath = format("/v1/events?mandate_external_id=%s", testMandate.getExternalId());

        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("count", is(1))
                .body("results", hasSize(1));
    }

    @Test
    public void shouldReturnAnEventForTransactionIdParameter() {
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withTransactionId(testTransaction.getId())
                .withEventType(MANDATE)
                .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                .withEventDate(ZonedDateTime.now())
                .insert(testContext.getJdbi());

        String requestPath = format("/v1/events?transaction_external_id=%s", testTransaction.getExternalId());

        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("count", is(1))
                .body("results", hasSize(1));
    }
    
    @Test
    public void shouldReturnTwoEventsWherePageSizeIsSetToTwo() {
        for (int i = 0; i < 4; i++) {
            aDirectDebitEventFixture()
                    .withMandateId(testMandate.getId())
                    .withTransactionId(testTransaction.getId())
                    .withEventType(MANDATE)
                    .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                    .withEventDate(ZonedDateTime.now())
                    .insert(testContext.getJdbi());
        }

        String requestPath = format("/v1/events?transaction_external_id=%s&display_size=2", testTransaction.getExternalId());

        given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("page", Is.is(1))
                .body("total", Is.is(4))
                .body("count", Is.is(2))
                .body("results", hasSize(2));
    }
    
    @Test
    public void shouldReturnThirdEventWherePageSizeIsTwoAndPageIsTwo() {
        
        for (int i = 1; i < 4; i++) {
            aDirectDebitEventFixture()
                    .withId((long) i)
                    .withExternalId("testId" + i)
                    .withMandateId(testMandate.getId())
                    .withTransactionId(testTransaction.getId())
                    .withEventType(MANDATE)
                    .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                    .withEventDate(ZonedDateTime.now())
                    .insert(testContext.getJdbi());
        }

        given().port(testContext.getPort())
                .contentType(JSON)
                .get("/v1/events?display_size=2&page=2")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("count", is(1))
                .body("total", is(3))
                .body("results", hasSize(1));
    }
    
    @Test
    @Parameters({
            "1, 2, null, 1, 5",
            "2, 3, 1, 1, 5",
            "5, null, 4, 1, 5"
    })
    public void testLinksForTenEventsWithPageSizeOfTwo(String currentPage, @Nullable String nextPage, @Nullable String prevPage, String firstPage, String lastPage) {

        for (int i = 1; i < 11; i++) {
            aDirectDebitEventFixture()
                    .withId((long) i)
                    .withMandateId(testMandate.getId())
                    .withTransactionId(testTransaction.getId())
                    .withEventType(MANDATE)
                    .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                    .withEventDate(ZonedDateTime.now())
                    .insert(testContext.getJdbi());
        }

        String requestPath = format("/v1/events?display_size=2&page=%s", currentPage);

        String body = given().port(testContext.getPort())
                .contentType(JSON)
                .get(requestPath).body().asString();

        String directDebitConnectorUrl = format("http://localhost:%s", app.getLocalPort());
        
        JsonAssert.with(body)
                .assertThat("_links.self.href", is(format("%s/v1/events?page=%s&display_size=2", directDebitConnectorUrl, currentPage)))
                .assertThat("_links.first_page.href", is(format("%s/v1/events?page=%s&display_size=2", directDebitConnectorUrl, firstPage)))
                .assertThat("_links.last_page.href", is(format("%s/v1/events?page=%s&display_size=2", directDebitConnectorUrl, lastPage)));
                
        if (nextPage == null) {
            JsonAssert.with(body).assertNotDefined("_links.next_page.href");
        } else {
            JsonAssert.with(body).assertThat("_links.next_page.href", is(format("%s/v1/events?page=%s&display_size=2", directDebitConnectorUrl, nextPage)));
        }
        
        if (prevPage == null) {
            JsonAssert.with(body).assertNotDefined("_links.prev_page.href");
        } else{
            JsonAssert.with(body).assertThat("_links.prev_page.href", is(format("%s/v1/events?page=%s&display_size=2", directDebitConnectorUrl, prevPage)));
        }
    }
    
    @Test
    public void shouldReturnFiveHundredEventsWhenPageSizeIsFiveHundredAndOne() {
        for (int i = 1; i < 503; i++) {
            aDirectDebitEventFixture()
                    .withId((long) i)
                    .withMandateId(testMandate.getId())
                    .withTransactionId(testTransaction.getId())
                    .withEventType(MANDATE)
                    .withEvent(PAYMENT_ACKNOWLEDGED_BY_PROVIDER)
                    .withEventDate(ZonedDateTime.now())
                    .insert(testContext.getJdbi());
        }

        given().port(testContext.getPort())
                .contentType(JSON)
                .get("/v1/events?display_size=501")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("results", hasSize(500));
    }
}
