package cl.zthorken.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.zthorken.backend.dto.CategoriaRequest;
import cl.zthorken.backend.exception.RecursoNoEncontradoException;
import cl.zthorken.backend.exception.ReglaNegocioException;
import cl.zthorken.backend.model.Categoria;
import cl.zthorken.backend.repository.CategoriaRepository;
import cl.zthorken.backend.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock CategoriaRepository categoriaRepo;
    @Mock ProductoRepository productoRepo;
    @InjectMocks CategoriaService service;

    @Test
    void crearRechazaNombreRepetido() {
        when(categoriaRepo.existsByNombreIgnoreCase("Audio")).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> service.crear(new CategoriaRequest(" Audio ", null)));
        verify(categoriaRepo, never()).save(any());
    }

    @Test
    void actualizarPermiteMantenerElMismoNombre() {
        Categoria audio = new Categoria("Audio", null);
        audio.setId(3L);
        when(categoriaRepo.findById(3L)).thenReturn(Optional.of(audio));
        when(categoriaRepo.findByNombreIgnoreCase("audio")).thenReturn(Optional.of(audio));
        when(categoriaRepo.save(audio)).thenReturn(audio);

        assertEquals("audio", service.actualizar(3L, new CategoriaRequest("audio", "/img.jpg")).nombre());
    }

    @Test
    void noSeEliminaUnaCategoriaConProductos() {
        Categoria audio = new Categoria("Audio", null);
        when(categoriaRepo.findById(3L)).thenReturn(Optional.of(audio));
        when(productoRepo.existsByCategoriaId(3L)).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> service.eliminar(3L));
        verify(categoriaRepo, never()).delete(any());
    }

    @Test
    void obtenerFallaSiNoExiste() {
        when(categoriaRepo.findById(9L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException e = assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(9L));
        assertEquals("Categoría con id 9 no existe", e.getMessage());
    }
}
