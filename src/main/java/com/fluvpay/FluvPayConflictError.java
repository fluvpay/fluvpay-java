package com.fluvpay;

import java.util.List;

/** 409: conflito (inclui IDEMPOTENCY_CONFLICT). */
public class FluvPayConflictError extends FluvPayError {

    private static final long serialVersionUID = 1L;

    public FluvPayConflictError(
            String message, String code, Integer statusCode, List<ErrorDetail> details, String traceId) {
        super(message, code, statusCode, details, traceId, null);
    }
}
