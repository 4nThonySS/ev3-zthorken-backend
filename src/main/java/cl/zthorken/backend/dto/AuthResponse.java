package cl.zthorken.backend.dto;

public record AuthResponse(
    String token,
    UsuarioResponse usuario
) {
}
