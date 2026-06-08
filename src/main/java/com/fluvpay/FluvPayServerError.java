package com.fluvpay;

import java.util.List;

/** 5xx: erro interno da FluvPay. */
public class FluvPayServerError extends FluvPayError {

    private static final long serialVersionUID = 1L;

    public FluvPayServerError(
            String message, String code, Integer statusCode, List<ErrorDetail> details, String traceId) {
        super(message, code, statusCode, details, traceId, null);
    }
}
