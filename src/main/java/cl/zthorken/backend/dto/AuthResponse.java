package cl.zthorken.backend.dto;

public record AuthResponse(
    String token,
    long expiraEn,
    UsuarioResponse usuario
) {
}
