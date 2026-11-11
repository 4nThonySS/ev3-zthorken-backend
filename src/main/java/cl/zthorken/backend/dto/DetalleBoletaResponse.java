package cl.zthorken.backend.dto;

import cl.zthorken.backend.model.DetalleBoleta;

public record DetalleBoletaResponse(
    Long productoId,
    String nombre,
    Integer precio,
    Integer cantidad,
    Integer subtotal
) {

    public static DetalleBoletaResponse de(DetalleBoleta d) {
        Long productoId = d.getProducto() != null ? d.getProducto().getId() : null;
        return new DetalleBoletaResponse(productoId, d.getNombreProducto(), d.getPrecioUnitario(),
            d.getCantidad(), d.getSubtotal());
    }
}
