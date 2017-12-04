package uk.gov.pay.directdebit.payments.model;

public enum PaymentState {
    NEW,
    ENTER_DIRECT_DEBIT_DETAILS,
    ENTER_DIRECT_DEBIT_DETAILS_FAILED,
    ENTER_DIRECT_DEBIT_DETAILS_ERROR,
    CONFIRM_DIRECT_DEBIT_DETAILS,
    CONFIRM_DIRECT_DEBIT_DETAILS_FAILED,
    CONFIRM_DIRECT_DEBIT_DETAILS_ERROR,
    REQUESTED,
    REQUESTED_FAILED,
    REQUESTED_ERROR,
    IN_PROGRESS,
    IN_PROGRESS_FAILED,
    PROVIDER_CANCELLED,
    EXPIRED,
    USER_CANCELLED,
    SYSTEM_CANCELLED,
    SUCCESS,
    PAID_OUT
}
