package com.fluvpay;

import java.util.List;

/** 404: recurso não encontrado. */
public class FluvPayNotFoundError extends FluvPayError {

    private static final long serialVersionUID = 1L;

    public FluvPayNotFoundError(
            String message, String code, Integer statusCode, List<ErrorDetail> details, String traceId) {
        super(message, code, statusCode, details, traceId, null);
    }
}
