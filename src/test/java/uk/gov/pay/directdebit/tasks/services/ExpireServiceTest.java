package uk.gov.pay.directdebit.tasks.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateStatesGraph;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.mandate.services.MandateStateUpdateService;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.PaymentStatesGraph;
import uk.gov.pay.directdebit.payments.services.PaymentService;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ExpireServiceTest {

    @DropwizardTestContext
    private TestContext testContext;

    private PaymentStatesGraph paymentStatesGraph = new PaymentStatesGraph();
    private MandateStatesGraph mandateStatesGraph = new MandateStatesGraph();
    @Mock
    private PaymentService paymentService;
    @Mock
    private MandateQueryService mandateQueryService;
    @Mock
    private MandateStateUpdateService mandateStateUpdateService;
    private ExpireService expireService;
    
    @Before
    public void setup() {
        expireService = new ExpireService(paymentService, mandateStatesGraph, paymentStatesGraph, mandateQueryService, mandateStateUpdateService);
    }
    
    @Test
    public void expirePayments_shouldCallTransactionServiceWithPriorStatesToPending() {
        Payment payment = PaymentFixture.aPaymentFixture().withState(PaymentState.NEW).toEntity();
        when(paymentService
                .findAllPaymentsBySetOfStatesAndCreationTime(eq(paymentStatesGraph.getPriorStates(PaymentState.PENDING)), any()))
                .thenReturn(Collections.singletonList(payment));
        
        int numberOfExpiredPayments = expireService.expirePayments();
        assertEquals(1, numberOfExpiredPayments);
    }

    @Test
    public void expireMandates_shouldCallMandateServiceWithPriorStatesToPending() {
        Mandate mandate = MandateFixture.aMandateFixture().withState(MandateState.CREATED).toEntity();
        when(mandateQueryService
                .findAllMandatesBySetOfStatesAndMaxCreationTime(eq(mandateStatesGraph.getPriorStates(MandateState.PENDING)), any()))
                .thenReturn(Collections.singletonList(mandate));
        int numberOfExpiredMandates = expireService.expireMandates();
        assertEquals(1, numberOfExpiredMandates);
    }

    @Test
    public void shouldGetCorrectPaymentsStatesPriorToPending() {
        Set<PaymentState> paymentStates = paymentStatesGraph.getPriorStates(PaymentState.PENDING);
        Set<PaymentState> expectedPaymentStates = new HashSet<>(Collections.singletonList(PaymentState.NEW));
        assertEquals(expectedPaymentStates, paymentStates);
    }

    @Test
    public void shouldGetCorrectMandateStatesPriorToPending() {
        Set<MandateState> paymentStates = mandateStatesGraph.getPriorStates(MandateState.PENDING);
        Set<MandateState> expectedPaymentStates = new HashSet<>(Arrays.asList(MandateState.CREATED, 
                                                                              MandateState.AWAITING_DIRECT_DEBIT_DETAILS,
                                                                              MandateState.SUBMITTED));
        assertEquals(expectedPaymentStates, paymentStates);
    }
}
