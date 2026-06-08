package com.fluvpay.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Detalhes de uma cobrança PIX, conforme devolvido por criar e recuperar.
 *
 * <p>O campo {@code status} assume um entre {@code pending}, {@code paid},
 * {@code expired}, {@code cancelled} e {@code refunded}. As datas vêm como
 * strings ISO 8601 (date-time).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Charge {

    @JsonProperty("id")
    private String id;

    @JsonProperty("merchant_id")
    private String merchantId;

    @JsonProperty("amount_cents")
    private Integer amountCents;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("description")
    private String description;

    @JsonProperty("customer")
    private Customer customer;

    @JsonProperty("status")
    private String status;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("expires_at")
    private String expiresAt;

    @JsonProperty("paid_at")
    private String paidAt;

    @JsonProperty("pix_qr_code")
    private String pixQrCode;

    @JsonProperty("pix_copy_paste")
    private String pixCopyPaste;

    @JsonProperty("fee_processor_cents")
    private Integer feeProcessorCents;

    @JsonProperty("fee_platform_cents")
    private Integer feePlatformCents;

    @JsonProperty("net_amount_cents")
    private Integer netAmountCents;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    public String getId() {
        return id;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public Integer getAmountCents() {
        return amountCents;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public Customer getCustomer() {
        return customer;
    }

    /** Um entre: pending, paid, expired, cancelled, refunded. */
    public String getStatus() {
        return status;
    }

    /** Método de pagamento (sempre "pix"). */
    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public String getPaidAt() {
        return paidAt;
    }

    /** Imagem do QR Code em base64. */
    public String getPixQrCode() {
        return pixQrCode;
    }

    /** Código copia-e-cola PIX. */
    public String getPixCopyPaste() {
        return pixCopyPaste;
    }

    public Integer getFeeProcessorCents() {
        return feeProcessorCents;
    }

    public Integer getFeePlatformCents() {
        return feePlatformCents;
    }

    public Integer getNetAmountCents() {
        return netAmountCents;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Charge{id=" + id + ", status=" + status + ", amountCents=" + amountCents + "}";
    }
}
