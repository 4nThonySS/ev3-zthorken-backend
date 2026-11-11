package cl.zthorken.backend.dto;

import cl.zthorken.backend.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    String nombre,
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
    String apellidos,
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    String correo,
    @Size(min = 4, max = 100, message = "La contraseña debe tener entre 4 y 100 caracteres")
    String password,
    Rol rol,
    String calle,
    String departamento,
    String region,
    String comuna
) {
}
