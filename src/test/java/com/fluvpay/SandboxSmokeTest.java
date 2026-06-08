package com.fluvpay;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fluvpay.models.Charge;
import com.fluvpay.models.ChargeCreateParams;
import com.fluvpay.models.ChargesPage;
import com.fluvpay.models.SandboxReset;
import com.fluvpay.models.SandboxScenarios;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Smoke no sandbox, gated pela env {@code FLUVPAY_TEST_KEY} (prefixo
 * {@code fluv_test_}). É ignorado quando a variável não está presente, então
 * não faz rede nas execuções unitárias padrão.
 *
 * <p>Saques e transferências internas são live-only e não rodam aqui.
 */
@EnabledIfEnvironmentVariable(named = "FLUVPAY_TEST_KEY", matches = "fluv_test_.+")
class SandboxSmokeTest {

    private FluvPay client() {
        FluvPay.Builder builder = FluvPay.builder().apiKey(System.getenv("FLUVPAY_TEST_KEY"));
        String baseUrl = System.getenv("FLUVPAY_BASE_URL");
        if (baseUrl != null && !baseUrl.isEmpty()) {
            builder.baseUrl(baseUrl);
        }
        return builder.build();
    }

    @Test
    void criaRecuperaListaEReseta() {
        FluvPay fluvpay = client();
        assertTrue(fluvpay.isTestKey(), "A chave de smoke precisa ser fluv_test_.");

        SandboxScenarios scenarios = fluvpay.sandbox().scenarios();
        assertNotNull(scenarios.getInfo());

        Charge charge = fluvpay.charges().create(new ChargeCreateParams()
                .amountCents(2500)
                .description("Smoke test do SDK Java"));
        assertNotNull(charge.getId());

        Charge fetched = fluvpay.charges().retrieve(charge.getId());
        assertNotNull(fetched.getId());

        ChargesPage page = fluvpay.charges().list();
        assertNotNull(page.getData());

        SandboxReset reset = fluvpay.sandbox().reset();
        assertNotNull(reset);
    }
}
