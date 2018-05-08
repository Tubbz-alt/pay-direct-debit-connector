package uk.gov.pay.directdebit.payments.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payments.api.PaymentViewResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentViewDao;
import uk.gov.pay.directdebit.payments.dao.PaymentViewSearchParams;
import uk.gov.pay.directdebit.payments.exception.RecordsNotFoundException;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.PaymentView;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentViewServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private String gatewayAccountExternalId = RandomIdGenerator.newId();
    private ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC);
    private List<PaymentView> paymentViewList = new ArrayList<>();
    @Mock
    private PaymentViewDao paymentViewDao;
    @Mock
    private GatewayAccountDao gatewayAccountDao;

    private PaymentViewService paymentViewService;
    private GatewayAccount gatewayAccount;

    @Before
    public void setUp() {
        for (int i = 0; i < 4; i++) {
            PaymentView paymentView = new PaymentView(
                    gatewayAccountExternalId,
                    RandomIdGenerator.newId(),
                    1000l + i,
                    "Pay reference" + i,
                    "This is a description" + i,
                    "http://return-service.com/" + i,
                    createdDate,
                    "John Doe" + i,
                    "doe@mail.mail",
                    PaymentState.PROCESSING_DIRECT_DEBIT_DETAILS);
            paymentViewList.add(paymentView);
        }
        paymentViewService = new PaymentViewService(paymentViewDao, gatewayAccountDao);
        gatewayAccount = new GatewayAccount(1l, gatewayAccountExternalId, null, null, null, null, null);
    }

    @Test
    public void getPaymentViewList_withGatewayAccountIdAndOffsetAndLimit_shouldPopulateResponse() {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountExternalId, 1l, 100l);
        when(gatewayAccountDao.findByExternalId(gatewayAccountExternalId)).thenReturn(Optional.of(gatewayAccount));
        when(paymentViewDao.searchPaymentView(any(PaymentViewSearchParams.class))).thenReturn(paymentViewList);

        PaymentViewResponse response = paymentViewService.getPaymentViewResponse(searchParams);
        assertThat(response.getPaymentViewResponses().get(3).getAmount(), is(1003l));
        assertThat(response.getPaymentViewResponses().get(1).getName(), is("John Doe1"));
        assertThat(response.getPaymentViewResponses().get(0).getState(), is((PaymentState.PROCESSING_DIRECT_DEBIT_DETAILS.toExternal())));
        assertThat(response.getPaymentViewResponses().get(2).getCreatedDate(), is(createdDate.toString()));
    }

    @Test
    public void shouldThrowNoRecordsFoundException_whenPaymentViewListIsEmpty() {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountExternalId, 10l, 100l);
        thrown.expect(RecordsNotFoundException.class);
        thrown.expectMessage("Found no records with page size 10 and display_size 100");
        when(gatewayAccountDao.findByExternalId(gatewayAccountExternalId)).thenReturn(Optional.of(gatewayAccount));
        when(paymentViewDao.searchPaymentView(any(PaymentViewSearchParams.class))).thenReturn(new ArrayList<>());
        paymentViewService.getPaymentViewResponse(searchParams);
    }

    @Test
    public void shouldThrowNoRecordsFoundException_whenGatewayNotExists() {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountExternalId, 10l, 100l);
        thrown.expect(GatewayAccountNotFoundException.class);
        thrown.expectMessage("Unknown gateway account: " + gatewayAccountExternalId);
        when(gatewayAccountDao.findByExternalId(gatewayAccountExternalId)).thenReturn(Optional.empty());
        paymentViewService.getPaymentViewResponse(searchParams);
    }
}