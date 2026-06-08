package com.fluvpay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fluvpay.models.ChargesPage;
import com.fluvpay.models.InternalTransfersPage;
import com.fluvpay.models.TransactionsPage;
import com.fluvpay.models.WithdrawalsPage;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaginationTest {

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
    void chargesListParseiaEnvelopePagePerPage() {
        server.enqueue(200, "{\"data\":[{\"id\":\"chg_1\",\"amount_cents\":2500,\"currency\":\"BRL\","
                + "\"status\":\"paid\",\"created_at\":\"2026-06-01T10:00:00Z\"}],"
                + "\"page\":1,\"per_page\":20,\"total\":1,\"has_next\":false,\"has_prev\":false}");

        Map<String, Object> params = FluvPay.query();
        params.put("status", "paid");
        params.put("page", 1);
        params.put("per_page", 20);
        params.put("sort", "-created_at");

        ChargesPage page = client.charges().list(params);

        assertEquals(1, page.getData().size());
        assertEquals("chg_1", page.getData().get(0).getId());
        assertEquals(1, page.getPage());
        assertEquals(20, page.getPerPage());
        assertEquals(1, page.getTotal());
        assertFalse(page.getHasNext());
        assertFalse(page.getHasPrev());

        String path = server.lastRequest().path;
        assertTrue(path.startsWith("/charges/?"));
        assertTrue(path.contains("status=paid"));
        assertTrue(path.contains("page=1"));
        assertTrue(path.contains("per_page=20"));
        assertTrue(path.contains("sort=-created_at"));
    }

    @Test
    void transactionsListParseiaEnvelopePagePerPage() {
        server.enqueue(200, "{\"data\":[{\"id\":\"tx_1\",\"merchant_id\":\"m\",\"type\":\"charge\","
                + "\"direction\":\"credit\",\"amount_cents\":2500,\"fee_cents\":23,\"net_amount_cents\":2477,"
                + "\"status\":\"completed\",\"metadata\":{},\"created_at\":\"2026-06-01T10:00:00Z\"}],"
                + "\"page\":2,\"per_page\":50,\"total\":120,\"has_next\":true,\"has_prev\":true}");

        Map<String, Object> params = FluvPay.query();
        params.put("page", 2);
        params.put("per_page", 50);

        TransactionsPage page = client.transactions().list(params);

        assertEquals("tx_1", page.getData().get(0).getId());
        assertEquals("credit", page.getData().get(0).getDirection());
        assertEquals(2, page.getPage());
        assertEquals(120, page.getTotal());
        assertTrue(page.getHasNext());
        assertTrue(page.getHasPrev());
    }

    @Test
    void withdrawalsListParseiaEnvelopeLimitOffset() {
        server.enqueue(200, "{\"data\":[{\"id\":\"wd_1\",\"status\":\"completed\",\"amount_cents\":5000,"
                + "\"fee_cents\":50,\"net_cents\":4950,\"pix_key\":\"chave@exemplo.com\",\"pix_key_type\":\"email\","
                + "\"created_at\":\"2026-06-01T10:00:00Z\"}],\"limit\":20,\"offset\":0,\"total\":1}");

        Map<String, Object> params = FluvPay.query();
        params.put("limit", 20);
        params.put("offset", 0);

        WithdrawalsPage page = client.withdrawals().list(params);

        assertEquals("wd_1", page.getData().get(0).getId());
        assertEquals("email", page.getData().get(0).getPixKeyType());
        assertEquals(20, page.getLimit());
        assertEquals(0, page.getOffset());
        assertEquals(1, page.getTotal());
    }

    @Test
    void internalTransfersListParseiaEnvelopeLimitOffset() {
        server.enqueue(200, "{\"data\":[{\"id\":\"itr_1\",\"from_merchant_id\":\"a\",\"to_merchant_id\":\"b\","
                + "\"amount_cents\":1000,\"status\":\"completed\",\"created_at\":\"2026-06-01T10:00:00Z\"}],"
                + "\"limit\":10,\"offset\":5,\"total\":42}");

        Map<String, Object> params = FluvPay.query();
        params.put("direction", "sent");
        params.put("limit", 10);
        params.put("offset", 5);

        InternalTransfersPage page = client.internalTransfers().list(params);

        assertEquals("itr_1", page.getData().get(0).getId());
        assertEquals("completed", page.getData().get(0).getStatus());
        assertEquals(10, page.getLimit());
        assertEquals(5, page.getOffset());
        assertEquals(42, page.getTotal());
        assertTrue(server.lastRequest().path.contains("direction=sent"));
    }
}
