package cl.zthorken.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import cl.zthorken.backend.model.Boleta;
import cl.zthorken.backend.model.Categoria;
import cl.zthorken.backend.model.Producto;
import cl.zthorken.backend.model.Rol;
import cl.zthorken.backend.model.Usuario;

@SpringBootTest
@Transactional
class RepositoriosTest {

    @Autowired CategoriaRepository categoriaRepo;
    @Autowired ProductoRepository productoRepo;
    @Autowired UsuarioRepository usuarioRepo;
    @Autowired BoletaRepository boletaRepo;
    @Autowired DetalleBoletaRepository detalleRepo;

    @Test
    void cargaLosDatosIniciales() {
        assertEquals(6, categoriaRepo.count());
        assertEquals(12, productoRepo.count());
        assertEquals(3, usuarioRepo.count());
        assertEquals(1, boletaRepo.count());
    }

    @Test
    void hayUnUsuarioPorRolConPasswordCifrada() {
        for (Rol rol : Rol.values()) {
            assertEquals(1, usuarioRepo.findByRol(rol).size(), "usuarios con rol " + rol);
        }
        Usuario admin = usuarioRepo.findByCorreoIgnoreCase("ADMIN@zthorken.cl").orElseThrow();
        assertNotEquals("admin123", admin.getPassword());
        assertTrue(admin.getPassword().startsWith("$2"), "debe estar cifrada con BCrypt");
    }

    @Test
    void correoDeUsuarioEsUnico() {
        Usuario repetido = new Usuario("Otro", "Admin", "admin@zthorken.cl", "x", Rol.CLIENTE);
        assertThrows(DataIntegrityViolationException.class, () -> usuarioRepo.saveAndFlush(repetido));
    }

    @Test
    void productosSeFiltranPorCategoria() {
        Categoria audio = categoriaRepo.findByNombreIgnoreCase("audio").orElseThrow();
        List<Producto> productos = productoRepo.findByCategoriaId(audio.getId());
        assertEquals(2, productos.size());
        assertTrue(productoRepo.existsByCategoriaId(audio.getId()));
    }

    @Test
    void detectaProductosConStockCritico() {
        Set<String> criticos = productoRepo.findConStockCritico().stream().map(Producto::getNombre).collect(Collectors.toSet());
        assertEquals(Set.of("Notebook ASUS TUF Gaming F15", "Mouse Logitech G502 Hero", "Tarjeta de video RTX 4060 8 GB"), criticos);
    }

    @Test
    void precioFinalUsaLaOfertaSiExiste() {
        Producto conOferta = productoRepo.findByNombreContainingIgnoreCase("HyperX").get(0);
        Producto sinOferta = productoRepo.findByNombreContainingIgnoreCase("JBL").get(0);
        assertEquals(59990, conOferta.getPrecioFinal());
        assertEquals(109990, sinOferta.getPrecioFinal());
        assertEquals(6, productoRepo.findByPrecioOfertaIsNotNull().size());
    }

    @Test
    void boletaGuardaSusDetallesYElTotal() {
        Usuario pedro = usuarioRepo.findByCorreoIgnoreCase("pedro@correo.cl").orElseThrow();
        List<Boleta> boletas = boletaRepo.findByUsuarioIdOrderByFechaDesc(pedro.getId());
        assertEquals(1, boletas.size());

        Boleta boleta = boletas.get(0);
        assertEquals(2, boleta.getDetalles().size());
        assertEquals(129970, boleta.getTotal());
        assertEquals(boleta.calcularTotal(), boleta.getTotal());
    }

    @Test
    void borrarBoletaBorraSusDetalles() {
        Boleta boleta = boletaRepo.findAll().get(0);
        boletaRepo.delete(boleta);
        boletaRepo.flush();
        assertEquals(0, detalleRepo.count());
    }
}
