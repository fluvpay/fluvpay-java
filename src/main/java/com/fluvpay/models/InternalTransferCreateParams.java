package com.fluvpay.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parâmetros para iniciar uma transferência interna (FluvPay para FluvPay).
 *
 * <p>Exige exatamente um entre {@code recipientEmail} e
 * {@code recipientMerchantId}. {@code amountCents} tem cap de R$ 100.000,00.
 * {@code description} é opcional (até 140 caracteres).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class InternalTransferCreateParams {

    @JsonProperty("amount_cents")
    private Integer amountCents;

    @JsonProperty("recipient_email")
    private String recipientEmail;

    @JsonProperty("recipient_merchant_id")
    private String recipientMerchantId;

    @JsonProperty("description")
    private String description;

    public InternalTransferCreateParams() {
    }

    /** Valor em centavos (mín R$ 1,00, máx R$ 100.000,00). Obrigatório. */
    public InternalTransferCreateParams amountCents(int amountCents) {
        this.amountCents = amountCents;
        return this;
    }

    /** E-mail do destinatário. Use este OU o merchant id, não ambos. */
    public InternalTransferCreateParams recipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
        return this;
    }

    /** Identificador ULID do merchant destinatário (26 caracteres). Use este OU o e-mail. */
    public InternalTransferCreateParams recipientMerchantId(String recipientMerchantId) {
        this.recipientMerchantId = recipientMerchantId;
        return this;
    }

    /** Descrição livre (até 140 caracteres). */
    public InternalTransferCreateParams description(String description) {
        this.description = description;
        return this;
    }

    public Integer getAmountCents() {
        return amountCents;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getRecipientMerchantId() {
        return recipientMerchantId;
    }

    public String getDescription() {
        return description;
    }
}
