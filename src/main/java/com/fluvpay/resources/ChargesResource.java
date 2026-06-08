package com.fluvpay.resources;

import com.fluvpay.FluvPay;
import com.fluvpay.models.Charge;
import com.fluvpay.models.ChargeCreateParams;
import com.fluvpay.models.ChargesPage;
import java.util.Map;

/**
 * Recurso de cobranças PIX: criar, recuperar e listar.
 */
public final class ChargesResource {

    private final FluvPay client;

    public ChargesResource(FluvPay client) {
        this.client = client;
    }

    /**
     * Cria uma cobrança PIX. A {@code Idempotency-Key} é gerada automaticamente
     * (UUIDv4).
     */
    public Charge create(ChargeCreateParams params) {
        return create(params, client.generateIdempotencyKey());
    }

    /**
     * Cria uma cobrança PIX com a {@code Idempotency-Key} informada. Reenviar a
     * mesma chave devolve a resposta original.
     */
    public Charge create(ChargeCreateParams params, String idempotencyKey) {
        String key = idempotencyKey != null ? idempotencyKey : client.generateIdempotencyKey();
        return Decoder.decode(
                client,
                client.request(FluvPay.RequestOptions.post("/charges/", params, key)),
                Charge.class);
    }

    /** Recupera uma cobrança por identificador. */
    public Charge retrieve(String chargeId) {
        return Decoder.decode(
                client,
                client.request(FluvPay.RequestOptions.get("/charges/" + Decoder.segment(chargeId))),
                Charge.class);
    }

    /** Lista cobranças (paginação por page/per_page). */
    public ChargesPage list() {
        return list(null);
    }

    /**
     * Lista cobranças com filtros opcionais. Chaves aceitas em {@code params}:
     * {@code status}, {@code page}, {@code per_page} e {@code sort}.
     */
    public ChargesPage list(Map<String, Object> params) {
        return Decoder.decode(
                client,
                client.request(FluvPay.RequestOptions.get("/charges/", params)),
                ChargesPage.class);
    }
}
