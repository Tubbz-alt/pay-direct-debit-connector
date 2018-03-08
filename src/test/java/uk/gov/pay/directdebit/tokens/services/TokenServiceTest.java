package uk.gov.pay.directdebit.tokens.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.tokens.dao.TokenDao;
import uk.gov.pay.directdebit.tokens.exception.TokenNotFoundException;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(MockitoJUnitRunner.class)
public class TokenServiceTest {

    private static final String TOKEN = "token";

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    private TokenDao mockedTokenDao;
    @Mock
    private TransactionService mockedTransactionService;

    private TransactionFixture transactionFixture;
    private TokenService service;

    @Before
    public void setUp() throws Exception {
        transactionFixture = aTransactionFixture().withState(PaymentState.NEW);
        service = new TokenService(mockedTokenDao, mockedTransactionService);
        when(mockedTransactionService.findTransactionForToken(TOKEN))
                .thenReturn(Optional.of(transactionFixture.toEntity()));
    }

    @Test
    public void shouldGenerateANewToken() {
        PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture();
        Token token = service.generateNewTokenFor(paymentRequestFixture.toEntity());
        assertThat(token.getId(), is(notNullValue()));
        assertThat(token.getToken(), is(notNullValue()));
        assertThat(token.getPaymentRequestId(), is(paymentRequestFixture.getId()));
    }

    @Test
    public void shouldValidateAPaymentRequestWithAToken() {
        Transaction charge = service.validateChargeWithToken(TOKEN);
        assertThat(charge.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
    }

    @Test
    public void shouldThrowIfTokenDoesNotExist() {
        when(mockedTransactionService.findTransactionForToken("not-existing")).thenReturn(Optional.empty());
        thrown.expect(TokenNotFoundException.class);
        thrown.expectMessage("No one-time token found for payment request");
        thrown.reportMissingExceptionWithMessage("TokenNotFoundException.class expected");
        service.validateChargeWithToken("not-existing");
    }
}
