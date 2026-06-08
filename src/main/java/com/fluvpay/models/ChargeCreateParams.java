package com.fluvpay.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Parâmetros para criar uma cobrança PIX.
 *
 * <p>Aceita apenas os campos do contrato. Não envie {@code currency} nem
 * {@code method}: a moeda e o método (PIX) são implícitos, e a API rejeita
 * campos extras com erro de validação.
 *
 * <p>Use o builder fluente, começando por {@code amountCents} (obrigatório, em
 * centavos, entre 100 e 100000).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ChargeCreateParams {

    @JsonProperty("amount_cents")
    private Integer amountCents;

    @JsonProperty("description")
    private String description;

    @JsonProperty("customer")
    private Customer customer;

    @JsonProperty("expires_in_seconds")
    private Integer expiresInSeconds;

    @JsonProperty("affiliate_code")
    private String affiliateCode;

    @JsonProperty("split_rule_id")
    private String splitRuleId;

    @JsonProperty("pass_fee_to_payer")
    private Boolean passFeeToPayer;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    public ChargeCreateParams() {
    }

    /** Valor em centavos (mín R$ 1,00, máx R$ 1.000,00). Obrigatório. */
    public ChargeCreateParams amountCents(int amountCents) {
        this.amountCents = amountCents;
        return this;
    }

    /** Descrição livre (até 500 caracteres). */
    public ChargeCreateParams description(String description) {
        this.description = description;
        return this;
    }

    /** Dados do pagador. */
    public ChargeCreateParams customer(Customer customer) {
        this.customer = customer;
        return this;
    }

    /** Tempo de expiração em segundos (60 a 604800). Usa o padrão do processador se omitido. */
    public ChargeCreateParams expiresInSeconds(int expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
        return this;
    }

    /** Código de afiliado opcional (4 a 24 caracteres). */
    public ChargeCreateParams affiliateCode(String affiliateCode) {
        this.affiliateCode = affiliateCode;
        return this;
    }

    /** Identificador de uma regra de split do merchant (20 a 32 caracteres). */
    public ChargeCreateParams splitRuleId(String splitRuleId) {
        this.splitRuleId = splitRuleId;
        return this;
    }

    /** Repassar a taxa ao pagador (soma no QR/PIX). Padrão ligado. */
    public ChargeCreateParams passFeeToPayer(boolean passFeeToPayer) {
        this.passFeeToPayer = passFeeToPayer;
        return this;
    }

    /** Metadados livres (objeto arbitrário). */
    public ChargeCreateParams metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    public Integer getAmountCents() {
        return amountCents;
    }

    public String getDescription() {
        return description;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Integer getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public String getAffiliateCode() {
        return affiliateCode;
    }

    public String getSplitRuleId() {
        return splitRuleId;
    }

    public Boolean getPassFeeToPayer() {
        return passFeeToPayer;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
