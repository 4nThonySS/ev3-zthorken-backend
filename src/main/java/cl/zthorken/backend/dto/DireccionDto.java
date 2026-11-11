package cl.zthorken.backend.dto;

import cl.zthorken.backend.model.DireccionEnvio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DireccionDto(
    @NotBlank(message = "La calle es obligatoria")
    String calle,
    String departamento,
    @NotBlank(message = "La región es obligatoria")
    String region,
    @NotBlank(message = "La comuna es obligatoria")
    String comuna,
    @Size(max = 500, message = "Las indicaciones no pueden superar 500 caracteres")
    String indicaciones
) {

    public DireccionEnvio aEntidad() {
        return new DireccionEnvio(calle, departamento, region, comuna, indicaciones);
    }

    public static DireccionDto de(DireccionEnvio d) {
        if (d == null) {
            return null;
        }
        return new DireccionDto(d.getCalle(), d.getDepartamento(), d.getRegion(), d.getComuna(), d.getIndicaciones());
    }
}
