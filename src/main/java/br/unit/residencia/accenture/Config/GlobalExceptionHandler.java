package br.unit.residencia.accenture.Config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Converte exceções de domínio em respostas HTTP com código e mensagem claros,
 * evitando que erros previsíveis retornem 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ApiError(int status, String mensagem) {}

    public record ApiValidationError(int status, String mensagem, List<String> erros) {}

    // Erros de validação (@Valid) — retorna 400 com a lista de campos inválidos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiValidationError> tratarValidacao(MethodArgumentNotValidException ex) {
        List<String> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();

        return ResponseEntity.badRequest().body(
                new ApiValidationError(400, "Erro de validação nos campos enviados.", erros)
        );
    }

    // Recurso não encontrado ou argumento inválido — 404 ou 400 conforme a mensagem
    @ExceptionHandler({IllegalArgumentException.class, NoSuchElementException.class})
    public ResponseEntity<ApiError> tratarRequisicaoInvalida(RuntimeException ex) {
        HttpStatus status = ex.getMessage() != null && ex.getMessage().toLowerCase().contains("não encontrad")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(new ApiError(status.value(), ex.getMessage()));
    }

    // Conflito de regra de negócio (ex.: cadeira já reservada, líder não pode ser removido)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> tratarEstadoInvalido(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    // Qualquer outro erro não tratado — 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> tratarGenerico(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(500, "Erro interno: " + ex.getMessage()));
    }
}