package com.uade.e_commerce.config;

import com.uade.e_commerce.model.Producto;
import com.uade.e_commerce.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {

    // CommandLineRunner se ejecuta automáticamente una vez que la app arranca.
    // Si la tabla de productos ya tiene datos, no hace nada (evita duplicados).
    @Bean
    public CommandLineRunner cargarProductosIniciales(ProductoRepository productoRepository) {
        return args -> {

            // Si ya hay productos en la base de datos, no inserta nada
            if (productoRepository.count() > 0) return;

            // Lista de productos iniciales (equivalente al products.json del frontend)
            List<Producto> productos = List.of(

                // --- Indumentaria ---
                Producto.builder().nombre("Remera UADE").descripcion("Remera oficial algodón con logo UADE estampado").precio(new BigDecimal("15000")).stock(50).imagen("/images/101.jpg").build(),
                Producto.builder().nombre("Buzo Canguro UADE").descripcion("Buzo canguro con capucha y letras UADE bordadas").precio(new BigDecimal("32000")).stock(20).imagen("/images/102.jpg").build(),
                Producto.builder().nombre("Buzo Cuello Redondo UADE").descripcion("Buzo cuello redondo con letras UADE bordadas, varios colores").precio(new BigDecimal("29000")).stock(18).imagen("/images/103.jpg").build(),
                Producto.builder().nombre("Campera UADE").descripcion("Campera rompeviento con logo UADE bordado").precio(new BigDecimal("45000")).stock(15).imagen("/images/104.jpg").build(),
                Producto.builder().nombre("Chomba UADE").descripcion("Chomba piqué con logo UADE bordado en el pecho").precio(new BigDecimal("20000")).stock(22).imagen("/images/105.jpg").build(),
                Producto.builder().nombre("Gorra UADE").descripcion("Gorra con visera y logo UADE bordado, regulable").precio(new BigDecimal("8000")).stock(40).imagen("/images/106.jpg").build(),
                Producto.builder().nombre("Gorro de Invierno UADE").descripcion("Gorro de lana con logo UADE bordado").precio(new BigDecimal("6500")).stock(35).imagen("/images/107.jpg").build(),

                // --- Bazar ---
                Producto.builder().nombre("Taza Cerámica UADE").descripcion("Taza cerámica 350ml con logo UADE").precio(new BigDecimal("6000")).stock(80).imagen("/images/201.jpg").build(),
                Producto.builder().nombre("Mate Acero UADE").descripcion("Mate de acero inoxidable con grabado láser del logo UADE").precio(new BigDecimal("12000")).stock(35).imagen("/images/202.jpg").build(),
                Producto.builder().nombre("Mate de Vidrio UADE").descripcion("Mate de vidrio forrado con cuero sintético y logo UADE").precio(new BigDecimal("9500")).stock(25).imagen("/images/203.jpg").build(),
                Producto.builder().nombre("Bombilla Acero").descripcion("Bombilla de acero inoxidable con filtro").precio(new BigDecimal("3500")).stock(60).imagen("/images/204.jpg").build(),
                Producto.builder().nombre("Termo Térmico UADE").descripcion("Termo térmico 1L con logo UADE, mantiene temperatura 12hs").precio(new BigDecimal("18000")).stock(30).imagen("/images/205.jpg").build(),
                Producto.builder().nombre("Yerbera UADE").descripcion("Yerbera de acero con tapa hermética y logo UADE").precio(new BigDecimal("7500")).stock(40).imagen("/images/206.jpg").build(),
                Producto.builder().nombre("Azucarera UADE").descripcion("Azucarera de cerámica con logo UADE").precio(new BigDecimal("5500")).stock(45).imagen("/images/207.jpg").build(),
                Producto.builder().nombre("Vaso Térmico Café UADE").descripcion("Vaso térmico 350ml para café con tapa y logo UADE").precio(new BigDecimal("8500")).stock(28).imagen("/images/208.jpg").build(),
                Producto.builder().nombre("Botella Deportiva UADE").descripcion("Botella deportiva recargable 750ml con logo UADE").precio(new BigDecimal("10000")).stock(55).imagen("/images/209.jpg").build(),

                // --- Accesorios ---
                Producto.builder().nombre("Lanyard UADE").descripcion("Cinta porta credencial con logo UADE, ideal para molinetes").precio(new BigDecimal("2500")).stock(100).imagen("/images/301.jpg").build(),
                Producto.builder().nombre("Llavero UADE").descripcion("Llavero metálico con logo UADE grabado").precio(new BigDecimal("1800")).stock(120).imagen("/images/302.jpg").build(),
                Producto.builder().nombre("Pin UADE").descripcion("Pin metálico esmaltado con logo UADE").precio(new BigDecimal("1200")).stock(150).imagen("/images/303.jpg").build(),
                Producto.builder().nombre("Mochila UADE").descripcion("Mochila universitaria con logo UADE bordado, varios compartimentos").precio(new BigDecimal("38000")).stock(20).imagen("/images/304.jpg").build(),
                Producto.builder().nombre("Tote Bag UADE").descripcion("Bolso de tela resistente con diseño institucional UADE").precio(new BigDecimal("5000")).stock(60).imagen("/images/305.jpg").build(),

                // --- Librería ---
                Producto.builder().nombre("Cuaderno Espiralado UADE").descripcion("Cuaderno universitario espiralado A4 con gráfica UADE, 80 hojas").precio(new BigDecimal("4500")).stock(70).imagen("/images/401.jpg").build(),
                Producto.builder().nombre("Agenda UADE").descripcion("Agenda anual tapa dura con diseño institucional UADE").precio(new BigDecimal("9000")).stock(40).imagen("/images/402.jpg").build(),
                Producto.builder().nombre("Anotador UADE").descripcion("Anotador tapa blanda con logo UADE, 50 hojas").precio(new BigDecimal("3000")).stock(90).imagen("/images/403.jpg").build(),
                Producto.builder().nombre("Lapicera UADE").descripcion("Lapicera metálica con logo UADE grabado").precio(new BigDecimal("2000")).stock(200).imagen("/images/404.jpg").build(),
                Producto.builder().nombre("Cartuchera UADE").descripcion("Cartuchera de tela con logo UADE estampado").precio(new BigDecimal("5500")).stock(55).imagen("/images/405.jpg").build()
            );

            // Guarda todos los productos en la base de datos de una sola vez
            productoRepository.saveAll(productos);

            System.out.println("✓ Productos iniciales cargados: " + productos.size() + " productos insertados.");
        };
    }
}
