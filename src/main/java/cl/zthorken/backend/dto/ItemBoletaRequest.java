package cl.zthorken.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemBoletaRequest(
    @NotNull(message = "El producto es obligatorio")
    Long productoId,
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    Integer cantidad
) {
}
