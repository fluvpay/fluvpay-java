package com.fluvpay;

import java.util.List;

/** 401: autenticação obrigatória ou chave inválida. */
public class FluvPayAuthenticationError extends FluvPayError {

    private static final long serialVersionUID = 1L;

    public FluvPayAuthenticationError(
            String message, String code, Integer statusCode, List<ErrorDetail> details, String traceId) {
        super(message, code, statusCode, details, traceId, null);
    }
}
