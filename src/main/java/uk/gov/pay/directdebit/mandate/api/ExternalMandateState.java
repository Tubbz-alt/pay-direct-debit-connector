package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ExternalMandateState {
    EXTERNAL_CREATED("created", false),
    EXTERNAL_STARTED("started", false),
    EXTERNAL_PENDING("pending", false),
    EXTERNAL_SUBMITTED("submitted", false),
    EXTERNAL_ACTIVE("active", true),
    EXTERNAL_INACTIVE("inactive", true),
    EXTERNAL_CANCELLED("cancelled", true);

    private final String value;
    private final boolean finished;
    private final String details = "example details";
    
    ExternalMandateState(String value, boolean finished) {
        this.value = value;
        this.finished = finished;
    }

    public String getDetails() {
        return details;
    }

    @JsonProperty("status")
    public String getState() {
        return value;
    }

    public boolean isFinished() {
        return finished;
    }
}
