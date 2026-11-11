package cl.zthorken.backend.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record BoletaRequest(
    Long usuarioId,
    @NotNull(message = "Los datos del cliente son obligatorios")
    @Valid
    ClienteDto cliente,
    @NotNull(message = "La dirección de envío es obligatoria")
    @Valid
    DireccionDto direccion,
    @NotEmpty(message = "La boleta debe tener al menos un producto")
    List<@Valid ItemBoletaRequest> items
) {
}
