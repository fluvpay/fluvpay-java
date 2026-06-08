package com.fluvpay.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Map;

/**
 * Evento de webhook entregue pela FluvPay, já validado pela assinatura.
 *
 * <p>O campo {@code type} assume um entre: {@code charge.created},
 * {@code charge.paid}, {@code charge.expired}, {@code charge.cancelled},
 * {@code charge.refunded}, {@code payout.created}, {@code payout.completed} e
 * {@code payout.failed}. O conteúdo de {@code data} varia conforme o tipo.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class WebhookEvent {

    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("data")
    private Map<String, Object> data;

    @JsonProperty("created_at")
    private String createdAt;

    public String getId() {
        return id;
    }

    /** Tipo do evento (ex: charge.paid, payout.completed). */
    public String getType() {
        return type;
    }

    /** Payload do evento (mapa livre). Nunca nulo; vazio quando ausente. */
    public Map<String, Object> getData() {
        return data == null ? Collections.emptyMap() : data;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "WebhookEvent{id=" + id + ", type=" + type + "}";
    }
}
