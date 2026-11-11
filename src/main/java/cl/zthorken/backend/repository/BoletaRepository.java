package cl.zthorken.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.zthorken.backend.model.Boleta;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    List<Boleta> findAllByOrderByFechaDesc();

    List<Boleta> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
