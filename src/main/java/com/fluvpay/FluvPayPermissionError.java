package com.fluvpay;

import java.util.List;

/** 403: escopo insuficiente, conta sem permissão ou operação indisponível no sandbox. */
public class FluvPayPermissionError extends FluvPayError {

    private static final long serialVersionUID = 1L;

    public FluvPayPermissionError(
            String message, String code, Integer statusCode, List<ErrorDetail> details, String traceId) {
        super(message, code, statusCode, details, traceId, null);
    }
}
