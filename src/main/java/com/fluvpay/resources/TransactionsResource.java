package com.fluvpay.resources;

import com.fluvpay.FluvPay;
import com.fluvpay.models.Transaction;
import com.fluvpay.models.TransactionsPage;
import java.util.Map;

/**
 * Recurso do extrato financeiro consolidado: listar e recuperar lançamentos.
 *
 * <p>Não suportado em sandbox: chaves {@code fluv_test_} recebem 403.
 */
public final class TransactionsResource {

    private final FluvPay client;

    public TransactionsResource(FluvPay client) {
        this.client = client;
    }

    /** Lista lançamentos (paginação por page/per_page). */
    public TransactionsPage list() {
        return list(null);
    }

    /**
     * Lista lançamentos com filtros opcionais. Chaves aceitas em {@code params}:
     * {@code page}, {@code per_page} e {@code sort}.
     */
    public TransactionsPage list(Map<String, Object> params) {
        return Decoder.decode(
                client,
                client.request(FluvPay.RequestOptions.get("/transactions/", params)),
                TransactionsPage.class);
    }

    /** Recupera um lançamento por identificador. */
    public Transaction retrieve(String txId) {
        return Decoder.decode(
                client,
                client.request(FluvPay.RequestOptions.get("/transactions/" + Decoder.segment(txId))),
                Transaction.class);
    }
}
