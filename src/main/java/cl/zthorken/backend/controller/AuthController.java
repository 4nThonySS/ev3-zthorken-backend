package cl.zthorken.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cl.zthorken.backend.config.JwtService;
import cl.zthorken.backend.dto.AuthResponse;
import cl.zthorken.backend.dto.LoginRequest;
import cl.zthorken.backend.dto.UsuarioRequest;
import cl.zthorken.backend.dto.UsuarioResponse;
import cl.zthorken.backend.model.Rol;
import cl.zthorken.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Login y registro; devuelven el token JWT")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public AuthController(UsuarioService usuarioService, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
        description = "Devuelve el token para enviar en el header Authorization: Bearer <token>")
    @ApiResponse(responseCode = "200", description = "Credenciales correctas")
    @ApiResponse(responseCode = "401", description = "Correo o contraseña incorrectos")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return conToken(usuarioService.autenticar(req.correo(), req.password()));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrarse como cliente", description = "El rol enviado se ignora: siempre crea un CLIENTE")
    @ApiResponse(responseCode = "201", description = "Cuenta creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o correo ya registrado")
    public AuthResponse register(@Valid @RequestBody UsuarioRequest req) {
        UsuarioRequest cliente = new UsuarioRequest(req.nombre(), req.apellidos(), req.correo(), req.password(),
            Rol.CLIENTE, req.calle(), req.departamento(), req.region(), req.comuna());
        return conToken(usuarioService.crear(cliente));
    }

    @PutMapping("/perfil")
    @Operation(summary = "Editar mis datos",
        description = "Actualiza al usuario del token. El rol no cambia; la contraseña vacía se mantiene")
    @ApiResponse(responseCode = "200", description = "Perfil actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o correo ya registrado")
    @ApiResponse(responseCode = "401", description = "Falta el token")
    public UsuarioResponse perfil(@Valid @RequestBody UsuarioRequest req, Authentication auth) {
        Long id = (Long) auth.getPrincipal();
        Rol rolActual = usuarioService.obtener(id).rol();
        return usuarioService.actualizar(id, new UsuarioRequest(req.nombre(), req.apellidos(), req.correo(),
            req.password(), rolActual, req.calle(), req.departamento(), req.region(), req.comuna()));
    }

    private AuthResponse conToken(UsuarioResponse usuario) {
        return new AuthResponse(jwtService.generar(usuario.id(), usuario.rol()), usuario);
    }
}
