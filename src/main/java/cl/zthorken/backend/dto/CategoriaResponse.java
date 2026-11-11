package cl.zthorken.backend.dto;

import cl.zthorken.backend.model.Categoria;

public record CategoriaResponse(Long id, String nombre, String imagen) {

    public static CategoriaResponse de(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getImagen());
    }
}
