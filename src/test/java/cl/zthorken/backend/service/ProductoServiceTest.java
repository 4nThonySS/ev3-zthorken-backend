package cl.zthorken.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.zthorken.backend.dto.ProductoRequest;
import cl.zthorken.backend.dto.ProductoResponse;
import cl.zthorken.backend.exception.RecursoNoEncontradoException;
import cl.zthorken.backend.exception.ReglaNegocioException;
import cl.zthorken.backend.model.Categoria;
import cl.zthorken.backend.model.DetalleBoleta;
import cl.zthorken.backend.model.Producto;
import cl.zthorken.backend.repository.CategoriaRepository;
import cl.zthorken.backend.repository.DetalleBoletaRepository;
import cl.zthorken.backend.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock ProductoRepository productoRepo;
    @Mock CategoriaRepository categoriaRepo;
    @Mock DetalleBoletaRepository detalleRepo;
    @InjectMocks ProductoService service;

    Categoria audio;

    @BeforeEach
    void setUp() {
        audio = new Categoria("Audio", null);
        audio.setId(3L);
    }

    @Test
    void crearAsignaLaCategoria() {
        when(categoriaRepo.findById(3L)).thenReturn(Optional.of(audio));
        when(productoRepo.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse p = service.crear(new ProductoRequest(" Parlante ", "desc", 10000, 8000, 5, 2, null, 3L));

        assertEquals("Parlante", p.nombre());
        assertEquals(8000, p.precioFinal());
        assertEquals("Audio", p.categoriaNombre());
    }

    @Test
    void ofertaDebeSerMenorQueElPrecio() {
        ProductoRequest req = new ProductoRequest("Parlante", null, 10000, 10000, 5, 2, null, 3L);

        assertThrows(ReglaNegocioException.class, () -> service.crear(req));
        verify(productoRepo, never()).save(any());
    }

    @Test
    void crearFallaSiLaCategoriaNoExiste() {
        when(categoriaRepo.findById(3L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
            () -> service.crear(new ProductoRequest("Parlante", null, 10000, null, 5, 2, null, 3L)));
    }

    @Test
    void listarFiltraPorTextoYOferta() {
        Producto a = new Producto("Audífonos HyperX", null, 79990, 59990, 20, 5, null, audio);
        Producto b = new Producto("Parlante JBL", null, 109990, null, 10, 3, null, audio);
        Producto c = new Producto("Audífonos Sony", null, 50000, null, 10, 3, null, audio);
        when(productoRepo.findAll()).thenReturn(List.of(a, b, c));

        assertEquals(2, service.listar(null, "audífonos", false).size());
        assertEquals(1, service.listar(null, "audífonos", true).size());
        assertEquals(3, service.listar(null, " ", false).size());
    }

    @Test
    void eliminarDesvinculaLosDetallesDeBoleta() {
        Producto p = new Producto("Parlante", null, 10000, null, 5, 2, null, audio);
        p.setId(6L);
        DetalleBoleta detalle = new DetalleBoleta(p, 10000, 1);
        when(productoRepo.findById(6L)).thenReturn(Optional.of(p));
        when(detalleRepo.findByProductoId(6L)).thenReturn(List.of(detalle));

        service.eliminar(6L);

        assertNull(detalle.getProducto());
        assertEquals("Parlante", detalle.getNombreProducto());
        verify(productoRepo).delete(p);
    }
}
