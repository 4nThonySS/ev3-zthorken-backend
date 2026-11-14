package cl.zthorken.backend.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Permisos por rol:
     * - Cualquiera: login, registro, ver productos y categorías, y comprar (también sin cuenta).
     * - Autenticado: editar sus propios datos (/auth/perfil) y ver boletas. El vendedor y el admin ven todas; el cliente solo las suyas (BoletaController).
     * - Solo ADMIN: el resto (crear, editar y borrar productos, categorías y usuarios; anular y borrar boletas).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/h2-console/**", "/error").permitAll()
                .requestMatchers("/api/v1/auth/perfil").authenticated()
                .requestMatchers("/api/v1/auth/**", "/api/v1/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/productos/**", "/api/v1/categorias/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/boletas").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/boletas/**").authenticated()
                .anyRequest().hasRole("ADMIN"));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
