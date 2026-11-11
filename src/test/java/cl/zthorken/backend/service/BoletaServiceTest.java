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

import cl.zthorken.backend.dto.BoletaRequest;
import cl.zthorken.backend.dto.BoletaResponse;
import cl.zthorken.backend.dto.ClienteDto;
import cl.zthorken.backend.dto.DireccionDto;
import cl.zthorken.backend.dto.ItemBoletaRequest;
import cl.zthorken.backend.exception.RecursoNoEncontradoException;
import cl.zthorken.backend.exception.ReglaNegocioException;
import cl.zthorken.backend.model.Boleta;
import cl.zthorken.backend.model.Categoria;
import cl.zthorken.backend.model.DetalleBoleta;
import cl.zthorken.backend.model.EstadoBoleta;
import cl.zthorken.backend.model.Producto;
import cl.zthorken.backend.repository.BoletaRepository;
import cl.zthorken.backend.repository.ProductoRepository;
import cl.zthorken.backend.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class BoletaServiceTest {

    @Mock BoletaRepository boletaRepo;
    @Mock ProductoRepository productoRepo;
    @Mock UsuarioRepository usuarioRepo;
    @InjectMocks BoletaService service;

    Producto mouse;
    Producto teclado;

    @BeforeEach
    void setUp() {
        Categoria perifericos = new Categoria("Periféricos", null);
        perifericos.setId(4L);
        mouse = new Producto("Mouse", null, 49990, 39990, 4, 5, null, perifericos);
        mouse.setId(8L);
        teclado = new Producto("Teclado", null, 34990, null, 25, 5, null, perifericos);
        teclado.setId(7L);
    }

    private BoletaRequest pedido(ItemBoletaRequest... items) {
        return new BoletaRequest(null,
            new ClienteDto("Ana", "Rojas", "ANA@correo.cl"),
            new DireccionDto("Calle 1", null, "Región Metropolitana de Santiago", "Maipú", null),
            List.of(items));
    }

    @Test
    void crearCalculaTotalConPrecioDeOfertaYDescuentaStock() {
        when(productoRepo.findById(8L)).thenReturn(Optional.of(mouse));
        when(productoRepo.findById(7L)).thenReturn(Optional.of(teclado));
        when(boletaRepo.save(any(Boleta.class))).thenAnswer(inv -> inv.getArgument(0));

        BoletaResponse boleta = service.crear(pedido(new ItemBoletaRequest(8L, 2), new ItemBoletaRequest(7L, 1)));

        assertEquals(2 * 39990 + 34990, boleta.total());
        assertEquals(EstadoBoleta.PAGADA, boleta.estado());
        assertEquals("ana@correo.cl", boleta.cliente().correo());
        assertNull(boleta.usuarioId());
        assertEquals(2, mouse.getStock());
        assertEquals(24, teclado.getStock());
    }

    @Test
    void crearJuntaLineasDelMismoProducto() {
        when(productoRepo.findById(7L)).thenReturn(Optional.of(teclado));
        when(boletaRepo.save(any(Boleta.class))).thenAnswer(inv -> inv.getArgument(0));

        BoletaResponse boleta = service.crear(pedido(new ItemBoletaRequest(7L, 1), new ItemBoletaRequest(7L, 2)));

        assertEquals(1, boleta.items().size());
        assertEquals(3, boleta.items().get(0).cantidad());
        assertEquals(22, teclado.getStock());
    }

    @Test
    void crearFallaSiNoHayStockSuficiente() {
        when(productoRepo.findById(8L)).thenReturn(Optional.of(mouse));

        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
            () -> service.crear(pedido(new ItemBoletaRequest(8L, 5))));

        assertEquals("Stock insuficiente para Mouse: disponible 4, solicitado 5", e.getMessage());
        verify(boletaRepo, never()).save(any());
    }

    @Test
    void crearFallaSiElProductoNoExiste() {
        when(productoRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(pedido(new ItemBoletaRequest(99L, 1))));
    }

    @Test
    void crearFallaSiElUsuarioNoExiste() {
        when(usuarioRepo.findById(50L)).thenReturn(Optional.empty());
        BoletaRequest req = new BoletaRequest(50L, new ClienteDto("Ana", "Rojas", "ana@correo.cl"),
            new DireccionDto("Calle 1", null, "RM", "Maipú", null), List.of(new ItemBoletaRequest(7L, 1)));

        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(req));
    }

    @Test
    void anularDevuelveElStock() {
        Boleta boleta = new Boleta();
        boleta.setId(1L);
        boleta.setEstado(EstadoBoleta.PAGADA);
        boleta.agregarDetalle(new DetalleBoleta(teclado, 34990, 3));
        boleta.setTotal(boleta.calcularTotal());
        when(boletaRepo.findById(1L)).thenReturn(Optional.of(boleta));
        when(boletaRepo.save(boleta)).thenReturn(boleta);

        BoletaResponse anulada = service.anular(1L);

        assertEquals(EstadoBoleta.ANULADA, anulada.estado());
        assertEquals(28, teclado.getStock());
    }

    @Test
    void noSePuedeAnularDosVeces() {
        Boleta boleta = new Boleta();
        boleta.setEstado(EstadoBoleta.ANULADA);
        when(boletaRepo.findById(1L)).thenReturn(Optional.of(boleta));

        assertThrows(ReglaNegocioException.class, () -> service.anular(1L));
    }
}
