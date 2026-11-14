package cl.zthorken.backend.controller;

import static cl.zthorken.backend.controller.ApiControllersTest.token;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import cl.zthorken.backend.repository.BoletaRepository;
import cl.zthorken.backend.repository.ProductoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SeguridadTest {

    @Autowired MockMvc mvc;
    @Autowired BoletaRepository boletaRepo;
    @Autowired ProductoRepository productoRepo;

    private static final String NUEVO_CLIENTE = """
        {"nombre": "Ana", "apellidos": "Rojas", "correo": "ana@correo.cl", "password": "ana123", "rol": "ADMIN"}
        """;

    private String vendedor() throws Exception {
        return token(mvc, "vendedor@zthorken.cl", "vendedor123");
    }

    private String pedro() throws Exception {
        return token(mvc, "pedro@correo.cl", "pedro123");
    }

    private Long boletaDePedro() {
        return boletaRepo.findAllByOrderByFechaDesc().get(0).getId();
    }

    @Test
    void loginCorrectoDevuelveTokenYUsuario() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"ADMIN@zthorken.cl\",\"password\":\"admin123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.usuario.rol").value("ADMIN"))
            .andExpect(jsonPath("$.usuario.password").doesNotExist());
    }

    @Test
    void loginIncorrectoDevuelve401() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"admin@zthorken.cl\",\"password\":\"mala\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos"));
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"nadie@correo.cl\",\"password\":\"x\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void sinTokenSoloSeVeLaTienda() throws Exception {
        mvc.perform(get("/api/v1/productos")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/categorias")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/boletas")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/usuarios")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/v1/productos/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoDevuelve401() throws Exception {
        mvc.perform(get("/api/v1/boletas").header("Authorization", "Bearer no.es.un.token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void vendedorSoloVeProductosYBoletas() throws Exception {
        String v = vendedor();
        mvc.perform(get("/api/v1/productos").header("Authorization", v)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/boletas").header("Authorization", v))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].items", hasSize(2)));
        mvc.perform(get("/api/v1/boletas/" + boletaDePedro()).header("Authorization", v)).andExpect(status().isOk());

        mvc.perform(get("/api/v1/usuarios").header("Authorization", v)).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/categorias").header("Authorization", v).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Nueva\"}"))
            .andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/boletas/" + boletaDePedro() + "/anular").header("Authorization", v))
            .andExpect(status().isForbidden());
    }

    @Test
    void clienteSoloVeSusBoletas() throws Exception {
        String p = pedro();
        mvc.perform(get("/api/v1/boletas").header("Authorization", p)).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/v1/boletas/" + boletaDePedro()).header("Authorization", p)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/usuarios").header("Authorization", p)).andExpect(status().isForbidden());

        String ana = mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(NUEVO_CLIENTE))
            .andReturn().getResponse().getContentAsString();
        String tokenAna = "Bearer " + JsonPath.read(ana, "$.token");
        Long idPedro = boletaRepo.findById(boletaDePedro()).orElseThrow().getUsuario().getId();

        mvc.perform(get("/api/v1/boletas").header("Authorization", tokenAna).param("usuarioId", idPedro.toString()))
            .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/v1/boletas/" + boletaDePedro()).header("Authorization", tokenAna))
            .andExpect(status().isForbidden());
    }

    @Test
    void registroCreaSiempreUnCliente() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(NUEVO_CLIENTE))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"));
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(NUEVO_CLIENTE))
            .andExpect(status().isBadRequest());
    }

    @Test
    void compraConTokenQuedaANombreDelCliente() throws Exception {
        String p = pedro();
        Long idPedro = boletaRepo.findById(boletaDePedro()).orElseThrow().getUsuario().getId();
        Long teclado = productoRepo.findByNombreContainingIgnoreCase("Redragon").get(0).getId();
        String body = """
            {
              "usuarioId": 9999,
              "cliente": {"nombre": "Pedro", "apellidos": "Pérez", "correo": "pedro@correo.cl"},
              "direccion": {"calle": "Calle 1", "region": "RM", "comuna": "Maipú"},
              "items": [{"productoId": %d, "cantidad": 1}]
            }
            """.formatted(teclado);

        mvc.perform(post("/api/v1/boletas").header("Authorization", p).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.usuarioId").value(idPedro));
        mvc.perform(get("/api/v1/boletas").header("Authorization", p)).andExpect(jsonPath("$", hasSize(2)));
    }
}
