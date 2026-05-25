package ma.atos.billing.invoice.billing_invoice.exception;

import org.springframework.http.HttpStatus;

public class FunctionalException extends RuntimeException {

    private final HttpStatus status;

    public FunctionalException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
