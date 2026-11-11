package cl.zthorken.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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
    @Operation(summary = "Listar boletas", description = "Ordenadas de la más reciente a la más antigua")
    public List<BoletaResponse> listar(
            @Parameter(description = "Solo las boletas de este usuario") @RequestParam(required = false) Long usuarioId) {
        return service.listar(usuarioId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una boleta con su detalle")
    @ApiResponse(responseCode = "200", description = "Boleta encontrada")
    @ApiResponse(responseCode = "404", description = "La boleta no existe")
    public BoletaResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar una compra",
        description = "Usa el precio vigente de cada producto, calcula el total y descuenta el stock")
    @ApiResponse(responseCode = "201", description = "Boleta creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o stock insuficiente")
    @ApiResponse(responseCode = "404", description = "Un producto o el usuario no existe")
    public BoletaResponse crear(@Valid @RequestBody BoletaRequest req) {
        return service.crear(req);
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
}
