package cl.zthorken.backend.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import cl.zthorken.backend.repository.CategoriaRepository;
import cl.zthorken.backend.repository.ProductoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiControllersTest {

    @Autowired MockMvc mvc;
    @Autowired CategoriaRepository categoriaRepo;
    @Autowired ProductoRepository productoRepo;

    private Long idCategoria(String nombre) {
        return categoriaRepo.findByNombreIgnoreCase(nombre).orElseThrow().getId();
    }

    private Long idProducto(String texto) {
        return productoRepo.findByNombreContainingIgnoreCase(texto).get(0).getId();
    }

    @Test
    void listaCategoriasYProductos() throws Exception {
        mvc.perform(get("/api/v1/categorias"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(6)));
        mvc.perform(get("/api/v1/productos").param("categoriaId", idCategoria("Audio").toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/v1/productos").param("oferta", "true"))
            .andExpect(jsonPath("$", hasSize(6)));
        mvc.perform(get("/api/v1/productos/stock-critico"))
            .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void productoInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/v1/productos/9999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.mensaje").value("Producto con id 9999 no existe"));
    }

    @Test
    void idNoNumericoDevuelve400() throws Exception {
        mvc.perform(get("/api/v1/productos/abc"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void crudDeCategoria() throws Exception {
        String json = mvc.perform(post("/api/v1/categorias").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Accesorios\",\"imagen\":null}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Accesorios"))
            .andReturn().getResponse().getContentAsString();
        String id = json.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(put("/api/v1/categorias/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Accesorios gamer\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Accesorios gamer"));

        mvc.perform(delete("/api/v1/categorias/" + id))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/categorias/" + id))
            .andExpect(status().isNotFound());
    }

    @Test
    void noSeBorraCategoriaConProductos() throws Exception {
        mvc.perform(delete("/api/v1/categorias/" + idCategoria("Audio")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void validaLosDatosDelProducto() throws Exception {
        mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"\",\"precio\":-5,\"stock\":1,\"stockCritico\":1}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.nombre").exists())
            .andExpect(jsonPath("$.campos.precio").value("El precio debe ser mayor a 0"))
            .andExpect(jsonPath("$.campos.categoriaId").exists());
    }

    @Test
    void usuariosNoExponenLaPassword() throws Exception {
        mvc.perform(get("/api/v1/usuarios"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0]", not(hasKey("password"))));
        mvc.perform(get("/api/v1/usuarios").param("rol", "VENDEDOR"))
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void compraDescuentaStockYAnularLoDevuelve() throws Exception {
        Long teclado = idProducto("Redragon");
        String body = """
            {
              "cliente": {"nombre": "Ana", "apellidos": "Rojas", "correo": "ana@correo.cl"},
              "direccion": {"calle": "Calle 1", "region": "Región Metropolitana de Santiago", "comuna": "Maipú"},
              "items": [{"productoId": %d, "cantidad": 3}]
            }
            """.formatted(teclado);

        String json = mvc.perform(post("/api/v1/boletas").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.total").value(3 * 34990))
            .andExpect(jsonPath("$.estado").value("PAGADA"))
            .andExpect(jsonPath("$.items[0].subtotal").value(3 * 34990))
            .andReturn().getResponse().getContentAsString();
        String id = json.replaceAll("^\\{\"id\":(\\d+).*", "$1");

        mvc.perform(get("/api/v1/productos/" + teclado)).andExpect(jsonPath("$.stock").value(22));

        mvc.perform(patch("/api/v1/boletas/" + id + "/anular"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("ANULADA"));
        mvc.perform(get("/api/v1/productos/" + teclado)).andExpect(jsonPath("$.stock").value(25));
        mvc.perform(patch("/api/v1/boletas/" + id + "/anular"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void compraSinStockDevuelve400() throws Exception {
        String body = """
            {
              "cliente": {"nombre": "Ana", "apellidos": "Rojas", "correo": "ana@correo.cl"},
              "direccion": {"calle": "Calle 1", "region": "RM", "comuna": "Maipú"},
              "items": [{"productoId": %d, "cantidad": 100}]
            }
            """.formatted(idProducto("Logitech"));

        mvc.perform(post("/api/v1/boletas").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.mensaje").value("Stock insuficiente para Mouse Logitech G502 Hero: disponible 4, solicitado 100"));
    }

    @Test
    void compraSinItemsDevuelve400() throws Exception {
        String body = """
            {"cliente": {"nombre": "Ana", "apellidos": "Rojas", "correo": "ana@correo.cl"},
             "direccion": {"calle": "Calle 1", "region": "RM", "comuna": "Maipú"},
             "items": []}
            """;

        mvc.perform(post("/api/v1/boletas").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.items").exists());
    }

    @Test
    void listaBoletasConDetalle() throws Exception {
        mvc.perform(get("/api/v1/boletas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].items", hasSize(2)))
            .andExpect(jsonPath("$[0].total").value(129970));
    }
}
