package cl.zthorken.backend.config;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cl.zthorken.backend.model.Boleta;
import cl.zthorken.backend.model.Categoria;
import cl.zthorken.backend.model.DetalleBoleta;
import cl.zthorken.backend.model.DireccionEnvio;
import cl.zthorken.backend.model.EstadoBoleta;
import cl.zthorken.backend.model.Producto;
import cl.zthorken.backend.model.Rol;
import cl.zthorken.backend.model.Usuario;
import cl.zthorken.backend.repository.BoletaRepository;
import cl.zthorken.backend.repository.CategoriaRepository;
import cl.zthorken.backend.repository.ProductoRepository;
import cl.zthorken.backend.repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CategoriaRepository categoriaRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;
    private final BoletaRepository boletaRepo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(CategoriaRepository categoriaRepo, ProductoRepository productoRepo,
                           UsuarioRepository usuarioRepo, BoletaRepository boletaRepo,
                           PasswordEncoder passwordEncoder) {
        this.categoriaRepo = categoriaRepo;
        this.productoRepo = productoRepo;
        this.usuarioRepo = usuarioRepo;
        this.boletaRepo = boletaRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoriaRepo.count() > 0) {
            log.info("Datos iniciales ya cargados, se omite la carga.");
            return;
        }

        Map<String, Categoria> cat = new HashMap<>();
        cat.put("computadores", categoria("Computadores", "computadores"));
        cat.put("notebooks", categoria("Notebooks", "notebooks"));
        cat.put("audio", categoria("Audio", "audio"));
        cat.put("perifericos", categoria("Periféricos", "perifericos"));
        cat.put("monitores", categoria("Monitores", "monitores"));
        cat.put("componentes", categoria("Componentes", "componentes"));

        producto("PC Gamer Ryzen 5 RTX 4060", "Ryzen 5 7600, 16 GB RAM, SSD 1 TB, RTX 4060 8 GB.", 899990, null, 8, 3, cat.get("computadores"), "pc-gamer");
        producto("PC Oficina Intel Core i5", "Core i5 12400, 8 GB RAM, SSD 512 GB, Windows 11.", 449990, 399990, 12, 3, cat.get("computadores"), "pc-oficina");
        producto("Notebook Lenovo IdeaPad 3", "15,6\" Full HD, Ryzen 5, 8 GB RAM, SSD 512 GB.", 499990, null, 15, 4, cat.get("notebooks"), "notebook-lenovo");
        producto("Notebook ASUS TUF Gaming F15", "15,6\" 144 Hz, Core i7, 16 GB RAM, RTX 4050.", 1099990, 949990, 2, 3, cat.get("notebooks"), "notebook-asus");
        Producto audifonos = producto("Audífonos HyperX Cloud II", "Audífonos gamer con sonido 7.1 y micrófono desmontable.", 79990, 59990, 20, 5, cat.get("audio"), "audifonos-hyperx");
        producto("Parlante JBL Flip 6", "Parlante Bluetooth portátil resistente al agua.", 109990, null, 10, 3, cat.get("audio"), "parlante-jbl");
        Producto teclado = producto("Teclado Mecánico Redragon Kumara", "Teclado mecánico TKL con switches rojos y retroiluminación.", 34990, null, 25, 5, cat.get("perifericos"), "teclado-redragon");
        producto("Mouse Logitech G502 Hero", "Mouse gamer 25.600 DPI con 11 botones programables.", 49990, 39990, 4, 5, cat.get("perifericos"), "mouse-logitech");
        producto("Monitor Samsung 24\" Full HD", "Panel IPS 24\", 75 Hz, HDMI.", 129990, null, 14, 4, cat.get("monitores"), "monitor-samsung");
        producto("Monitor LG UltraGear 27\" 165 Hz", "Panel IPS 27\" QHD, 165 Hz, 1 ms.", 289990, 249990, 6, 3, cat.get("monitores"), "monitor-lg");
        producto("Tarjeta de video RTX 4060 8 GB", "GPU NVIDIA GeForce RTX 4060 con DLSS 3.", 349990, null, 3, 3, cat.get("componentes"), "tarjeta-video");
        producto("SSD Kingston NV2 1 TB", "Unidad SSD NVMe PCIe 4.0 de 1 TB.", 54990, 44990, 30, 5, cat.get("componentes"), "ssd-kingston");

        usuarioRepo.save(new Usuario("Admin", "Zthorken", "admin@zthorken.cl", passwordEncoder.encode("admin123"), Rol.ADMIN));
        usuarioRepo.save(new Usuario("Valentina", "Soto", "vendedor@zthorken.cl", passwordEncoder.encode("vendedor123"), Rol.VENDEDOR));
        Usuario pedro = new Usuario("Pedro", "Pérez", "pedro@correo.cl", passwordEncoder.encode("pedro123"), Rol.CLIENTE);
        pedro.setCalle("Los Crisantemos 123");
        pedro.setDepartamento("Depto 603");
        pedro.setRegion("Región Metropolitana de Santiago");
        pedro.setComuna("Cerrillos");
        usuarioRepo.save(pedro);

        Boleta boleta = new Boleta();
        boleta.setUsuario(pedro);
        boleta.setFecha(LocalDateTime.of(2026, 10, 1, 12, 0));
        boleta.setEstado(EstadoBoleta.PAGADA);
        boleta.setClienteNombre(pedro.getNombre());
        boleta.setClienteApellidos(pedro.getApellidos());
        boleta.setClienteCorreo(pedro.getCorreo());
        boleta.setDireccion(new DireccionEnvio(pedro.getCalle(), pedro.getDepartamento(), pedro.getRegion(), pedro.getComuna(), ""));
        boleta.agregarDetalle(new DetalleBoleta(audifonos, 59990, 1));
        boleta.agregarDetalle(new DetalleBoleta(teclado, 34990, 2));
        boleta.setTotal(boleta.calcularTotal());
        boletaRepo.save(boleta);

        log.info("Datos iniciales cargados: {} categorías, {} productos, {} usuarios, {} boletas.",
            categoriaRepo.count(), productoRepo.count(), usuarioRepo.count(), boletaRepo.count());
    }

    private Categoria categoria(String nombre, String archivo) {
        return categoriaRepo.save(new Categoria(nombre, "/img/categorias/" + archivo + ".jpg"));
    }

    private Producto producto(String nombre, String descripcion, int precio, Integer precioOferta,
                              int stock, int stockCritico, Categoria categoria, String archivo) {
        return productoRepo.save(new Producto(nombre, descripcion, precio, precioOferta, stock, stockCritico,
            "/img/productos/" + archivo + ".jpg", categoria));
    }
}
