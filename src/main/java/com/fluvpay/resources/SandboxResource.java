package com.fluvpay.resources;

import com.fluvpay.FluvPay;
import com.fluvpay.models.SandboxReset;
import com.fluvpay.models.SandboxScenarios;

/**
 * Utilitários do sandbox, disponíveis apenas com chave {@code fluv_test_}.
 */
public final class SandboxResource {

    private final FluvPay client;

    public SandboxResource(FluvPay client) {
        this.client = client;
    }

    /** Apaga todos os dados do sandbox da conta. */
    public SandboxReset reset() {
        return Decoder.decode(
                client,
                client.request(FluvPay.RequestOptions.post("/test/reset")),
                SandboxReset.class);
    }

    /** Lista os valores mágicos do sandbox e o comportamento simulado de cada um. */
    public SandboxScenarios scenarios() {
        return Decoder.decode(
                client,
                client.request(FluvPay.RequestOptions.get("/test/scenarios")),
                SandboxScenarios.class);
    }
}
