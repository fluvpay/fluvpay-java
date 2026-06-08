package com.fluvpay.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Resultado de apagar os dados do sandbox. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SandboxReset {

    @JsonProperty("reset")
    private Boolean reset;

    @JsonProperty("deleted_charges")
    private Integer deletedCharges;

    @JsonProperty("merchant_id")
    private String merchantId;

    public Boolean getReset() {
        return reset;
    }

    public Integer getDeletedCharges() {
        return deletedCharges;
    }

    public String getMerchantId() {
        return merchantId;
    }

    @Override
    public String toString() {
        return "SandboxReset{reset=" + reset + ", deletedCharges=" + deletedCharges + "}";
    }
}
