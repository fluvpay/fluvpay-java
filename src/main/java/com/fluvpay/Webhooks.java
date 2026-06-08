package com.fluvpay;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fluvpay.models.WebhookEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Verificação de assinatura de webhooks da FluvPay.
 *
 * <p>A FluvPay envia ao endpoint do lojista os headers
 * {@code X-FluvPay-Event}, {@code X-FluvPay-Timestamp},
 * {@code X-FluvPay-Delivery-Id} e {@code X-FluvPay-Signature}. A assinatura tem
 * o formato {@code v1=<hex>}, onde
 * {@code <hex> = HMAC_SHA256(secret, timestamp + "." + rawBody)} em hexadecimal.
 *
 * <p>A verificação usa o corpo CRU da requisição, exatamente como recebido.
 * Nunca re-serialize o JSON: isso muda os bytes e invalida a assinatura.
 */
public final class Webhooks {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    public Webhooks() {
    }

    /**
     * Verifica a assinatura e devolve o evento parseado.
     *
     * <p>Algoritmo (exato): {@code HMAC_SHA256(secret, timestamp + "." + payload)},
     * em hexadecimal. O header vem como {@code v1=<hex>}. A comparação é em tempo
     * constante. Lança {@link FluvPaySignatureVerificationError} se algo não
     * conferir.
     */
    public WebhookEvent verifySignature(VerifyParams params) {
        if (params.signatureHeader == null || params.signatureHeader.isEmpty()) {
            throw new FluvPaySignatureVerificationError("Assinatura de webhook ausente.");
        }
        if (params.timestamp == null || params.timestamp.isEmpty()) {
            throw new FluvPaySignatureVerificationError("Timestamp de webhook ausente.");
        }
        if (params.secret == null || params.secret.isEmpty()) {
            throw new FluvPaySignatureVerificationError("Segredo do webhook ausente.");
        }
        if (params.payload == null) {
            throw new FluvPaySignatureVerificationError("Corpo do webhook ausente.");
        }

        String provided = extractV1(params.signatureHeader);
        if (provided == null) {
            throw new FluvPaySignatureVerificationError(
                    "Formato de assinatura inválido: esperado 'v1=<hex>'.");
        }

        byte[] rawBody = params.payload;
        byte[] prefix = (params.timestamp + ".").getBytes(StandardCharsets.UTF_8);
        byte[] signedPayload = new byte[prefix.length + rawBody.length];
        System.arraycopy(prefix, 0, signedPayload, 0, prefix.length);
        System.arraycopy(rawBody, 0, signedPayload, prefix.length, rawBody.length);

        String expected = hmacSha256Hex(params.secret, signedPayload);

        if (!constantTimeEquals(provided, expected)) {
            throw new FluvPaySignatureVerificationError("Assinatura de webhook inválida.");
        }

        if (params.toleranceSeconds != null) {
            try {
                long ts = Long.parseLong(params.timestamp.trim());
                long now = Instant.now().getEpochSecond();
                if (Math.abs(now - ts) > params.toleranceSeconds) {
                    throw new FluvPaySignatureVerificationError(
                            "Timestamp do webhook fora da tolerância permitida.");
                }
            } catch (NumberFormatException ignored) {
                // Timestamp não numérico: não aplica a tolerância.
            }
        }

        return parseEvent(rawBody);
    }

    /** Sobrecarga conveniente recebendo o corpo como {@code String}. */
    public WebhookEvent verifySignature(String payload, String signatureHeader,
                                        String timestamp, String secret) {
        return verifySignature(VerifyParams.builder()
                .payload(payload)
                .signatureHeader(signatureHeader)
                .timestamp(timestamp)
                .secret(secret)
                .build());
    }

    private static String extractV1(String header) {
        for (String part : header.split(",")) {
            String trimmed = part.trim();
            int eq = trimmed.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String scheme = trimmed.substring(0, eq).trim();
            String value = trimmed.substring(eq + 1).trim();
            if ("v1".equals(scheme) && !value.isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private static String hmacSha256Hex(String secret, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(data);
            return toHex(digest);
        } catch (Exception ex) {
            throw new FluvPaySignatureVerificationError(
                    "Falha ao calcular a assinatura do webhook.", ex);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    /** Comparação de duas strings hexadecimais em tempo constante. */
    private static boolean constantTimeEquals(String a, String b) {
        byte[] ba = a.getBytes(StandardCharsets.UTF_8);
        byte[] bb = b.getBytes(StandardCharsets.UTF_8);
        if (ba.length != bb.length || ba.length == 0) {
            return false;
        }
        return MessageDigest.isEqual(ba, bb);
    }

    private static WebhookEvent parseEvent(byte[] rawBody) {
        try {
            return MAPPER.readValue(rawBody, WebhookEvent.class);
        } catch (IOException ex) {
            throw new FluvPaySignatureVerificationError(
                    "Corpo do webhook não é um JSON válido.", ex);
        }
    }

    /** Parâmetros para {@link Webhooks#verifySignature(VerifyParams)}. */
    public static final class VerifyParams {
        private final byte[] payload;
        private final String signatureHeader;
        private final String timestamp;
        private final String secret;
        private final Long toleranceSeconds;

        private VerifyParams(Builder b) {
            this.payload = b.payload;
            this.signatureHeader = b.signatureHeader;
            this.timestamp = b.timestamp;
            this.secret = b.secret;
            this.toleranceSeconds = b.toleranceSeconds;
        }

        public static Builder builder() {
            return new Builder();
        }

        /** Builder dos parâmetros de verificação. */
        public static final class Builder {
            private byte[] payload;
            private String signatureHeader;
            private String timestamp;
            private String secret;
            private Long toleranceSeconds;

            private Builder() {
            }

            /** Corpo CRU como bytes, exatamente como recebido. */
            public Builder payload(byte[] payload) {
                this.payload = payload;
                return this;
            }

            /** Corpo CRU como String (convertido em UTF-8). */
            public Builder payload(String payload) {
                this.payload = payload == null ? null : payload.getBytes(StandardCharsets.UTF_8);
                return this;
            }

            /** Valor do header {@code X-FluvPay-Signature} (formato {@code v1=<hex>}). */
            public Builder signatureHeader(String signatureHeader) {
                this.signatureHeader = signatureHeader;
                return this;
            }

            /** Valor do header {@code X-FluvPay-Timestamp}. */
            public Builder timestamp(String timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            /** Segredo do webhook ({@code whsec_...}) exibido na criação. */
            public Builder secret(String secret) {
                this.secret = secret;
                return this;
            }

            /**
             * Tolerância em segundos entre o timestamp e o momento atual. Se
             * informado e o timestamp for numérico, rejeita eventos velhos demais.
             */
            public Builder toleranceSeconds(long toleranceSeconds) {
                this.toleranceSeconds = toleranceSeconds;
                return this;
            }

            public VerifyParams build() {
                return new VerifyParams(this);
            }
        }
    }
}
