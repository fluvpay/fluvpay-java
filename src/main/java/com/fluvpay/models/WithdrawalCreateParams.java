package com.fluvpay.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parâmetros para criar um saque PIX.
 *
 * <p>{@code amountCents} é o valor bruto em centavos (cap de R$ 100.000,00).
 * {@code pixKey} e {@code pixKeyType} identificam a chave PIX de destino.
 * {@code description} é opcional (até 140 caracteres).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class WithdrawalCreateParams {

    @JsonProperty("amount_cents")
    private Integer amountCents;

    @JsonProperty("pix_key")
    private String pixKey;

    @JsonProperty("pix_key_type")
    private String pixKeyType;

    @JsonProperty("description")
    private String description;

    public WithdrawalCreateParams() {
    }

    /** Valor em centavos (mín R$ 1,00, máx R$ 100.000,00). Obrigatório. */
    public WithdrawalCreateParams amountCents(int amountCents) {
        this.amountCents = amountCents;
        return this;
    }

    /** Chave PIX de destino. Obrigatório. */
    public WithdrawalCreateParams pixKey(String pixKey) {
        this.pixKey = pixKey;
        return this;
    }

    /** Tipo da chave PIX: cpf, cnpj, email, phone ou evp. Obrigatório. */
    public WithdrawalCreateParams pixKeyType(String pixKeyType) {
        this.pixKeyType = pixKeyType;
        return this;
    }

    /** Descrição livre (até 140 caracteres). */
    public WithdrawalCreateParams description(String description) {
        this.description = description;
        return this;
    }

    public Integer getAmountCents() {
        return amountCents;
    }

    public String getPixKey() {
        return pixKey;
    }

    public String getPixKeyType() {
        return pixKeyType;
    }

    public String getDescription() {
        return description;
    }
}
