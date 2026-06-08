package com.fluvpay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fluvpay.Webhooks.VerifyParams;
import com.fluvpay.models.WebhookEvent;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * Vetor determinístico de assinatura de webhook. O mesmo trio
 * (secret/timestamp/body) e o mesmo hex valem em todos os SDKs oficiais, pois o
 * algoritmo é idêntico: HMAC_SHA256(secret, timestamp + "." + rawBody) em hex.
 */
class WebhooksTest {

    private static final String SECRET = "whsec_test_3f9a2b7c1d4e5f60718293a4b5c6d7e8";
    private static final String TIMESTAMP = "1718000000";
    private static final String BODY =
            "{\"id\":\"evt_01HZX3K8M9N2P4Q6R8S0T2V4W6\",\"type\":\"charge.paid\","
            + "\"data\":{\"id\":\"chg_01HZX3K8M9N2P4Q6R8S0T2V4W6\",\"status\":\"paid\",\"amount_cents\":2500}}";
    private static final String HEX =
            "ba4516ed60e9e89b613b9ce78746f8e65294baf5f2785ef6aee8f05eb15a0f99";

    private final Webhooks webhooks = FluvPay.webhooks();

    @Test
    void aceitaAssinaturaCorretaERetornaEvento() {
        WebhookEvent event = webhooks.verifySignature(VerifyParams.builder()
                .payload(BODY)
                .signatureHeader("v1=" + HEX)
                .timestamp(TIMESTAMP)
                .secret(SECRET)
                .build());

        assertEquals("charge.paid", event.getType());
        assertEquals("evt_01HZX3K8M9N2P4Q6R8S0T2V4W6", event.getId());
        assertEquals(2500, ((Number) event.getData().get("amount_cents")).intValue());
    }

    @Test
    void aceitaCorpoCruComoBytes() {
        WebhookEvent event = webhooks.verifySignature(VerifyParams.builder()
                .payload(BODY.getBytes(StandardCharsets.UTF_8))
                .signatureHeader("v1=" + HEX)
                .timestamp(TIMESTAMP)
                .secret(SECRET)
                .build());

        assertEquals("charge.paid", event.getType());
    }

    @Test
    void sobrecargaDeStringFunciona() {
        WebhookEvent event = webhooks.verifySignature(BODY, "v1=" + HEX, TIMESTAMP, SECRET);
        assertEquals("evt_01HZX3K8M9N2P4Q6R8S0T2V4W6", event.getId());
    }

    @Test
    void lancaQuandoAssinaturaFoiAdulterada() {
        String tampered = "ba4516ed60e9e89b613b9ce78746f8e65294baf5f2785ef6aee8f05eb15a0f00";
        assertThrows(FluvPaySignatureVerificationError.class, () ->
                webhooks.verifySignature(VerifyParams.builder()
                        .payload(BODY)
                        .signatureHeader("v1=" + tampered)
                        .timestamp(TIMESTAMP)
                        .secret(SECRET)
                        .build()));
    }

    @Test
    void lancaQuandoCorpoFoiAlteradoComMesmaAssinatura() {
        assertThrows(FluvPaySignatureVerificationError.class, () ->
                webhooks.verifySignature(VerifyParams.builder()
                        .payload(BODY.replace("2500", "9999"))
                        .signatureHeader("v1=" + HEX)
                        .timestamp(TIMESTAMP)
                        .secret(SECRET)
                        .build()));
    }

    @Test
    void lancaQuandoSegredoEstaErrado() {
        assertThrows(FluvPaySignatureVerificationError.class, () ->
                webhooks.verifySignature(VerifyParams.builder()
                        .payload(BODY)
                        .signatureHeader("v1=" + HEX)
                        .timestamp(TIMESTAMP)
                        .secret("whsec_outro_segredo")
                        .build()));
    }

    @Test
    void rejeitaFormatoDeHeaderInvalido() {
        assertThrows(FluvPaySignatureVerificationError.class, () ->
                webhooks.verifySignature(VerifyParams.builder()
                        .payload(BODY)
                        .signatureHeader(HEX)
                        .timestamp(TIMESTAMP)
                        .secret(SECRET)
                        .build()));
    }

    @Test
    void respeitaToleranciaQuandoTimestampEhVelhoDemais() {
        assertThrows(FluvPaySignatureVerificationError.class, () ->
                webhooks.verifySignature(VerifyParams.builder()
                        .payload(BODY)
                        .signatureHeader("v1=" + HEX)
                        .timestamp(TIMESTAMP)
                        .secret(SECRET)
                        .toleranceSeconds(300)
                        .build()));
    }
}
