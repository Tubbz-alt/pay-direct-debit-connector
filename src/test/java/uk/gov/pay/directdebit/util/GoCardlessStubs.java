package uk.gov.pay.directdebit.util;

import uk.gov.pay.directdebit.payers.fixtures.GoCardlessCustomerFixture;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.GOCARDLESS_CREATE_CUSTOMER_BANK_ACCOUNT_REQUEST;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.GOCARDLESS_CREATE_CUSTOMER_BANK_ACCOUNT_SUCCESS_RESPONSE;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.GOCARDLESS_CREATE_CUSTOMER_REQUEST;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.GOCARDLESS_CREATE_CUSTOMER_SUCCESS_RESPONSE;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.GOCARDLESS_CREATE_MANDATE_REQUEST;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.GOCARDLESS_CREATE_MANDATE_SUCCESS_RESPONSE;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.GOCARDLESS_CREATE_PAYMENT_REQUEST;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.GOCARDLESS_CREATE_PAYMENT_SUCCESS_RESPONSE;
import static uk.gov.pay.directdebit.util.TestRequestResponsesLoader.load;

public class GoCardlessStubs {
    public static void stubCreateCustomer(String paymentRequestExternalId, PayerFixture payerFixture, String goCardlessCustomerId) {
        String customerRequestExpectedBody = load(GOCARDLESS_CREATE_CUSTOMER_REQUEST)
                .replace("{{email}}", payerFixture.getEmail())
                .replace("{{family_name}}", payerFixture.getName())
                .replace("{{given_name}}", payerFixture.getName());
        String customerResponseBody = load(GOCARDLESS_CREATE_CUSTOMER_SUCCESS_RESPONSE)
                .replace("{{email}}", payerFixture.getEmail())
                .replace("{{family_name}}", payerFixture.getName())
                .replace("{{given_name}}", payerFixture.getName())
                .replace("{{gocardless_customer_id}}", goCardlessCustomerId);

        stubCallsFor("/customers", 200, paymentRequestExternalId, customerRequestExpectedBody, customerResponseBody);
    }

    public static void stubCreateCustomerBankAccount(String paymentRequestExternalId, PayerFixture payerFixture, String goCardlessCustomerId, String goCardlessBankAccountId) {
        String customerBankAccountRequestExpectedBody = load(GOCARDLESS_CREATE_CUSTOMER_BANK_ACCOUNT_REQUEST)
                .replace("{{account_holder_name}}", payerFixture.getName())
                .replace("{{account_number}}", payerFixture.getAccountNumber())
                .replace("{{sort_code}}", payerFixture.getSortCode())
                .replace("{{gocardless_customer_id}}", goCardlessCustomerId);

        String customerBankAccountResponseBody = load(GOCARDLESS_CREATE_CUSTOMER_BANK_ACCOUNT_SUCCESS_RESPONSE)
                .replace("{{account_holder_name}}", payerFixture.getName())
                .replace("{{gocardless_customer_id}}", goCardlessCustomerId)
                .replace("{{gocardless_customer_bank_account_id}}", goCardlessBankAccountId);
        stubCallsFor("/customer_bank_accounts", 200, paymentRequestExternalId, customerBankAccountRequestExpectedBody, customerBankAccountResponseBody);
    }

    public static void stubCreateMandate(String paymentRequestExternalId, GoCardlessCustomerFixture goCardlessCustomerFixture) {
        String mandateRequestExpectedBody = load(GOCARDLESS_CREATE_MANDATE_REQUEST)
                .replace("{{customer_bank_account_id}}", goCardlessCustomerFixture.getCustomerBankAccountId());

        String mandateResponseBody = load(GOCARDLESS_CREATE_MANDATE_SUCCESS_RESPONSE)
                .replace("{{customer_bank_account_id}}", goCardlessCustomerFixture.getCustomerBankAccountId())
                .replace("{{customer_id}}", goCardlessCustomerFixture.getCustomerId())
                .replace("{{gocardless_customer_bank_account_id}}", goCardlessCustomerFixture.getCustomerBankAccountId());
        stubCallsFor("/mandates", 200, paymentRequestExternalId, mandateRequestExpectedBody, mandateResponseBody);
    }

    public static void stubCreatePayment(String paymentRequestExternalId, TransactionFixture transactionFixture) {
        String paymentRequestExpectedBody = load(GOCARDLESS_CREATE_PAYMENT_REQUEST)
                .replace("{{amount}}", String.valueOf(transactionFixture.getAmount()))
                .replace("{{reference}}", transactionFixture.getPaymentRequestDescription());

        String paymentResponseBody = load(GOCARDLESS_CREATE_PAYMENT_SUCCESS_RESPONSE)
                .replace("{{amount}}", String.valueOf(transactionFixture.getAmount()))
                .replace("{{reference}}", transactionFixture.getPaymentRequestDescription());
        stubCallsFor("/payments", 200, paymentRequestExternalId, paymentRequestExpectedBody, paymentResponseBody);
    }
    private static void stubCallsFor(String url, int statusCode, String idempotencyKey, String requestBody, String responseBody) {
        stubFor(
                post(urlPathEqualTo(url))
                        .withHeader("Authorization", equalTo("Bearer accesstoken"))
                        .withHeader("Idempotency-Key", equalTo(idempotencyKey))
                        .withRequestBody(equalToJson(requestBody))
                        .willReturn(
                                aResponse()
                                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                                        .withStatus(statusCode)
                                        .withBody(responseBody)
                        )
        );
    }
}