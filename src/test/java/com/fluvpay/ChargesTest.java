package com.fluvpay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fluvpay.models.Charge;
import com.fluvpay.models.ChargeCreateParams;
import com.fluvpay.models.Customer;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChargesTest {

    private MockServer server;
    private FluvPay client;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        server = new MockServer();
        client = FluvPay.builder()
                .apiKey("fluv_test_chave_de_teste")
                .baseUrl(server.baseUrl())
                .maxRetries(0)
                .build();
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    @Test
    void createEnviaBodyCorretoEHeadersEParseiaCharge() throws Exception {
        server.enqueue(201, "{"
                + "\"id\":\"chg_123\",\"merchant_id\":\"mer_1\",\"amount_cents\":2500,"
                + "\"currency\":\"BRL\",\"status\":\"pending\",\"payment_method\":\"pix\","
                + "\"pix_copy_paste\":\"00020126...\",\"fee_processor_cents\":10,"
                + "\"fee_platform_cents\":13,\"net_amount_cents\":2477,\"metadata\":{\"pedido_id\":\"1042\"},"
                + "\"created_at\":\"2026-06-01T10:00:00Z\",\"updated_at\":\"2026-06-01T10:00:00Z\"}");

        Charge charge = client.charges().create(new ChargeCreateParams()
                .amountCents(2500)
                .description("Pedido #1042")
                .customer(new Customer().setName("Cliente Exemplo").setEmail("cliente@exemplo.com"))
                .passFeeToPayer(true));

        assertEquals("chg_123", charge.getId());
        assertEquals("pending", charge.getStatus());
        assertEquals("pix", charge.getPaymentMethod());
        assertEquals(2500, charge.getAmountCents());
        assertEquals("00020126...", charge.getPixCopyPaste());

        MockServer.RecordedRequest req = server.lastRequest();
        assertEquals("POST", req.method);
        assertEquals("/charges/", req.path);
        assertEquals("Bearer fluv_test_chave_de_teste", req.authorization);
        assertEquals("application/json", req.contentType);
        assertEquals("fluvpay-java/" + FluvPay.VERSION, req.userAgent);
        assertNotNull(req.idempotencyKey);
        assertFalse(req.idempotencyKey.isEmpty());

        JsonNode body = mapper.readTree(req.body);
        assertEquals(2500, body.get("amount_cents").asInt());
        assertEquals("Pedido #1042", body.get("description").asText());
        assertTrue(body.get("pass_fee_to_payer").asBoolean());
        assertEquals("Cliente Exemplo", body.get("customer").get("name").asText());
        // O contrato proíbe currency/method no corpo: o SDK não deve enviar.
        assertNull(body.get("currency"));
        assertNull(body.get("method"));
        assertNull(body.get("payment_method"));
    }

    @Test
    void createRespeitaIdempotencyKeyInformada() throws Exception {
        server.enqueue(201, "{\"id\":\"chg_1\",\"merchant_id\":\"m\",\"amount_cents\":100,"
                + "\"currency\":\"BRL\",\"status\":\"pending\",\"payment_method\":\"pix\","
                + "\"fee_processor_cents\":0,\"fee_platform_cents\":0,\"metadata\":{},"
                + "\"created_at\":\"2026-06-01T10:00:00Z\",\"updated_at\":\"2026-06-01T10:00:00Z\"}");

        client.charges().create(new ChargeCreateParams().amountCents(100), "pedido-1042-tentativa-1");

        assertEquals("pedido-1042-tentativa-1", server.lastRequest().idempotencyKey);
    }

    @Test
    void retrieveBuscaPorIdEParseia() {
        server.enqueue(200, "{\"id\":\"chg_abc\",\"merchant_id\":\"m\",\"amount_cents\":500,"
                + "\"currency\":\"BRL\",\"status\":\"paid\",\"payment_method\":\"pix\","
                + "\"fee_processor_cents\":2,\"fee_platform_cents\":3,\"metadata\":{},"
                + "\"created_at\":\"2026-06-01T10:00:00Z\",\"updated_at\":\"2026-06-01T10:05:00Z\"}");

        Charge charge = client.charges().retrieve("chg_abc");

        assertEquals("chg_abc", charge.getId());
        assertEquals("paid", charge.getStatus());
        assertEquals("GET", server.lastRequest().method);
        assertEquals("/charges/chg_abc", server.lastRequest().path);
    }

    @Test
    void isTestKeyDetectaPrefixo() {
        assertTrue(client.isTestKey());
        FluvPay live = FluvPay.builder().apiKey("fluv_live_abc").build();
        assertFalse(live.isTestKey());
    }
}
