package com.fluvpay;

import java.util.Collections;
import java.util.List;

/**
 * Base de todos os erros do SDK da FluvPay.
 *
 * <p>Carrega o código canônico do erro, a mensagem em português, os detalhes de
 * validação, o {@code traceId} para correlacionar nos logs e o status HTTP
 * quando houver. As subclasses representam cada faixa de status: validação,
 * autenticação, permissão, não encontrado, conflito, limite de taxa, erro de
 * servidor e falha de conexão.
 */
public class FluvPayError extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String code;
    private final Integer statusCode;
    private final List<ErrorDetail> details;
    private final String traceId;

    public FluvPayError(String message) {
        this(message, null, null, null, null, null);
    }

    public FluvPayError(
            String message,
            String code,
            Integer statusCode,
            List<ErrorDetail> details,
            String traceId,
            Throwable cause) {
        super(message, cause);
        this.code = code;
        this.statusCode = statusCode;
        this.details = details == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(details);
        this.traceId = traceId;
    }

    /** Código canônico do erro (ex: VALIDATION_ERROR, NOT_FOUND, RATE_LIMITED). */
    public String getCode() {
        return code;
    }

    /** Status HTTP da resposta, ou nulo em falhas de conexão. */
    public Integer getStatusCode() {
        return statusCode;
    }

    /** Lista de detalhes de validação (nunca nula; vazia quando ausente). */
    public List<ErrorDetail> getDetails() {
        return details;
    }

    /** Identificador da requisição para correlacionar nos logs (pode ser nulo). */
    public String getTraceId() {
        return traceId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{code=" + code
                + ", statusCode=" + statusCode
                + ", message=" + getMessage()
                + ", traceId=" + traceId + "}";
    }
}
