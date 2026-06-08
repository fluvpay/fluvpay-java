package com.fluvpay.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Versão enxuta de cobrança usada em listagens. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ChargeListItem {

    @JsonProperty("id")
    private String id;

    @JsonProperty("amount_cents")
    private Integer amountCents;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("status")
    private String status;

    @JsonProperty("description")
    private String description;

    @JsonProperty("paid_at")
    private String paidAt;

    @JsonProperty("created_at")
    private String createdAt;

    public String getId() {
        return id;
    }

    public Integer getAmountCents() {
        return amountCents;
    }

    public String getCurrency() {
        return currency;
    }

    /** Um entre: pending, paid, expired, cancelled, refunded. */
    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getPaidAt() {
        return paidAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "ChargeListItem{id=" + id + ", status=" + status + ", amountCents=" + amountCents + "}";
    }
}
