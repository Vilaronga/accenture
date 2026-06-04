package br.unit.residencia.accenture.Config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

/**
 * Converte exceções de domínio em respostas HTTP com código e mensagem claros,
 * evitando que erros previsíveis retornem 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ApiError(int status, String mensagem) {
    }

    @ExceptionHandler({IllegalArgumentException.class, NoSuchElementException.class})
    public ResponseEntity<ApiError> tratarRequisicaoInvalida(RuntimeException ex) {
        HttpStatus status = ex.getMessage() != null && ex.getMessage().toLowerCase().contains("não encontrad")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(new ApiError(status.value(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> tratarEstadoInvalido(IllegalStateException ex) {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(new ApiError(status.value(), ex.getMessage()));
    }
}
