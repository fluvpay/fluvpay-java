package com.fluvpay;

import java.util.List;

/** 400 / 422: dados inválidos ou estado impeditivo (ex: INSUFFICIENT_BALANCE). */
public class FluvPayValidationError extends FluvPayError {

    private static final long serialVersionUID = 1L;

    public FluvPayValidationError(
            String message, String code, Integer statusCode, List<ErrorDetail> details, String traceId) {
        super(message, code, statusCode, details, traceId, null);
    }
}
