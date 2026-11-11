package cl.zthorken.backend.dto;

import cl.zthorken.backend.model.Producto;

public record ProductoResponse(
    Long id,
    String nombre,
    String descripcion,
    Integer precio,
    Integer precioOferta,
    Integer precioFinal,
    Integer stock,
    Integer stockCritico,
    boolean enStockCritico,
    String imagen,
    Long categoriaId,
    String categoriaNombre
) {

    public static ProductoResponse de(Producto p) {
        return new ProductoResponse(p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(),
            p.getPrecioOferta(), p.getPrecioFinal(), p.getStock(), p.getStockCritico(), p.isStockCritico(),
            p.getImagen(), p.getCategoria().getId(), p.getCategoria().getNombre());
    }
}
