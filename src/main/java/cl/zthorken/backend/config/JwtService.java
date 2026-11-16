package cl.zthorken.backend.config;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cl.zthorken.backend.model.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final String EMISOR = "zthorken-api";

    private final SecretKey key;
    private final long expiracionMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expiracionMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    public TokenGenerado generar(Long usuarioId, Rol rol) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expiracionMs);
        String token = Jwts.builder()
            .id(UUID.randomUUID().toString())
            .issuer(EMISOR)
            .subject(usuarioId.toString())
            .claim("rol", rol.name())
            .issuedAt(ahora)
            .expiration(expira)
            .signWith(key)
            .compact();
        return new TokenGenerado(token, expira.getTime());
    }

    public Claims validar(String token) {
        return Jwts.parser().verifyWith(key).requireIssuer(EMISOR).build().parseSignedClaims(token).getPayload();
    }

    public record TokenGenerado(String token, long expiraEn) {
    }
}
