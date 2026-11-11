package cl.zthorken.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cl.zthorken.backend.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoriaId(Long categoriaId);

    boolean existsByCategoriaId(Long categoriaId);

    List<Producto> findByNombreContainingIgnoreCase(String texto);

    List<Producto> findByPrecioOfertaIsNotNull();

    @Query("select p from Producto p where p.stock <= p.stockCritico")
    List<Producto> findConStockCritico();
}
