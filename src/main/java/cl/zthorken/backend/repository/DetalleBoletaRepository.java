package cl.zthorken.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.zthorken.backend.model.DetalleBoleta;

public interface DetalleBoletaRepository extends JpaRepository<DetalleBoleta, Long> {

    List<DetalleBoleta> findByProductoId(Long productoId);
}
