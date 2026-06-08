package com.fluvpay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fluvpay.models.ChargeCreateParams;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ErrorsTest {

    private MockServer server;
    private FluvPay client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockServer();
        client = FluvPay.builder()
                .apiKey("fluv_test_chave")
                .baseUrl(server.baseUrl())
                .maxRetries(0)
                .build();
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    @Test
    void status422ViraValidationErrorComDetails() {
        server.enqueue(422, "{\"error\":{\"code\":\"VALIDATION_ERROR\",\"message\":\"Dados inválidos\","
                + "\"details\":[{\"field\":\"amount_cents\",\"message\":\"Input should be greater than or equal to 100\","
                + "\"type\":\"greater_than_equal\"}],\"trace_id\":\"01J123\"}}");

        FluvPayValidationError err = assertThrows(FluvPayValidationError.class,
                () -> client.charges().create(new ChargeCreateParams().amountCents(1)));

        assertEquals("VALIDATION_ERROR", err.getCode());
        assertEquals(422, err.getStatusCode());
        assertEquals("01J123", err.getTraceId());
        assertEquals(1, err.getDetails().size());
        assertEquals("amount_cents", err.getDetails().get(0).getField());
        assertEquals("greater_than_equal", err.getDetails().get(0).getType());
    }

    @Test
    void status400ViraValidationError() {
        server.enqueue(400, "{\"error\":{\"code\":\"BAD_REQUEST\",\"message\":\"Requisição inválida\"}}");
        FluvPayValidationError err = assertThrows(FluvPayValidationError.class,
                () -> client.charges().retrieve("x"));
        assertEquals(400, err.getStatusCode());
    }

    @Test
    void status401ViraAuthenticationError() {
        server.enqueue(401, "{\"error\":{\"code\":\"AUTHENTICATION_REQUIRED\",\"message\":\"Autenticação obrigatória\"}}");
        FluvPayAuthenticationError err = assertThrows(FluvPayAuthenticationError.class,
                () -> client.charges().retrieve("x"));
        assertEquals("AUTHENTICATION_REQUIRED", err.getCode());
    }

    @Test
    void status403ViraPermissionError() {
        server.enqueue(403, "{\"error\":{\"code\":\"PERMISSION_DENIED\",\"message\":\"Escopo insuficiente\"}}");
        FluvPayPermissionError err = assertThrows(FluvPayPermissionError.class,
                () -> client.charges().retrieve("x"));
        assertEquals("PERMISSION_DENIED", err.getCode());
    }

    @Test
    void status404ViraNotFoundError() {
        server.enqueue(404, "{\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"Recurso não encontrado\"}}");
        FluvPayNotFoundError err = assertThrows(FluvPayNotFoundError.class,
                () -> client.charges().retrieve("inexistente"));
        assertEquals("NOT_FOUND", err.getCode());
        assertEquals(404, err.getStatusCode());
    }

    @Test
    void status409ViraConflictError() {
        server.enqueue(409, "{\"error\":{\"code\":\"IDEMPOTENCY_CONFLICT\","
                + "\"message\":\"Idempotency-Key reutilizada com payload diferente\"}}");

        FluvPayConflictError err = assertThrows(FluvPayConflictError.class,
                () -> client.charges().create(new ChargeCreateParams().amountCents(100), "chave-reusada"));

        assertEquals("IDEMPOTENCY_CONFLICT", err.getCode());
        assertEquals(409, err.getStatusCode());
    }

    @Test
    void status429ViraRateLimitErrorComRetryAfter() {
        server.enqueue(429, "{\"error\":{\"code\":\"RATE_LIMITED\",\"message\":\"Limite excedido\"}}", "7");

        FluvPayRateLimitError err = assertThrows(FluvPayRateLimitError.class,
                () -> client.charges().retrieve("x"));

        assertEquals("RATE_LIMITED", err.getCode());
        assertEquals(429, err.getStatusCode());
        assertEquals(7d, err.getRetryAfter());
    }

    @Test
    void status500ViraServerError() {
        server.enqueue(500, "{\"error\":{\"code\":\"INTERNAL\",\"message\":\"Erro interno\"}}");
        FluvPayServerError err = assertThrows(FluvPayServerError.class,
                () -> client.charges().retrieve("x"));
        assertEquals(500, err.getStatusCode());
    }

    @Test
    void falhaDeConexaoViraConnectionError() throws IOException {
        // Fecha o servidor antes de chamar: a conexão deve falhar e virar FluvPayConnectionError.
        String dead = server.baseUrl();
        server.close();
        FluvPay offline = FluvPay.builder().apiKey("fluv_test_x").baseUrl(dead).maxRetries(0).build();

        FluvPayConnectionError err = assertThrows(FluvPayConnectionError.class,
                () -> offline.charges().retrieve("x"));
        assertTrue(err.getMessage().startsWith("Falha de conexão"));
    }
}
