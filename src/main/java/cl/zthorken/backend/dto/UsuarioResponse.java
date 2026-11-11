package cl.zthorken.backend.dto;

import cl.zthorken.backend.model.Rol;
import cl.zthorken.backend.model.Usuario;

public record UsuarioResponse(
    Long id,
    String nombre,
    String apellidos,
    String correo,
    Rol rol,
    String calle,
    String departamento,
    String region,
    String comuna
) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getApellidos(), u.getCorreo(), u.getRol(),
            u.getCalle(), u.getDepartamento(), u.getRegion(), u.getComuna());
    }
}
