package com.fluvpay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fluvpay.models.Charge;
import com.fluvpay.models.ChargeCreateParams;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RetryTest {

    private MockServer server;
    private final List<Long> sleeps = new ArrayList<>();

    @BeforeEach
    void setUp() throws IOException {
        server = new MockServer();
        sleeps.clear();
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    private FluvPay clientWithRetries(int maxRetries) {
        return FluvPay.builder()
                .apiKey("fluv_test_chave")
                .baseUrl(server.baseUrl())
                .maxRetries(maxRetries)
                .sleeper(sleeps::add)
                .build();
    }

    private static final String CHARGE_OK = "{\"id\":\"chg_ok\",\"merchant_id\":\"m\",\"amount_cents\":2500,"
            + "\"currency\":\"BRL\",\"status\":\"pending\",\"payment_method\":\"pix\","
            + "\"fee_processor_cents\":10,\"fee_platform_cents\":13,\"metadata\":{},"
            + "\"created_at\":\"2026-06-01T10:00:00Z\",\"updated_at\":\"2026-06-01T10:00:00Z\"}";

    @Test
    void retentaApos429EObtemSucesso() {
        server.enqueue(429, "{\"error\":{\"code\":\"RATE_LIMITED\",\"message\":\"Limite\"}}", "0");
        server.enqueue(200, "{\"id\":\"chg_ok\",\"merchant_id\":\"m\",\"amount_cents\":2500,"
                + "\"currency\":\"BRL\",\"status\":\"paid\",\"payment_method\":\"pix\","
                + "\"fee_processor_cents\":10,\"fee_platform_cents\":13,\"metadata\":{},"
                + "\"created_at\":\"2026-06-01T10:00:00Z\",\"updated_at\":\"2026-06-01T10:00:00Z\"}");

        FluvPay client = clientWithRetries(2);
        Charge charge = client.charges().retrieve("chg_ok");

        assertEquals("chg_ok", charge.getId());
        assertEquals(2, server.requestCount());
        assertEquals(1, sleeps.size());
        // Retry-After "0" deve ser respeitado: espera de 0 ms.
        assertEquals(0L, sleeps.get(0));
    }

    @Test
    void retentaApos500EObtemSucesso() {
        server.enqueue(500, "{\"error\":{\"code\":\"INTERNAL\",\"message\":\"Erro\"}}");
        server.enqueue(200, CHARGE_OK);

        FluvPay client = clientWithRetries(2);
        Charge charge = client.charges().retrieve("chg_ok");

        assertEquals("chg_ok", charge.getId());
        assertEquals(2, server.requestCount());
        assertEquals(1, sleeps.size());
    }

    @Test
    void postComIdempotencyKeyEhRetentavel() {
        server.enqueue(503, "{\"error\":{\"code\":\"UNAVAILABLE\",\"message\":\"Indisponível\"}}");
        server.enqueue(201, CHARGE_OK);

        FluvPay client = clientWithRetries(2);
        Charge charge = client.charges().create(new ChargeCreateParams().amountCents(2500));

        assertEquals("chg_ok", charge.getId());
        assertEquals(2, server.requestCount());
    }

    @Test
    void naoRetentaQuandoMaxRetriesZero() {
        server.enqueue(500, "{\"error\":{\"code\":\"INTERNAL\",\"message\":\"Erro\"}}");

        FluvPay client = clientWithRetries(0);
        assertThrows(FluvPayServerError.class, () -> client.charges().retrieve("x"));

        assertEquals(1, server.requestCount());
        assertTrue(sleeps.isEmpty());
    }

    @Test
    void erroNaoTransienteNaoEhRetentado() {
        server.enqueue(422, "{\"error\":{\"code\":\"VALIDATION_ERROR\",\"message\":\"Inválido\"}}");

        FluvPay client = clientWithRetries(2);
        assertThrows(FluvPayValidationError.class,
                () -> client.charges().create(new ChargeCreateParams().amountCents(1)));

        assertEquals(1, server.requestCount());
    }

    @Test
    void esgotaTentativasEPropagaOErro() {
        server.enqueue(500, "{\"error\":{\"code\":\"INTERNAL\",\"message\":\"Erro\"}}");
        server.enqueue(500, "{\"error\":{\"code\":\"INTERNAL\",\"message\":\"Erro\"}}");
        server.enqueue(500, "{\"error\":{\"code\":\"INTERNAL\",\"message\":\"Erro\"}}");

        FluvPay client = clientWithRetries(2);
        assertThrows(FluvPayServerError.class, () -> client.charges().retrieve("x"));

        // 1 tentativa inicial + 2 retentativas = 3 requisições.
        assertEquals(3, server.requestCount());
        assertEquals(2, sleeps.size());
    }
}
