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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import cl.zthorken.backend.dto.UsuarioRequest;
import cl.zthorken.backend.dto.UsuarioResponse;
import cl.zthorken.backend.exception.ReglaNegocioException;
import cl.zthorken.backend.model.Rol;
import cl.zthorken.backend.model.Usuario;
import cl.zthorken.backend.repository.BoletaRepository;
import cl.zthorken.backend.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock UsuarioRepository usuarioRepo;
    @Mock BoletaRepository boletaRepo;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UsuarioService service;

    private UsuarioRequest request(String correo, String password, Rol rol) {
        return new UsuarioRequest("Ana", "Rojas", correo, password, rol, null, null, null, null);
    }

    @Test
    void crearCifraLaPasswordYUsaRolClientePorDefecto() {
        when(passwordEncoder.encode("secreta")).thenReturn("$2a$cifrada");
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse u = service.crear(request(" Ana@Correo.cl ", "secreta", null));

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepo).save(guardado.capture());
        assertEquals("$2a$cifrada", guardado.getValue().getPassword());
        assertEquals("ana@correo.cl", u.correo());
        assertEquals(Rol.CLIENTE, u.rol());
    }

    @Test
    void crearRechazaCorreoRepetido() {
        when(usuarioRepo.existsByCorreoIgnoreCase("admin@zthorken.cl")).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> service.crear(request("admin@zthorken.cl", "1234", Rol.ADMIN)));
        verify(usuarioRepo, never()).save(any());
    }

    @Test
    void crearExigePassword() {
        assertThrows(ReglaNegocioException.class, () -> service.crear(request("ana@correo.cl", null, null)));
    }

    @Test
    void actualizarSinPasswordMantieneLaActual() {
        Usuario existente = new Usuario("Ana", "Rojas", "ana@correo.cl", "$2a$vieja", Rol.CLIENTE);
        existente.setId(5L);
        when(usuarioRepo.findById(5L)).thenReturn(Optional.of(existente));
        when(usuarioRepo.findByCorreoIgnoreCase("ana@correo.cl")).thenReturn(Optional.of(existente));
        when(usuarioRepo.save(existente)).thenReturn(existente);

        service.actualizar(5L, request("ana@correo.cl", null, Rol.VENDEDOR));

        assertEquals("$2a$vieja", existente.getPassword());
        assertEquals(Rol.VENDEDOR, existente.getRol());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void actualizarRechazaCorreoDeOtroUsuario() {
        Usuario ana = new Usuario("Ana", "Rojas", "ana@correo.cl", "x", Rol.CLIENTE);
        ana.setId(5L);
        Usuario admin = new Usuario("Admin", "Z", "admin@zthorken.cl", "x", Rol.ADMIN);
        admin.setId(1L);
        when(usuarioRepo.findById(5L)).thenReturn(Optional.of(ana));
        when(usuarioRepo.findByCorreoIgnoreCase("admin@zthorken.cl")).thenReturn(Optional.of(admin));

        assertThrows(ReglaNegocioException.class, () -> service.actualizar(5L, request("admin@zthorken.cl", null, null)));
    }
}
