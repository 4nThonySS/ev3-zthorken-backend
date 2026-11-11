package cl.zthorken.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteDto(
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    String nombre,
    @NotBlank(message = "Los apellidos del cliente son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
    String apellidos,
    @NotBlank(message = "El correo del cliente es obligatorio")
    @Email(message = "El correo del cliente no es válido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    String correo
) {
}
