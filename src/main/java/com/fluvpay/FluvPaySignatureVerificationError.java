package com.fluvpay;

/** A assinatura do webhook não confere (ou o timestamp está fora da tolerância). */
public class FluvPaySignatureVerificationError extends FluvPayError {

    private static final long serialVersionUID = 1L;

    public FluvPaySignatureVerificationError(String message) {
        super(message);
    }

    public FluvPaySignatureVerificationError(String message, Throwable cause) {
        super(message, null, null, null, null, cause);
    }
}
