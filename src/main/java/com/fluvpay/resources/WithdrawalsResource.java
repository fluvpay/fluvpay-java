package com.fluvpay.resources;

import com.fluvpay.FluvPay;
import com.fluvpay.models.Withdrawal;
import com.fluvpay.models.WithdrawalCreateParams;
import com.fluvpay.models.WithdrawalsPage;
import java.util.Map;

/**
 * Recurso de saques PIX: criar, listar e recuperar.
 *
 * <p>Operações live-only: chaves {@code fluv_test_} recebem 403
 * ({@code SANDBOX_NOT_SUPPORTED_FOR_WITHDRAWALS}).
 */
public final class WithdrawalsResource {

    private final FluvPay client;

    public WithdrawalsResource(FluvPay client) {
        this.client = client;
    }

    /** Cria um saque PIX. A {@code Idempotency-Key} é gerada automaticamente (UUIDv4). */
    public Withdrawal create(WithdrawalCreateParams params) {
        return create(params, client.generateIdempotencyKey());
    }

    /** Cria um saque PIX com a {@code Idempotency-Key} informada. */
    public Withdrawal create(WithdrawalCreateParams params, String idempotencyKey) {
        String key = idempotencyKey != null ? idempotencyKey : client.generateIdempotencyKey();
        return Decoder.decode(
                client,
                client.request(FluvPay.RequestOptions.post("/withdrawals/", params, key)),
                Withdrawal.class);
    }

    /** Lista saques (paginação por limit/offset). */
    public WithdrawalsPage list() {
        return list(null);
    }

    /**
     * Lista saques com filtros opcionais. Chaves aceitas em {@code params}:
     * {@code limit}, {@code offset} e {@code status}.
     */
    public WithdrawalsPage list(Map<String, Object> params) {
        return Decoder.decode(
                client,
                client.request(FluvPay.RequestOptions.get("/withdrawals/", params)),
                WithdrawalsPage.class);
    }

    /** Recupera um saque por identificador. */
    public Withdrawal retrieve(String withdrawalId) {
        return Decoder.decode(
                client,
                client.request(FluvPay.RequestOptions.get("/withdrawals/" + Decoder.segment(withdrawalId))),
                Withdrawal.class);
    }
}
