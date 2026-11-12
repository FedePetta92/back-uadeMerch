package com.uade.e_commerce.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
import com.uade.e_commerce.repository.ProductoRepository;

/**
 * Prueba contra la aplicación real los cambios de la entrega:
 * - el registro valida los datos y responde 400/409 con un mensaje (antes, datos inválidos daban 500)
 * - el login devuelve el rol, que el front usa para mostrar el panel de administración
 * - solo un ADMIN puede crear y editar productos
 * - el buscador del catálogo filtra por nombre
 *
 * Igual que AutorizacionPorUsuarioTest: necesita la base prendida y deshace todo al terminar cada test.
 */
@SpringBootTest
@Transactional
class RegistroAdminYBuscadorTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ProductoRepository productoRepository;

    private MockMvc mockMvc;

    private record Respuesta(int status, String cuerpo) {
        void esperar(int statusEsperado) {
            assertThat(status)
                    .as("Se esperaba HTTP %d pero fue %d. Cuerpo: %s", statusEsperado, status, cuerpo)
                    .isEqualTo(statusEsperado);
        }
    }

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    // --------------------------------------------------------------- REGISTRO

    @Test
    void registro_sinSexo_devuelve400ConMensaje() throws Exception {
        Respuesta r = registrar(usuarioJson(emailNuevo(), "clave1234", null, null));
        r.esperar(400);
        assertThat(r.cuerpo()).contains("El sexo es obligatorio");
    }

    @Test
    void registro_sinFechaDeNacimiento_devuelve400ConMensaje() throws Exception {
        // Antes llegaba a la base y fallaba (en MySQL la columna es NOT NULL)
        Respuesta r = registrar("{\"nombre\":\"Test\",\"apellido\":\"Prueba\",\"email\":\"" + emailNuevo()
                + "\",\"password\":\"clave1234\",\"sexo\":\"OTRO\"}");
        r.esperar(400);
        assertThat(r.cuerpo()).contains("La fecha de nacimiento es obligatoria");
    }

    @Test
    void registro_conSexoVacio_devuelve400() throws Exception {
        // El front mandaba "" cuando no se elegía sexo: no es un valor del enum
        registrar(usuarioJson(emailNuevo(), "clave1234", "\"\"", null)).esperar(400);
    }

    @Test
    void registro_emailInvalidoYClaveCorta_devuelve400ConAmbosMensajes() throws Exception {
        Respuesta r = registrar(usuarioJson("no-es-un-email", "123", "\"OTRO\"", null));
        r.esperar(400);
        assertThat(r.cuerpo()).contains("email").contains("contraseña");
    }

    @Test
    void registro_emailRepetido_devuelve409() throws Exception {
        String email = emailNuevo();
        registrar(usuarioJson(email, "clave1234", "\"OTRO\"", null)).esperar(200);

        Respuesta r = registrar(usuarioJson(email, "clave1234", "\"OTRO\"", null));
        r.esperar(409);
        assertThat(r.cuerpo()).contains("Ya existe un usuario");
    }

    @Test
    void registro_respuestaIncluyeElRol_yNoLaContraseña() throws Exception {
        Respuesta r = registrar(usuarioJson(emailNuevo(), "clave1234", "\"OTRO\"", null));
        r.esperar(200);
        assertThat(r.cuerpo()).contains("\"rol\":\"USER\"").doesNotContain("password");
    }

    // ------------------------------------------------------------------ LOGIN

    @Test
    void login_devuelveElRolDelUsuario() throws Exception {
        assertThat(registrarYLoguear("USER").cuerpoLogin()).contains("\"rol\":\"USER\"");
        assertThat(registrarYLoguear("ADMIN").cuerpoLogin()).contains("\"rol\":\"ADMIN\"");
    }

    // -------------------------------------------------------------- PRODUCTOS

    @Test
    void admin_creaYEditaUnProducto() throws Exception {
        String token = registrarYLoguear("ADMIN").token();

        Respuesta creado = llamar(json(post("/api/productos"), productoJson("Taza Admin", "5000", 10)), token);
        creado.esperar(200);
        String id = extraer(creado.cuerpo(), "\"id\"\\s*:\\s*(\\d+)");

        Respuesta editado = llamar(json(put("/api/productos/" + id), """
                {"nombre":"Taza Admin XL","descripcion":"Nueva descripción","precio":6000,"stock":4,"imagen":"/images/x.jpg"}
                """), token);
        editado.esperar(200);
        assertThat(editado.cuerpo()).contains("Taza Admin XL").contains("Nueva descripción").contains("/images/x.jpg");
    }

    @Test
    void usuarioComun_noPuedeCrearProductos_devuelve403() throws Exception {
        String token = registrarYLoguear("USER").token();

        assertThat(llamar(json(post("/api/productos"), productoJson("Taza", "5000", 10)), token).status())
                .isEqualTo(403);
    }

    @Test
    void admin_productoSinNombreOPrecioNegativo_devuelve400() throws Exception {
        String token = registrarYLoguear("ADMIN").token();

        Respuesta r = llamar(json(post("/api/productos"), productoJson("", "-10", 5)), token);
        r.esperar(400);
        assertThat(r.cuerpo()).contains("nombre").contains("precio");
    }

    // --------------------------------------------------------------- BUSCADOR

    @Test
    void buscador_filtraPorNombreSinDistinguirMayusculas() throws Exception {
        guardarProducto("Mate Buscador Test");
        guardarProducto("Gorra Buscador Test");

        Respuesta r = llamar(get("/api/productos").param("nombre", "mATe buscador"), null);
        r.esperar(200);
        assertThat(r.cuerpo()).contains("Mate Buscador Test").doesNotContain("Gorra Buscador Test");
    }

    @Test
    void buscador_sinTexto_devuelveElCatalogoCompleto() throws Exception {
        guardarProducto("Mate Buscador Test");
        guardarProducto("Gorra Buscador Test");

        Respuesta r = llamar(get("/api/productos").param("nombre", " "), null);
        r.esperar(200);
        assertThat(r.cuerpo()).contains("Mate Buscador Test").contains("Gorra Buscador Test");
    }

    // ----------------------------------------------------------------- AYUDAS

    private record Sesion(String token, String cuerpoLogin) {
    }

    private Sesion registrarYLoguear(String rol) throws Exception {
        String email = emailNuevo();
        registrar(usuarioJson(email, "clave1234", "\"OTRO\"", rol)).esperar(200);
        Respuesta login = llamar(json(post("/api/login"),
                "{\"email\":\"" + email + "\",\"password\":\"clave1234\"}"), null);
        login.esperar(200);
        return new Sesion(extraer(login.cuerpo(), "\"token\"\\s*:\\s*\"([^\"]+)\""), login.cuerpo());
    }

    private Respuesta registrar(String cuerpo) throws Exception {
        return llamar(json(post("/api/usuarios"), cuerpo), null);
    }

    // sexo y rol se pasan ya como JSON (con comillas) o null para omitirlos
    private String usuarioJson(String email, String password, String sexo, String rol) {
        return "{\"nombre\":\"Test\",\"apellido\":\"Prueba\",\"email\":\"" + email + "\",\"password\":\"" + password
                + "\",\"fechaNacimiento\":\"2000-01-01\""
                + (sexo != null ? ",\"sexo\":" + sexo : "")
                + (rol != null ? ",\"rol\":\"" + rol + "\"" : "")
                + "}";
    }

    private String productoJson(String nombre, String precio, int stock) {
        return "{\"nombre\":\"" + nombre + "\",\"descripcion\":\"Producto de prueba\",\"precio\":" + precio
                + ",\"stock\":" + stock + "}";
    }

    private void guardarProducto(String nombre) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(new BigDecimal("1000.00"));
        producto.setStock(10);
        producto.setActivo(true);
        productoRepository.save(producto);
    }

    private String emailNuevo() {
        return "test-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    }

    private Respuesta llamar(MockHttpServletRequestBuilder peticion, String token) throws Exception {
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
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
