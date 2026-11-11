package cl.zthorken.backend.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.zthorken.backend.dto.BoletaRequest;
import cl.zthorken.backend.dto.BoletaResponse;
import cl.zthorken.backend.dto.ItemBoletaRequest;
import cl.zthorken.backend.exception.RecursoNoEncontradoException;
import cl.zthorken.backend.exception.ReglaNegocioException;
import cl.zthorken.backend.model.Boleta;
import cl.zthorken.backend.model.DetalleBoleta;
import cl.zthorken.backend.model.EstadoBoleta;
import cl.zthorken.backend.model.Producto;
import cl.zthorken.backend.model.Usuario;
import cl.zthorken.backend.repository.BoletaRepository;
import cl.zthorken.backend.repository.ProductoRepository;
import cl.zthorken.backend.repository.UsuarioRepository;

@Service
@Transactional
public class BoletaService {

    private final BoletaRepository boletaRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;

    public BoletaService(BoletaRepository boletaRepo, ProductoRepository productoRepo, UsuarioRepository usuarioRepo) {
        this.boletaRepo = boletaRepo;
        this.productoRepo = productoRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Transactional(readOnly = true)
    public List<BoletaResponse> listar(Long usuarioId) {
        List<Boleta> boletas = usuarioId != null
            ? boletaRepo.findByUsuarioIdOrderByFechaDesc(usuarioId)
            : boletaRepo.findAllByOrderByFechaDesc();
        return boletas.stream().map(BoletaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public BoletaResponse obtener(Long id) {
        return BoletaResponse.de(buscar(id));
    }

    public BoletaResponse crear(BoletaRequest req) {
        Boleta boleta = new Boleta();
        if (req.usuarioId() != null) {
            Usuario usuario = usuarioRepo.findById(req.usuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", req.usuarioId()));
            boleta.setUsuario(usuario);
        }
        boleta.setFecha(LocalDateTime.now());
        boleta.setEstado(EstadoBoleta.PAGADA);
        boleta.setClienteNombre(req.cliente().nombre().trim());
        boleta.setClienteApellidos(req.cliente().apellidos().trim());
        boleta.setClienteCorreo(req.cliente().correo().trim().toLowerCase());
        boleta.setDireccion(req.direccion().aEntidad());

        for (Map.Entry<Long, Integer> item : agruparPorProducto(req.items()).entrySet()) {
            Long productoId = item.getKey();
            int cantidad = item.getValue();
            Producto producto = productoRepo.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", productoId));
            if (producto.getStock() < cantidad) {
                throw new ReglaNegocioException("Stock insuficiente para " + producto.getNombre()
                    + ": disponible " + producto.getStock() + ", solicitado " + cantidad);
            }
            producto.setStock(producto.getStock() - cantidad);
            boleta.agregarDetalle(new DetalleBoleta(producto, producto.getPrecioFinal(), cantidad));
        }
        boleta.setTotal(boleta.calcularTotal());
        return BoletaResponse.de(boletaRepo.save(boleta));
    }

    public BoletaResponse anular(Long id) {
        Boleta boleta = buscar(id);
        if (boleta.getEstado() == EstadoBoleta.ANULADA) {
            throw new ReglaNegocioException("La boleta " + id + " ya está anulada");
        }
        for (DetalleBoleta detalle : boleta.getDetalles()) {
            Producto producto = detalle.getProducto();
            if (producto != null) {
                producto.setStock(producto.getStock() + detalle.getCantidad());
            }
        }
        boleta.setEstado(EstadoBoleta.ANULADA);
        return BoletaResponse.de(boletaRepo.save(boleta));
    }

    public void eliminar(Long id) {
        boletaRepo.delete(buscar(id));
    }

    private Map<Long, Integer> agruparPorProducto(List<ItemBoletaRequest> items) {
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        items.forEach(i -> cantidades.merge(i.productoId(), i.cantidad(), Integer::sum));
        return cantidades;
    }

    private Boleta buscar(Long id) {
        return boletaRepo.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Boleta", id));
    }
}
