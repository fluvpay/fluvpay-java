package com.fluvpay.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Detalhes de uma transferência interna já registrada. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class InternalTransfer {

    @JsonProperty("id")
    private String id;

    @JsonProperty("from_merchant_id")
    private String fromMerchantId;

    @JsonProperty("to_merchant_id")
    private String toMerchantId;

    @JsonProperty("to_merchant_name")
    private String toMerchantName;

    @JsonProperty("amount_cents")
    private Integer amountCents;

    @JsonProperty("description")
    private String description;

    @JsonProperty("status")
    private String status;

    @JsonProperty("created_at")
    private String createdAt;

    public String getId() {
        return id;
    }

    public String getFromMerchantId() {
        return fromMerchantId;
    }

    public String getToMerchantId() {
        return toMerchantId;
    }

    public String getToMerchantName() {
        return toMerchantName;
    }

    public Integer getAmountCents() {
        return amountCents;
    }

    public String getDescription() {
        return description;
    }

    /** Um entre: completed, failed, reversed. */
    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "InternalTransfer{id=" + id + ", status=" + status + ", amountCents=" + amountCents + "}";
    }
}
