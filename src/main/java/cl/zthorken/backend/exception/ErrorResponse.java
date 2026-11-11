package cl.zthorken.backend.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String mensaje,
    Map<String, String> campos
) {
}
