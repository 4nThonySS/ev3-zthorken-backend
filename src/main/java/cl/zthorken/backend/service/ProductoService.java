package cl.zthorken.backend.service;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.zthorken.backend.dto.ProductoRequest;
import cl.zthorken.backend.dto.ProductoResponse;
import cl.zthorken.backend.exception.RecursoNoEncontradoException;
import cl.zthorken.backend.exception.ReglaNegocioException;
import cl.zthorken.backend.model.Categoria;
import cl.zthorken.backend.model.Producto;
import cl.zthorken.backend.repository.CategoriaRepository;
import cl.zthorken.backend.repository.DetalleBoletaRepository;
import cl.zthorken.backend.repository.ProductoRepository;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepo;
    private final CategoriaRepository categoriaRepo;
    private final DetalleBoletaRepository detalleRepo;

    public ProductoService(ProductoRepository productoRepo, CategoriaRepository categoriaRepo,
                           DetalleBoletaRepository detalleRepo) {
        this.productoRepo = productoRepo;
        this.categoriaRepo = categoriaRepo;
        this.detalleRepo = detalleRepo;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(Long categoriaId, String texto, boolean soloOferta) {
        Stream<Producto> productos = (categoriaId != null
            ? productoRepo.findByCategoriaId(categoriaId)
            : productoRepo.findAll()).stream();
        if (texto != null && !texto.isBlank()) {
            String buscado = texto.trim().toLowerCase();
            productos = productos.filter(p -> p.getNombre().toLowerCase().contains(buscado));
        }
        if (soloOferta) {
            productos = productos.filter(p -> p.getPrecioOferta() != null);
        }
        return productos.map(ProductoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarStockCritico() {
        return productoRepo.findConStockCritico().stream().map(ProductoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return ProductoResponse.de(buscar(id));
    }

    public ProductoResponse crear(ProductoRequest req) {
        Producto producto = new Producto();
        aplicar(producto, req);
        return ProductoResponse.de(productoRepo.save(producto));
    }

    public ProductoResponse actualizar(Long id, ProductoRequest req) {
        Producto producto = buscar(id);
        aplicar(producto, req);
        return ProductoResponse.de(productoRepo.save(producto));
    }

    public void eliminar(Long id) {
        Producto producto = buscar(id);
        detalleRepo.findByProductoId(id).forEach(d -> d.setProducto(null));
        productoRepo.delete(producto);
    }

    private void aplicar(Producto producto, ProductoRequest req) {
        if (req.precioOferta() != null && req.precioOferta() >= req.precio()) {
            throw new ReglaNegocioException("El precio de oferta debe ser menor que el precio normal");
        }
        Categoria categoria = categoriaRepo.findById(req.categoriaId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", req.categoriaId()));
        producto.setNombre(req.nombre().trim());
        producto.setDescripcion(req.descripcion());
        producto.setPrecio(req.precio());
        producto.setPrecioOferta(req.precioOferta());
        producto.setStock(req.stock());
        producto.setStockCritico(req.stockCritico());
        producto.setImagen(req.imagen());
        producto.setCategoria(categoria);
    }

    private Producto buscar(Long id) {
        return productoRepo.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
    }
}
