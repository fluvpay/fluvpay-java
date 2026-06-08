package com.fluvpay;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Constrói a exceção tipada a partir do status HTTP e do envelope de erro
 * {@code {"error": {code, message, details, trace_id}}}.
 */
final class ErrorFactory {

    private ErrorFactory() {
    }

    static FluvPayError fromResponse(int statusCode, JsonNode body, String retryAfterHeader) {
        String code = null;
        String message = null;
        String traceId = null;
        List<ErrorDetail> details = new ArrayList<>();

        JsonNode error = body != null ? body.get("error") : null;
        if (error != null && error.isObject()) {
            code = textOrNull(error, "code");
            message = textOrNull(error, "message");
            traceId = textOrNull(error, "trace_id");
            JsonNode rawDetails = error.get("details");
            if (rawDetails != null && rawDetails.isArray()) {
                for (JsonNode d : rawDetails) {
                    if (d != null && d.isObject()) {
                        details.add(new ErrorDetail(
                                textOrNull(d, "field"),
                                textOrNull(d, "message"),
                                textOrNull(d, "type")));
                    }
                }
            }
        }

        if (message == null || message.isEmpty()) {
            message = "Erro HTTP " + statusCode;
        }

        switch (statusCode) {
            case 400:
            case 422:
                return new FluvPayValidationError(message, code, statusCode, details, traceId);
            case 401:
                return new FluvPayAuthenticationError(message, code, statusCode, details, traceId);
            case 403:
                return new FluvPayPermissionError(message, code, statusCode, details, traceId);
            case 404:
                return new FluvPayNotFoundError(message, code, statusCode, details, traceId);
            case 409:
                return new FluvPayConflictError(message, code, statusCode, details, traceId);
            case 429:
                return new FluvPayRateLimitError(
                        message, code, statusCode, details, traceId, parseRetryAfter(retryAfterHeader));
            default:
                if (statusCode >= 500) {
                    return new FluvPayServerError(message, code, statusCode, details, traceId);
                }
                return new FluvPayError(message, code, statusCode, details, traceId, null);
        }
    }

    /**
     * Lê o header {@code Retry-After}, que pode vir como número de segundos ou
     * como data HTTP. Devolve segundos (nunca negativo) ou nulo.
     */
    static Double parseRetryAfter(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            double seconds = Double.parseDouble(trimmed);
            return Math.max(0d, seconds);
        } catch (NumberFormatException ignored) {
            // Tenta interpretar como data HTTP (RFC 1123).
        }
        try {
            ZonedDateTime when = ZonedDateTime.parse(trimmed, DateTimeFormatter.RFC_1123_DATE_TIME);
            double seconds = Duration.between(Instant.now(), when.toInstant()).toMillis() / 1000d;
            return Math.max(0d, seconds);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }
}
