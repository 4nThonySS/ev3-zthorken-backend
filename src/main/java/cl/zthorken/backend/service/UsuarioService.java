package cl.zthorken.backend.service;

import java.util.List;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.zthorken.backend.dto.UsuarioRequest;
import cl.zthorken.backend.dto.UsuarioResponse;
import cl.zthorken.backend.exception.RecursoNoEncontradoException;
import cl.zthorken.backend.exception.ReglaNegocioException;
import cl.zthorken.backend.model.Rol;
import cl.zthorken.backend.model.Usuario;
import cl.zthorken.backend.repository.BoletaRepository;
import cl.zthorken.backend.repository.UsuarioRepository;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final BoletaRepository boletaRepo;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepo, BoletaRepository boletaRepo, PasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.boletaRepo = boletaRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(Rol rol) {
        List<Usuario> usuarios = rol != null ? usuarioRepo.findByRol(rol) : usuarioRepo.findAll();
        return usuarios.stream().map(UsuarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.de(buscar(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse autenticar(String correo, String password) {
        return usuarioRepo.findByCorreoIgnoreCase(normalizarCorreo(correo))
            .filter(u -> passwordEncoder.matches(password, u.getPassword()))
            .map(UsuarioResponse::de)
            .orElseThrow(() -> new BadCredentialsException("Correo o contraseña incorrectos"));
    }

    public UsuarioResponse crear(UsuarioRequest req) {
        if (req.password() == null || req.password().isBlank()) {
            throw new ReglaNegocioException("La contraseña es obligatoria");
        }
        String correo = normalizarCorreo(req.correo());
        if (usuarioRepo.existsByCorreoIgnoreCase(correo)) {
            throw new ReglaNegocioException("Ya existe un usuario con el correo " + correo);
        }
        Usuario usuario = new Usuario();
        aplicar(usuario, req, correo);
        usuario.setPassword(passwordEncoder.encode(req.password()));
        return UsuarioResponse.de(usuarioRepo.save(usuario));
    }

    public UsuarioResponse actualizar(Long id, UsuarioRequest req) {
        Usuario usuario = buscar(id);
        String correo = normalizarCorreo(req.correo());
        usuarioRepo.findByCorreoIgnoreCase(correo)
            .filter(otro -> !otro.getId().equals(id))
            .ifPresent(otro -> {
                throw new ReglaNegocioException("Ya existe un usuario con el correo " + correo);
            });
        aplicar(usuario, req, correo);
        if (req.password() != null && !req.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(req.password()));
        }
        return UsuarioResponse.de(usuarioRepo.save(usuario));
    }

    public void eliminar(Long id) {
        Usuario usuario = buscar(id);
        boletaRepo.findByUsuarioIdOrderByFechaDesc(id).forEach(b -> b.setUsuario(null));
        usuarioRepo.delete(usuario);
    }

    private void aplicar(Usuario usuario, UsuarioRequest req, String correo) {
        usuario.setNombre(req.nombre().trim());
        usuario.setApellidos(req.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setRol(req.rol() != null ? req.rol() : Rol.CLIENTE);
        usuario.setCalle(req.calle());
        usuario.setDepartamento(req.departamento());
        usuario.setRegion(req.region());
        usuario.setComuna(req.comuna());
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }

    private Usuario buscar(Long id) {
        return usuarioRepo.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }
}
