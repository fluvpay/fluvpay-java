package com.fluvpay.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/** Linha do extrato financeiro consolidado (entradas e saídas). */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Transaction {

    @JsonProperty("id")
    private String id;

    @JsonProperty("merchant_id")
    private String merchantId;

    @JsonProperty("charge_id")
    private String chargeId;

    @JsonProperty("type")
    private String type;

    @JsonProperty("direction")
    private String direction;

    @JsonProperty("amount_cents")
    private Integer amountCents;

    @JsonProperty("fee_cents")
    private Integer feeCents;

    @JsonProperty("net_amount_cents")
    private Integer netAmountCents;

    @JsonProperty("status")
    private String status;

    @JsonProperty("description")
    private String description;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("counterparty_name")
    private String counterpartyName;

    @JsonProperty("counterparty_document_masked")
    private String counterpartyDocumentMasked;

    @JsonProperty("counterparty_pix_key")
    private String counterpartyPixKey;

    public String getId() {
        return id;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getChargeId() {
        return chargeId;
    }

    /** Um entre: charge, refund, payout, fee, adjustment, transfer_internal, crypto_payout. */
    public String getType() {
        return type;
    }

    /** Um entre: credit, debit. */
    public String getDirection() {
        return direction;
    }

    public Integer getAmountCents() {
        return amountCents;
    }

    public Integer getFeeCents() {
        return feeCents;
    }

    public Integer getNetAmountCents() {
        return netAmountCents;
    }

    /** Um entre: pending, completed, failed. */
    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getCounterpartyName() {
        return counterpartyName;
    }

    public String getCounterpartyDocumentMasked() {
        return counterpartyDocumentMasked;
    }

    public String getCounterpartyPixKey() {
        return counterpartyPixKey;
    }

    @Override
    public String toString() {
        return "Transaction{id=" + id + ", type=" + type + ", direction=" + direction
                + ", amountCents=" + amountCents + "}";
    }
}
