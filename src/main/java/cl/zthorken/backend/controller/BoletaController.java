package cl.zthorken.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cl.zthorken.backend.dto.BoletaRequest;
import cl.zthorken.backend.dto.BoletaResponse;
import cl.zthorken.backend.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/boletas")
@Tag(name = "Boletas", description = "Órdenes de compra con su detalle")
public class BoletaController {

    private final BoletaService service;

    public BoletaController(BoletaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar boletas",
        description = "Ordenadas de la más reciente a la más antigua. Un CLIENTE solo recibe las suyas")
    @ApiResponse(responseCode = "200", description = "Listado de boletas")
    @ApiResponse(responseCode = "401", description = "Falta el token")
    public List<BoletaResponse> listar(
            @Parameter(description = "Solo las boletas de este usuario") @RequestParam(required = false) Long usuarioId,
            Authentication auth) {
        return service.listar(tieneRol(auth, "CLIENTE") ? idDe(auth) : usuarioId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una boleta con su detalle")
    @ApiResponse(responseCode = "200", description = "Boleta encontrada")
    @ApiResponse(responseCode = "401", description = "Falta el token")
    @ApiResponse(responseCode = "403", description = "Un CLIENTE pidió una boleta que no es suya")
    @ApiResponse(responseCode = "404", description = "La boleta no existe")
    public BoletaResponse obtener(@PathVariable Long id, Authentication auth) {
        BoletaResponse boleta = service.obtener(id);
        if (tieneRol(auth, "CLIENTE") && !idDe(auth).equals(boleta.usuarioId())) {
            throw new AccessDeniedException("No puedes ver boletas de otro usuario");
        }
        return boleta;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar una compra",
        description = "Usa el precio vigente de cada producto, calcula el total y descuenta el stock. "
            + "La boleta queda a nombre del usuario del token (sin token, compra como invitado); "
            + "solo un ADMIN puede indicar otro usuarioId")
    @ApiResponse(responseCode = "201", description = "Boleta creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o stock insuficiente")
    @ApiResponse(responseCode = "404", description = "Un producto o el usuario no existe")
    public BoletaResponse crear(@Valid @RequestBody BoletaRequest req, Authentication auth) {
        Long usuarioId = auth == null ? null : tieneRol(auth, "ADMIN") ? req.usuarioId() : idDe(auth);
        return service.crear(new BoletaRequest(usuarioId, req.cliente(), req.direccion(), req.items()));
    }

    @PatchMapping("/{id}/anular")
    @Operation(summary = "Anular una boleta", description = "Cambia el estado a ANULADA y devuelve el stock de los productos")
    @ApiResponse(responseCode = "200", description = "Boleta anulada")
    @ApiResponse(responseCode = "400", description = "La boleta ya estaba anulada")
    @ApiResponse(responseCode = "404", description = "La boleta no existe")
    public BoletaResponse anular(@PathVariable Long id) {
        return service.anular(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar una boleta")
    @ApiResponse(responseCode = "204", description = "Boleta eliminada")
    @ApiResponse(responseCode = "404", description = "La boleta no existe")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    private static boolean tieneRol(Authentication auth, String rol) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + rol));
    }

    /** El JwtAuthFilter deja el id del usuario como principal. */
    private static Long idDe(Authentication auth) {
        return (Long) auth.getPrincipal();
    }
}
