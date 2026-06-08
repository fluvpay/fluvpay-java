package com.fluvpay;

/** Falha de rede, DNS ou tempo limite antes de obter uma resposta HTTP. */
public class FluvPayConnectionError extends FluvPayError {

    private static final long serialVersionUID = 1L;

    public FluvPayConnectionError(String message, Throwable cause) {
        super(message, null, null, null, null, cause);
    }
}
