package cl.zthorken.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductoRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    String nombre,
    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    String descripcion,
    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    Integer precio,
    @Positive(message = "El precio de oferta debe ser mayor a 0")
    Integer precioOferta,
    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    Integer stock,
    @NotNull(message = "El stock crítico es obligatorio")
    @PositiveOrZero(message = "El stock crítico no puede ser negativo")
    Integer stockCritico,
    String imagen,
    @NotNull(message = "La categoría es obligatoria")
    Long categoriaId
) {
}
