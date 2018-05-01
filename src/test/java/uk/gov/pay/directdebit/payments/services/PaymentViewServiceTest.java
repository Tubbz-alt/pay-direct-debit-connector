package uk.gov.pay.directdebit.payments.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.payments.api.PaymentViewResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentViewDao;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.PaymentView;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(MockitoJUnitRunner.class)
public class PaymentViewServiceTest {
    private String gatewayAccountExternalId = RandomIdGenerator.newId();
    private ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC);
    private List<PaymentView> paymentViewList = new ArrayList<>();

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Mock
    private PaymentViewDao paymentViewDao;
    
    private PaymentViewService paymentViewService;
    
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
        paymentViewService = new PaymentViewService(paymentViewDao);
        when(paymentViewDao.searchPaymentView(gatewayAccountExternalId, 0l, 10l)).thenReturn(paymentViewList);
    }

    @Test
    public void getPaymentViewList_withGatewayAccountIdAndOffsetAndLimit_shouldPopulateResponse() {
        List<PaymentViewResponse> responseList = paymentViewService.getPaymentViewResponse(gatewayAccountExternalId, 0l, 10l);
        assertThat(responseList.get(3).getAmount(), is(1003l));
        assertThat(responseList.get(1).getName(), is("John Doe1"));
        assertThat(responseList.get(0).getState(), is((PaymentState.PROCESSING_DIRECT_DEBIT_DETAILS.toExternal())));
        assertThat(responseList.get(2).getCreatedDate(), is(createdDate.toString()));
    }
}
