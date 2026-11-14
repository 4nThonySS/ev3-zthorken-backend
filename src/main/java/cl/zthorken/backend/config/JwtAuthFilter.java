package cl.zthorken.backend.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lee el header "Authorization: Bearer <token>" y, si el token es válido, deja autenticado al usuario
 * con su id como principal y su rol como ROLE_ADMIN, ROLE_VENDEDOR o ROLE_CLIENTE.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                // ponytail: el rol viaja en el token y no se relee de la BD; un cambio de rol o un usuario
                // borrado se nota recién al vencer el token. Buscar el usuario aquí si eso importa.
                Claims claims = jwtService.validar(header.substring(7));
                var auth = new UsernamePasswordAuthenticationToken(Long.valueOf(claims.getSubject()), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + claims.get("rol", String.class))));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException e) {
                // Token inválido o vencido: la petición sigue como anónima y las rutas protegidas responden 401.
            }
        }
        chain.doFilter(request, response);
    }
}
