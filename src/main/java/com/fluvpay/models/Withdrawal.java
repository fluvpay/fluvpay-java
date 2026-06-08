package com.fluvpay.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/** Detalhes de uma solicitação de saque PIX. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Withdrawal {

    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("amount_cents")
    private Integer amountCents;

    @JsonProperty("fee_cents")
    private Integer feeCents;

    @JsonProperty("net_cents")
    private Integer netCents;

    @JsonProperty("pix_key")
    private String pixKey;

    @JsonProperty("pix_key_type")
    private String pixKeyType;

    @JsonProperty("description")
    private String description;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("completed_at")
    private String completedAt;

    @JsonProperty("failure_reason")
    private String failureReason;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    public String getId() {
        return id;
    }

    /** Um entre: pending, processing, completed, failed. */
    public String getStatus() {
        return status;
    }

    public Integer getAmountCents() {
        return amountCents;
    }

    public Integer getFeeCents() {
        return feeCents;
    }

    public Integer getNetCents() {
        return netCents;
    }

    public String getPixKey() {
        return pixKey;
    }

    /** Um entre: cpf, cnpj, email, phone, evp. */
    public String getPixKeyType() {
        return pixKeyType;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "Withdrawal{id=" + id + ", status=" + status + ", amountCents=" + amountCents + "}";
    }
}
