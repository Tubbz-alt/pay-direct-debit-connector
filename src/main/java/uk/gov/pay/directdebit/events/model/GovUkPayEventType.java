package uk.gov.pay.directdebit.events.model;

public enum GovUkPayEventType {
    MANDATE_CREATED,
    MANDATE_TOKEN_EXCHANGED,
    MANDATE_SUBMITTED,
    MANDATE_EXPIRED_BY_SYSTEM,
    MANDATE_CANCELLED_BY_USER,
    MANDATE_CANCELLED_BY_USER_NOT_ELIGIBLE,
    PAYMENT_SUBMITTED
}