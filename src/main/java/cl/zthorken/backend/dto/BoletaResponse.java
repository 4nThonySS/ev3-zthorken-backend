package cl.zthorken.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import cl.zthorken.backend.model.Boleta;
import cl.zthorken.backend.model.EstadoBoleta;

public record BoletaResponse(
    Long id,
    Long usuarioId,
    LocalDateTime fecha,
    EstadoBoleta estado,
    Integer total,
    ClienteDto cliente,
    DireccionDto direccion,
    List<DetalleBoletaResponse> items
) {

    public static BoletaResponse de(Boleta b) {
        Long usuarioId = b.getUsuario() != null ? b.getUsuario().getId() : null;
        ClienteDto cliente = new ClienteDto(b.getClienteNombre(), b.getClienteApellidos(), b.getClienteCorreo());
        List<DetalleBoletaResponse> items = b.getDetalles().stream().map(DetalleBoletaResponse::de).toList();
        return new BoletaResponse(b.getId(), usuarioId, b.getFecha(), b.getEstado(), b.getTotal(), cliente,
            DireccionDto.de(b.getDireccion()), items);
    }
}
