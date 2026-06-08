package com.fluvpay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fluvpay.resources.ChargesResource;
import com.fluvpay.resources.InternalTransfersResource;
import com.fluvpay.resources.SandboxResource;
import com.fluvpay.resources.TransactionsResource;
import com.fluvpay.resources.WithdrawalsResource;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Cliente oficial da FluvPay para Java.
 *
 * <p>Cuida de autenticação, serialização, idempotência, mapeamento de erros e
 * retentativas. Os recursos ({@link #charges}, {@link #withdrawals}, ...)
 * delegam aqui. Construa com o {@link Builder}:
 *
 * <pre>{@code
 * FluvPay fluvpay = FluvPay.builder()
 *     .apiKey(System.getenv("FLUVPAY_API_KEY"))
 *     .build();
 * }</pre>
 *
 * <p>A API Key define o modo pelo prefixo: {@code fluv_live_} para produção e
 * {@code fluv_test_} para o sandbox. O SDK apenas repassa a chave.
 */
public final class FluvPay {

    /** Versão do SDK, embutida no User-Agent. */
    public static final String VERSION = "1.0.0";

    private static final String DEFAULT_BASE_URL = "https://api.fluvpay.com/api/v1";
    private static final long BASE_BACKOFF_MILLIS = 250L;

    /** Helper estático de webhooks, exposto como {@code FluvPay.webhooks()}. */
    private static final Webhooks WEBHOOKS = new Webhooks();

    private final String apiKey;
    private final String baseUrl;
    private final Duration timeout;
    private final int maxRetries;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final Sleeper sleeper;

    private final ChargesResource charges;
    private final TransactionsResource transactions;
    private final WithdrawalsResource withdrawals;
    private final InternalTransfersResource internalTransfers;
    private final SandboxResource sandbox;

    private FluvPay(Builder builder) {
        if (builder.apiKey == null || builder.apiKey.isEmpty()) {
            throw new IllegalArgumentException("FluvPay: apiKey é obrigatório.");
        }
        this.apiKey = builder.apiKey;
        this.baseUrl = stripTrailingSlashes(
                builder.baseUrl != null ? builder.baseUrl : DEFAULT_BASE_URL);
        this.timeout = builder.timeout != null ? builder.timeout : Duration.ofSeconds(30);
        this.maxRetries = Math.max(0, builder.maxRetries);
        this.mapper = builder.mapper != null ? builder.mapper : defaultMapper();
        this.sleeper = builder.sleeper != null ? builder.sleeper : Thread::sleep;
        this.httpClient = builder.httpClient != null
                ? builder.httpClient
                : HttpClient.newBuilder()
                        .connectTimeout(this.timeout)
                        .build();

        this.charges = new ChargesResource(this);
        this.transactions = new TransactionsResource(this);
        this.withdrawals = new WithdrawalsResource(this);
        this.internalTransfers = new InternalTransfersResource(this);
        this.sandbox = new SandboxResource(this);
    }

    /** Cria um builder do cliente. */
    public static Builder builder() {
        return new Builder();
    }

    /** Atalho para um cliente apenas com a API Key. */
    public static FluvPay create(String apiKey) {
        return builder().apiKey(apiKey).build();
    }

    /** Recurso de cobranças. */
    public ChargesResource charges() {
        return charges;
    }

    /** Recurso de extrato (transactions). */
    public TransactionsResource transactions() {
        return transactions;
    }

    /** Recurso de saques. */
    public WithdrawalsResource withdrawals() {
        return withdrawals;
    }

    /** Recurso de transferências internas. */
    public InternalTransfersResource internalTransfers() {
        return internalTransfers;
    }

    /** Recurso do sandbox (apenas chave fluv_test_). */
    public SandboxResource sandbox() {
        return sandbox;
    }

    /** Helper estático de verificação de webhooks. */
    public static Webhooks webhooks() {
        return WEBHOOKS;
    }

    /** Indica se a chave em uso é de sandbox ({@code fluv_test_}). */
    public boolean isTestKey() {
        return apiKey.startsWith("fluv_test_");
    }

    /** Gera uma chave de idempotência (UUIDv4). */
    public String generateIdempotencyKey() {
        return UUID.randomUUID().toString();
    }

    /** O {@link ObjectMapper} usado internamente (útil para resources). */
    public ObjectMapper mapper() {
        return mapper;
    }

    /**
     * Executa uma requisição e devolve o JSON parseado como {@link JsonNode}.
     *
     * <p>Aplica retentativas com backoff exponencial e jitter para falhas
     * transientes (429 e 5xx/conexão), respeitando o header Retry-After.
     * Retentativa só ocorre em GET e em POST que carregue Idempotency-Key.
     */
    public JsonNode request(RequestOptions options) {
        String url = buildUrl(options.path, options.query);
        String bodyJson = serializeBody(options);
        boolean retryable = isRetryable(options);

        int attempt = 0;
        while (true) {
            HttpResponse<String> response;
            try {
                response = send(url, options, bodyJson);
            } catch (HttpTimeoutException timeoutEx) {
                if (retryable && attempt < maxRetries) {
                    backoff(attempt, null);
                    attempt++;
                    continue;
                }
                throw new FluvPayConnectionError(
                        "Falha de conexão com a FluvPay: tempo limite excedido.", timeoutEx);
            } catch (IOException ioEx) {
                if (retryable && attempt < maxRetries) {
                    backoff(attempt, null);
                    attempt++;
                    continue;
                }
                throw new FluvPayConnectionError(
                        "Falha de conexão com a FluvPay: " + ioEx.getMessage(), ioEx);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new FluvPayConnectionError(
                        "Requisição interrompida antes de concluir.", interrupted);
            }

            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                return parseJson(response.body());
            }

            String retryAfterHeader = response.headers().firstValue("retry-after").orElse(null);
            JsonNode body = parseJsonQuiet(response.body());
            FluvPayError error = ErrorFactory.fromResponse(status, body, retryAfterHeader);

            boolean transientFailure = (error instanceof FluvPayRateLimitError)
                    || (error instanceof FluvPayServerError);
            if (retryable && transientFailure && attempt < maxRetries) {
                Double retryAfter = (error instanceof FluvPayRateLimitError)
                        ? ((FluvPayRateLimitError) error).getRetryAfter()
                        : null;
                backoff(attempt, retryAfter);
                attempt++;
                continue;
            }

            throw error;
        }
    }

    private HttpResponse<String> send(String url, RequestOptions options, String bodyJson)
            throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout)
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .header("User-Agent", "fluvpay-java/" + VERSION);

        if ("GET".equals(options.method)) {
            request.GET();
        } else {
            String payload = bodyJson != null ? bodyJson : "{}";
            request.header("Content-Type", "application/json");
            request.POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));
            if (options.idempotencyKey != null) {
                request.header("Idempotency-Key", options.idempotencyKey);
            }
        }

        return httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private boolean isRetryable(RequestOptions options) {
        if (maxRetries <= 0) {
            return false;
        }
        if ("GET".equals(options.method)) {
            return true;
        }
        return "POST".equals(options.method) && options.idempotencyKey != null;
    }

    private String serializeBody(RequestOptions options) {
        if (options.body == null || "GET".equals(options.method)) {
            return null;
        }
        try {
            return mapper.writeValueAsString(options.body);
        } catch (IOException ex) {
            throw new FluvPayError(
                    "Falha ao serializar o corpo da requisição: " + ex.getMessage(),
                    null, null, null, null, ex);
        }
    }

    private String buildUrl(String path, Map<String, Object> query) {
        StringBuilder url = new StringBuilder(baseUrl).append(path);
        if (query != null && !query.isEmpty()) {
            StringBuilder qs = new StringBuilder();
            for (Map.Entry<String, Object> entry : query.entrySet()) {
                Object value = entry.getValue();
                if (value == null) {
                    continue;
                }
                if (qs.length() > 0) {
                    qs.append('&');
                }
                qs.append(encode(entry.getKey())).append('=').append(encode(String.valueOf(value)));
            }
            if (qs.length() > 0) {
                url.append('?').append(qs);
            }
        }
        return url.toString();
    }

    private JsonNode parseJson(String body) {
        if (body == null || body.isEmpty()) {
            return mapper.createObjectNode();
        }
        try {
            return mapper.readTree(body);
        } catch (IOException ex) {
            throw new FluvPayError(
                    "Resposta da FluvPay não é um JSON válido.",
                    null, null, null, null, ex);
        }
    }

    private JsonNode parseJsonQuiet(String body) {
        if (body == null || body.isEmpty()) {
            return null;
        }
        try {
            return mapper.readTree(body);
        } catch (IOException ex) {
            return null;
        }
    }

    private void backoff(int attempt, Double retryAfterSeconds) {
        long delayMillis;
        if (retryAfterSeconds != null) {
            delayMillis = Math.round(retryAfterSeconds * 1000d);
        } else {
            long base = BASE_BACKOFF_MILLIS * (1L << attempt);
            double jitter = ThreadLocalRandom.current().nextDouble() * base;
            delayMillis = Math.round(base + jitter);
        }
        try {
            sleeper.sleep(delayMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new FluvPayConnectionError("Espera entre tentativas interrompida.", ex);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String stripTrailingSlashes(String url) {
        int end = url.length();
        while (end > 0 && url.charAt(end - 1) == '/') {
            end--;
        }
        return url.substring(0, end);
    }

    private static ObjectMapper defaultMapper() {
        return JsonMapper.builder().build();
    }

    /** Função de espera entre tentativas (injetável para testes determinísticos). */
    @FunctionalInterface
    public interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    /** Descrição de uma requisição interna (uso dos resources). */
    public static final class RequestOptions {
        final String method;
        final String path;
        final Map<String, Object> query;
        final Object body;
        final String idempotencyKey;

        private RequestOptions(String method, String path, Map<String, Object> query,
                               Object body, String idempotencyKey) {
            this.method = method;
            this.path = path;
            this.query = query;
            this.body = body;
            this.idempotencyKey = idempotencyKey;
        }

        /** Requisição GET sem query. */
        public static RequestOptions get(String path) {
            return new RequestOptions("GET", path, null, null, null);
        }

        /** Requisição GET com query. */
        public static RequestOptions get(String path, Map<String, Object> query) {
            return new RequestOptions("GET", path, query, null, null);
        }

        /** Requisição POST com corpo e chave de idempotência. */
        public static RequestOptions post(String path, Object body, String idempotencyKey) {
            return new RequestOptions("POST", path, null, body, idempotencyKey);
        }

        /** Requisição POST sem corpo (ex: sandbox reset). */
        public static RequestOptions post(String path) {
            return new RequestOptions("POST", path, null, null, null);
        }
    }

    /** Builder do cliente FluvPay. */
    public static final class Builder {
        private String apiKey;
        private String baseUrl;
        private Duration timeout;
        private int maxRetries = 2;
        private HttpClient httpClient;
        private ObjectMapper mapper;
        private Sleeper sleeper;

        private Builder() {
        }

        /** API Key. Prefixo {@code fluv_live_} (produção) ou {@code fluv_test_} (sandbox). Obrigatório. */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /** Base URL da API. Padrão {@code https://api.fluvpay.com/api/v1}. */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /** Tempo limite por requisição. Padrão 30 segundos. */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /** Número máximo de retentativas. Padrão 2. Use 0 para desligar. */
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /** {@link HttpClient} customizado (opcional). */
        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /** {@link ObjectMapper} customizado (opcional). */
        public Builder objectMapper(ObjectMapper mapper) {
            this.mapper = mapper;
            return this;
        }

        /** Função de espera customizada, útil para testes (opcional). */
        public Builder sleeper(Sleeper sleeper) {
            this.sleeper = sleeper;
            return this;
        }

        /** Constrói o cliente. */
        public FluvPay build() {
            return new FluvPay(this);
        }
    }

    /** Constrói um mapa de query mantendo a ordem de inserção. */
    public static Map<String, Object> query() {
        return new LinkedHashMap<>();
    }
}
