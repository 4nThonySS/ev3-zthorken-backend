package cl.zthorken.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.zthorken.backend.dto.CategoriaRequest;
import cl.zthorken.backend.dto.CategoriaResponse;
import cl.zthorken.backend.exception.RecursoNoEncontradoException;
import cl.zthorken.backend.exception.ReglaNegocioException;
import cl.zthorken.backend.model.Categoria;
import cl.zthorken.backend.repository.CategoriaRepository;
import cl.zthorken.backend.repository.ProductoRepository;

@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepo;
    private final ProductoRepository productoRepo;

    public CategoriaService(CategoriaRepository categoriaRepo, ProductoRepository productoRepo) {
        this.categoriaRepo = categoriaRepo;
        this.productoRepo = productoRepo;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepo.findAll().stream().map(CategoriaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtener(Long id) {
        return CategoriaResponse.de(buscar(id));
    }

    public CategoriaResponse crear(CategoriaRequest req) {
        String nombre = req.nombre().trim();
        if (categoriaRepo.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe una categoría llamada " + nombre);
        }
        return CategoriaResponse.de(categoriaRepo.save(new Categoria(nombre, req.imagen())));
    }

    public CategoriaResponse actualizar(Long id, CategoriaRequest req) {
        Categoria categoria = buscar(id);
        String nombre = req.nombre().trim();
        categoriaRepo.findByNombreIgnoreCase(nombre)
            .filter(otra -> !otra.getId().equals(id))
            .ifPresent(otra -> {
                throw new ReglaNegocioException("Ya existe una categoría llamada " + nombre);
            });
        categoria.setNombre(nombre);
        categoria.setImagen(req.imagen());
        return CategoriaResponse.de(categoriaRepo.save(categoria));
    }

    public void eliminar(Long id) {
        Categoria categoria = buscar(id);
        if (productoRepo.existsByCategoriaId(id)) {
            throw new ReglaNegocioException("No se puede eliminar la categoría " + categoria.getNombre() + " porque tiene productos");
        }
        categoriaRepo.delete(categoria);
    }

    private Categoria buscar(Long id) {
        return categoriaRepo.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Categoría", id));
    }
}
