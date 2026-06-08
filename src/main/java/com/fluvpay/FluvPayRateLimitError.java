package com.fluvpay;

import java.util.List;

/**
 * 429: limite de taxa excedido. O campo {@code retryAfter} traz o valor do header
 * {@code Retry-After} (em segundos), quando presente.
 */
public class FluvPayRateLimitError extends FluvPayError {

    private static final long serialVersionUID = 1L;

    private final Double retryAfter;

    public FluvPayRateLimitError(
            String message,
            String code,
            Integer statusCode,
            List<ErrorDetail> details,
            String traceId,
            Double retryAfter) {
        super(message, code, statusCode, details, traceId, null);
        this.retryAfter = retryAfter;
    }

    /** Segundos a esperar antes de tentar de novo (header Retry-After), ou nulo. */
    public Double getRetryAfter() {
        return retryAfter;
    }
}
