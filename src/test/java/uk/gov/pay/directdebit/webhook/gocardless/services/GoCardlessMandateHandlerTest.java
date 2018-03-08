package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.fixtures.GoCardlessMandateFixture;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessMandateHandlerTest {

    @Mock
    TransactionService mockedTransactionService;
    @Mock
    GoCardlessService mockedGoCardlessService;

    private GoCardlessMandateFixture goCardlessMandateFixture = GoCardlessMandateFixture.aGoCardlessMandateFixture();
    private GoCardlessMandateHandler goCardlessMandateHandler;
    @Before
    public void setUp() {
        goCardlessMandateHandler = new GoCardlessMandateHandler(mockedTransactionService, mockedGoCardlessService);
    }

    @Test
    public void shouldHandleEventsWithAValidAction() {
        GoCardlessEvent goCardlessEvent = GoCardlessEventFixture.aGoCardlessEventFixture().withAction("created").toEntity();
        PaymentRequestEvent paymentRequestEvent = PaymentRequestEventFixture.aPaymentRequestEventFixture().toEntity();
        Transaction transaction = TransactionFixture.aTransactionFixture().toEntity();
        when(mockedGoCardlessService.findMandateForEvent(goCardlessEvent)).thenReturn(goCardlessMandateFixture.toEntity());
        when(mockedTransactionService.findTransactionForMandateId(goCardlessMandateFixture.getMandateId())).thenReturn(transaction);
        when(mockedTransactionService.mandateCreatedFor(transaction)).thenReturn(paymentRequestEvent);
        goCardlessMandateHandler.handle(goCardlessEvent);

        verify(mockedTransactionService).mandateCreatedFor(transaction);
        ArgumentCaptor<GoCardlessEvent> geCaptor = forClass(GoCardlessEvent.class);

        verify(mockedGoCardlessService).storeEvent(geCaptor.capture());
        GoCardlessEvent storedGoCardlessEvent = geCaptor.getValue();
        Assert.assertThat(storedGoCardlessEvent.getPaymentRequestEventId(), is(paymentRequestEvent.getId()));
    }

    @Test
    public void shouldStoreEventsWithAnInvalidAction() {
        GoCardlessEvent goCardlessEvent = GoCardlessEventFixture.aGoCardlessEventFixture().withAction("somethingelse").toEntity();
        goCardlessMandateHandler.handle(goCardlessEvent);
        verify(mockedGoCardlessService).storeEvent(goCardlessEvent);
    }
}
