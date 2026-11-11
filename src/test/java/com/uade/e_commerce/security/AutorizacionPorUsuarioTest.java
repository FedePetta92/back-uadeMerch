package com.uade.e_commerce.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.uade.e_commerce.model.Producto;
import com.uade.e_commerce.repository.CarritoRepository;
import com.uade.e_commerce.repository.ProductoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Prueba el flujo completo contra la aplicacion real (filtro JWT, controllers, servicios y base de datos):
 * se registran dos usuarios (Ana y Beto), cada uno se loguea y recibe su token, y despues se verifica que
 * cada uno puede operar sobre lo suyo (200/201) y que NO puede tocar lo del otro (403).
 *
 * Necesita MySQL prendido, igual que contextLoads. Al ser @Transactional, todo lo que crea el test
 * (usuarios, carritos, ordenes) se deshace al terminar cada test: no deja datos en la base.
 */
@SpringBootTest
@Transactional
class AutorizacionPorUsuarioTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private CarritoRepository carritoRepository;
    @PersistenceContext
    private EntityManager entityManager;

    private MockMvc mockMvc;
    private Sesion ana;
    private Sesion beto;
    private Long productoId;

    // Lo que devuelve el login: el id del usuario y su token JWT.
    private record Sesion(Long id, String token) {
    }

    // Lo que devolvio la API: codigo HTTP y cuerpo.
    private record Respuesta(int status, String cuerpo) {
        void esperar(int statusEsperado) {
            assertThat(status)
                    .as("Se esperaba HTTP %d pero fue %d. Cuerpo: %s", statusEsperado, status, cuerpo)
                    .isEqualTo(statusEsperado);
        }

        // 403 con el mensaje de nuestra AccesoDenegadoException (el 403 "pelado" de Spring Security viene sin cuerpo).
        void esperarAccesoDenegado() {
            esperar(403);
            assertThat(cuerpo).contains("permiso");
        }
    }

    @BeforeEach
    void prepararEscenario() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        ana = registrarYLoguear("Ana");
        beto = registrarYLoguear("Beto");
        productoId = crearProducto();
    }

    // ---------------------------------------------------------------- USUARIOS

    @Test
    void usuario_consultaYActualizaSuPropioPerfil_funciona() throws Exception {
        Respuesta perfil = llamar(get("/api/usuarios/" + ana.id()), ana);
        perfil.esperar(200);
        assertThat(perfil.cuerpo()).contains("\"id\":" + ana.id());
        assertThat(perfil.cuerpo()).doesNotContain("password"); // B1: nunca sale el hash

        Respuesta actualizado = llamar(
                json(put("/api/usuarios/" + ana.id()), "{\"nombre\":\"Ana Maria\",\"apellido\":\"Nueva\"}"), ana);
        actualizado.esperar(200);
        assertThat(actualizado.cuerpo()).contains("Ana Maria");
    }

    @Test
    void usuario_consultarPerfilAjeno_devuelve403() throws Exception {
        llamar(get("/api/usuarios/" + beto.id()), ana).esperarAccesoDenegado();
    }

    @Test
    void usuario_actualizarPerfilAjeno_devuelve403_yNoCambiaNada() throws Exception {
        llamar(json(put("/api/usuarios/" + beto.id()), "{\"nombre\":\"Hackeado\",\"apellido\":\"Hackeado\"}"), ana)
                .esperarAccesoDenegado();

        Respuesta perfilDeBeto = llamar(get("/api/usuarios/" + beto.id()), beto);
        perfilDeBeto.esperar(200);
        assertThat(perfilDeBeto.cuerpo()).doesNotContain("Hackeado");
    }

    @Test
    void usuario_borrarCuentaAjena_devuelve403_yLaCuentaSigueExistiendo() throws Exception {
        llamar(delete("/api/usuarios/" + beto.id()), ana).esperarAccesoDenegado();

        llamar(get("/api/usuarios/" + beto.id()), beto).esperar(200);
    }

    @Test
    void usuario_borrarSuPropiaCuenta_funciona() throws Exception {
        llamar(delete("/api/usuarios/" + ana.id()), ana).esperar(200);

        // Con la cuenta borrada, su token ya no sirve: la API tiene que rechazarlo.
        Respuesta conTokenViejo = llamar(get("/api/usuarios/" + ana.id()), ana);
        assertThat(conTokenViejo.status()).isBetween(400, 499);
    }

    // ----------------------------------------------------------------- CARRITO

    @Test
    void carrito_flujoCompletoDelPropioUsuario_funciona() throws Exception {
        // Agregar un producto (2 unidades a $1000 c/u)
        Respuesta agregado = llamar(json(post("/api/carritos/" + ana.id() + "/items"), itemJson(2)), ana);
        agregado.esperar(200);

        // Ver el carrito
        Respuesta carrito = llamar(get("/api/carritos/" + ana.id()), ana);
        carrito.esperar(200);
        assertThat(carrito.cuerpo()).contains("\"usuarioId\":" + ana.id());
        assertThat(carrito.cuerpo()).contains("Remera Test");

        // Ver el total
        Respuesta total = llamar(get("/api/carritos/" + ana.id() + "/total"), ana);
        total.esperar(200);
        assertThat(total.cuerpo()).contains("2000");

        // Quitar el item: el carrito queda vacio
        Long itemId = idDelPrimerItem(ana.id());
        Respuesta sinItems = llamar(delete("/api/carritos/" + ana.id() + "/items/" + itemId), ana);
        sinItems.esperar(200);
        assertThat(sinItems.cuerpo()).contains("\"items\":[]");
    }

    @Test
    void carrito_verCarritoAjeno_devuelve403() throws Exception {
        llamar(json(post("/api/carritos/" + beto.id() + "/items"), itemJson(1)), beto).esperar(200);

        llamar(get("/api/carritos/" + beto.id()), ana).esperarAccesoDenegado();
    }

    @Test
    void carrito_agregarEnCarritoAjeno_devuelve403_yNoCreaNada() throws Exception {
        llamar(json(post("/api/carritos/" + beto.id() + "/items"), itemJson(1)), ana).esperarAccesoDenegado();

        sincronizarConBase();
        assertThat(carritoRepository.findByUsuarioId(beto.id())).isEmpty();
    }

    @Test
    void carrito_quitarItemDeCarritoAjeno_devuelve403_yElItemSigue() throws Exception {
        llamar(json(post("/api/carritos/" + beto.id() + "/items"), itemJson(1)), beto).esperar(200);
        Long itemDeBeto = idDelPrimerItem(beto.id());

        llamar(delete("/api/carritos/" + beto.id() + "/items/" + itemDeBeto), ana).esperarAccesoDenegado();

        sincronizarConBase();
        assertThat(carritoRepository.findByUsuarioId(beto.id()).orElseThrow().getItems()).hasSize(1);
    }

    @Test
    void carrito_totalAjeno_devuelve403() throws Exception {
        llamar(json(post("/api/carritos/" + beto.id() + "/items"), itemJson(1)), beto).esperar(200);

        llamar(get("/api/carritos/" + beto.id() + "/total"), ana).esperarAccesoDenegado();
    }

    // ------------------------------------------------------------------ COMPRA

    @Test
    void compra_flujoCompletoDelPropioUsuario_funciona() throws Exception {
        llamar(json(post("/api/carritos/" + ana.id() + "/items"), itemJson(2)), ana).esperar(200);

        // Checkout con una tarjeta que el pago simulado aprueba
        Respuesta orden = llamar(
                json(post("/api/ordenes-compra/" + ana.id() + "/checkout"), "{\"numeroTarjeta\":\"4111111111111111\"}"),
                ana);
        orden.esperar(201);
        assertThat(orden.cuerpo()).contains("PAGADA");

        // Se descuenta el stock (10 - 2) y el carrito queda vacio
        sincronizarConBase();
        assertThat(productoRepository.findById(productoId).orElseThrow().getStock()).isEqualTo(8);
        Respuesta carritoVacio = llamar(get("/api/carritos/" + ana.id()), ana);
        carritoVacio.esperar(200);
        assertThat(carritoVacio.cuerpo()).contains("\"items\":[]");

        // La orden aparece en su historial
        Respuesta historial = llamar(get("/api/ordenes-compra/usuario/" + ana.id()), ana);
        historial.esperar(200);
        assertThat(historial.cuerpo()).contains("\"usuarioId\":" + ana.id()).contains("PAGADA");
    }

    @Test
    void compra_checkoutEnNombreDeOtro_devuelve403_ySinTocarStockNiCarrito() throws Exception {
        llamar(json(post("/api/carritos/" + beto.id() + "/items"), itemJson(2)), beto).esperar(200);

        llamar(json(post("/api/ordenes-compra/" + beto.id() + "/checkout"), "{\"numeroTarjeta\":\"4111111111111111\"}"),
                ana).esperarAccesoDenegado();

        // Nada se cobro: stock intacto, carrito de Beto con su item y sin ordenes
        sincronizarConBase();
        assertThat(productoRepository.findById(productoId).orElseThrow().getStock()).isEqualTo(10);
        assertThat(carritoRepository.findByUsuarioId(beto.id()).orElseThrow().getItems()).hasSize(1);
        Respuesta historialDeBeto = llamar(get("/api/ordenes-compra/usuario/" + beto.id()), beto);
        historialDeBeto.esperar(200);
        assertThat(historialDeBeto.cuerpo()).isEqualTo("[]");
    }

    @Test
    void compra_historialAjeno_devuelve403() throws Exception {
        llamar(get("/api/ordenes-compra/usuario/" + beto.id()), ana).esperarAccesoDenegado();
    }

    // --------------------------------------------------------------- SIN TOKEN

    @Test
    void sinToken_losEndpointsProtegidosRechazanLaPeticion() throws Exception {
        assertThat(llamar(get("/api/carritos/" + ana.id()), null).status()).isBetween(400, 499);
        assertThat(llamar(get("/api/usuarios/" + ana.id()), null).status()).isBetween(400, 499);
        assertThat(llamar(get("/api/ordenes-compra/usuario/" + ana.id()), null).status()).isBetween(400, 499);
    }

    // ----------------------------------------------------------------- AYUDAS

    // Registra un usuario nuevo (email unico por corrida) y lo loguea como lo haria el front.
    private Sesion registrarYLoguear(String nombre) throws Exception {
        String email = nombre.toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        String clave = "clave1234";

        llamar(json(post("/api/usuarios"), """
                {"nombre":"%s","apellido":"Prueba","email":"%s","password":"%s","fechaNacimiento":"2000-01-01","sexo":"OTRO"}
                """.formatted(nombre, email, clave)), null).esperar(200);

        Respuesta login = llamar(json(post("/api/login"), """
                {"email":"%s","password":"%s"}
                """.formatted(email, clave)), null);
        login.esperar(200);

        return new Sesion(
                Long.valueOf(extraer(login.cuerpo(), "\"usuarioId\"\\s*:\\s*(\\d+)")),
                extraer(login.cuerpo(), "\"token\"\\s*:\\s*\"([^\"]+)\""));
    }

    // Crea un producto de prueba con stock 10. Se usa el constructor vacio porque el builder deja categorias en null.
    private Long crearProducto() {
        Producto producto = new Producto();
        producto.setNombre("Remera Test");
        producto.setDescripcion("Producto de prueba");
        producto.setPrecio(new BigDecimal("1000.00"));
        producto.setStock(10);
        producto.setActivo(true);
        return productoRepository.save(producto).getId();
    }

    private String itemJson(int cantidad) {
        return "{\"productoId\":" + productoId + ",\"cantidad\":" + cantidad + "}";
    }

    private Long idDelPrimerItem(Long usuarioId) {
        sincronizarConBase();
        return carritoRepository.findByUsuarioId(usuarioId).orElseThrow().getItems().get(0).getId();
    }

    // Manda lo pendiente a la base y vacia la memoria de JPA: lo que se lea despues viene de la base real.
    private void sincronizarConBase() {
        entityManager.flush();
        entityManager.clear();
    }

    private Respuesta llamar(MockHttpServletRequestBuilder peticion, Sesion sesion) throws Exception {
        if (sesion != null) {
            peticion.header("Authorization", "Bearer " + sesion.token());
        }
        MvcResult resultado = mockMvc.perform(peticion).andReturn();
        return new Respuesta(resultado.getResponse().getStatus(), resultado.getResponse().getContentAsString());
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder peticion, String cuerpo) {
        return peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
    }

    private String extraer(String texto, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(texto);
        assertThat(matcher.find()).as("No se encontro %s en: %s", regex, texto).isTrue();
        return matcher.group(1);
    }
}
