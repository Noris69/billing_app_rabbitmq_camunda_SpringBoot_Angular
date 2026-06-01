package ma.atos.billing.payment.billing_payment.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::buildValidationMessage)
                .toList();

        return ResponseEntity.badRequest().body(buildError(
                HttpStatus.BAD_REQUEST,
                "Parametres invalides",
                request.getRequestURI(),
                details
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest().body(buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI(),
                null
        ));
    }

    private ApiError buildError(HttpStatus status, String message, String path, List<String> details) {
        return new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.name(),
                message,
                path,
                details
        );
    }

    private String buildValidationMessage(FieldError error) {
        return error.getField() + " : " + error.getDefaultMessage();
    }
}
