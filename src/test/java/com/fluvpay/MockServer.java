package com.fluvpay;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Servidor HTTP em memória (JDK puro, sem rede externa) para os testes unitários.
 *
 * <p>Enfileira respostas planejadas e captura cada requisição recebida (método,
 * path, headers e corpo), permitindo asserções determinísticas sem rede de
 * verdade. Escuta em {@code 127.0.0.1} numa porta efêmera (loopback).
 */
final class MockServer implements AutoCloseable {

    /** Uma resposta planejada a ser devolvida em ordem. */
    static final class PlannedResponse {
        final int status;
        final String body;
        final String retryAfter;

        PlannedResponse(int status, String body, String retryAfter) {
            this.status = status;
            this.body = body;
            this.retryAfter = retryAfter;
        }
    }

    /** Uma requisição capturada pelo servidor. */
    static final class RecordedRequest {
        final String method;
        final String path;
        final String authorization;
        final String idempotencyKey;
        final String contentType;
        final String userAgent;
        final String body;

        RecordedRequest(String method, String path, String authorization, String idempotencyKey,
                        String contentType, String userAgent, String body) {
            this.method = method;
            this.path = path;
            this.authorization = authorization;
            this.idempotencyKey = idempotencyKey;
            this.contentType = contentType;
            this.userAgent = userAgent;
            this.body = body;
        }
    }

    private final HttpServer server;
    private final BlockingQueue<PlannedResponse> responses = new ArrayBlockingQueue<>(64);
    private final List<RecordedRequest> requests = new ArrayList<>();

    MockServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", new RootHandler());
        server.setExecutor(null);
        server.start();
    }

    String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    void enqueue(int status, String body) {
        responses.add(new PlannedResponse(status, body, null));
    }

    void enqueue(int status, String body, String retryAfter) {
        responses.add(new PlannedResponse(status, body, retryAfter));
    }

    synchronized List<RecordedRequest> recorded() {
        return new ArrayList<>(requests);
    }

    synchronized RecordedRequest lastRequest() {
        if (requests.isEmpty()) {
            return null;
        }
        return requests.get(requests.size() - 1);
    }

    synchronized int requestCount() {
        return requests.size();
    }

    @Override
    public void close() {
        server.stop(0);
    }

    private final class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = readBody(exchange.getRequestBody());
            RecordedRequest recorded = new RecordedRequest(
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().toString(),
                    exchange.getRequestHeaders().getFirst("Authorization"),
                    exchange.getRequestHeaders().getFirst("Idempotency-Key"),
                    exchange.getRequestHeaders().getFirst("Content-Type"),
                    exchange.getRequestHeaders().getFirst("User-Agent"),
                    body);
            synchronized (MockServer.this) {
                requests.add(recorded);
            }

            PlannedResponse planned = responses.poll();
            if (planned == null) {
                planned = new PlannedResponse(500, "{\"error\":{\"code\":\"NO_PLANNED_RESPONSE\","
                        + "\"message\":\"sem resposta planejada\"}}", null);
            }

            byte[] payload = planned.body != null
                    ? planned.body.getBytes(StandardCharsets.UTF_8)
                    : new byte[0];
            if (planned.retryAfter != null) {
                exchange.getResponseHeaders().add("Retry-After", planned.retryAfter);
            }
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(planned.status, payload.length == 0 ? -1 : payload.length);
            if (payload.length > 0) {
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(payload);
                }
            } else {
                exchange.close();
            }
        }
    }

    private static String readBody(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}
