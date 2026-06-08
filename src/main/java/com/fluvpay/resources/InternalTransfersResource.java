package com.fluvpay.resources;

import com.fluvpay.FluvPay;
import com.fluvpay.models.InternalTransfer;
import com.fluvpay.models.InternalTransferCreateParams;
import com.fluvpay.models.InternalTransfersPage;
import java.util.Map;

/**
 * Recurso de transferências internas (FluvPay para FluvPay): criar, listar e recuperar.
 *
 * <p>Operações live-only: chaves {@code fluv_test_} recebem 403
 * ({@code SANDBOX_NOT_SUPPORTED_FOR_TRANSFERS}).
 */
public final class InternalTransfersResource {

    private final FluvPay client;

    public InternalTransfersResource(FluvPay client) {
        this.client = client;
    }

    /** Cria uma transferência interna. A {@code Idempotency-Key} é gerada automaticamente (UUIDv4). */
    public InternalTransfer create(InternalTransferCreateParams params) {
        return create(params, client.generateIdempotencyKey());
    }

    /** Cria uma transferência interna com a {@code Idempotency-Key} informada. */
    public InternalTransfer create(InternalTransferCreateParams params, String idempotencyKey) {
        String key = idempotencyKey != null ? idempotencyKey : client.generateIdempotencyKey();
        return Decoder.decode(
                client,
                client.request(FluvPay.RequestOptions.post("/internal-transfers/", params, key)),
                InternalTransfer.class);
    }

    /** Lista transferências internas (paginação por limit/offset). */
    public InternalTransfersPage list() {
        return list(null);
    }

    /**
     * Lista transferências internas com filtros opcionais. Chaves aceitas em
     * {@code params}: {@code direction} (sent ou received), {@code limit} e
     * {@code offset}.
     */
    public InternalTransfersPage list(Map<String, Object> params) {
        return Decoder.decode(
                client,
                client.request(FluvPay.RequestOptions.get("/internal-transfers/", params)),
                InternalTransfersPage.class);
    }

    /** Recupera uma transferência interna por identificador. */
    public InternalTransfer retrieve(String transferId) {
        return Decoder.decode(
                client,
                client.request(FluvPay.RequestOptions.get("/internal-transfers/" + Decoder.segment(transferId))),
                InternalTransfer.class);
    }
}
