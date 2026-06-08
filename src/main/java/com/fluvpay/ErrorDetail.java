package com.fluvpay;

/**
 * Um item de {@code error.details} (validação campo a campo) no envelope de erro
 * da FluvPay.
 */
public final class ErrorDetail {

    private final String field;
    private final String message;
    private final String type;

    public ErrorDetail(String field, String message, String type) {
        this.field = field;
        this.message = message;
        this.type = type;
    }

    /** Nome do campo que falhou (pode ser nulo). */
    public String getField() {
        return field;
    }

    /** Mensagem legível do erro de validação. */
    public String getMessage() {
        return message;
    }

    /** Tipo do erro de validação (pode ser nulo). */
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ErrorDetail{field=" + field + ", message=" + message + ", type=" + type + "}";
    }
}
