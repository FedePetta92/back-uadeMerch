package com.uade.e_commerce.service;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.e_commerce.dto.ProductoRequestDTO;
import com.uade.e_commerce.dto.ProductoResponseDTO;
import com.uade.e_commerce.dto.ProductoUpdateDTO;
import com.uade.e_commerce.exceptions.PrecioNegativoException;
import com.uade.e_commerce.exceptions.RecursoNoEncontradoException;
import com.uade.e_commerce.model.Producto;
import com.uade.e_commerce.repository.CarritoItemRepository;
import com.uade.e_commerce.repository.ProductoRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductoService {

    
    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CarritoItemRepository carritoItemRepository;

      

    public List<Producto> getAllProductos() {
        return productoRepository.findByActivoTrue();
    }

    public Producto getProductoById(Long id) {
        return productoRepository.findByIdAndActivoTrue(id).orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
    }

    // Borrado lógico: el producto no se elimina porque las órdenes de compra que lo vendieron lo siguen referenciando.
    // Se saca del catálogo y de los carritos (que son datos temporales), así ya no se puede comprar.
    public void deleteProducto(Long id) {
        Producto producto = getProductoById(id);

        carritoItemRepository.deleteByProductoId(id);

        producto.setActivo(false);
        producto.setFechaBaja(LocalDateTime.now());
        productoRepository.save(producto);
    }

    public ProductoResponseDTO updateProducto(Long id, ProductoUpdateDTO dto) {
        Integer precioCasteado = dto.getPrecio().setScale(0,RoundingMode.HALF_UP).intValue();

        if (precioCasteado < 0) {
            throw new PrecioNegativoException();
        }

        Producto prod = getProductoById(id);
        prod.setNombre(dto.getNombre());
        prod.setPrecio(dto.getPrecio());
        prod.setStock(dto.getStock());
        Producto updated = productoRepository.save(prod);
        ProductoResponseDTO response = new ProductoResponseDTO();
        response.setId(updated.getId());
        response.setNombre(updated.getNombre());
        response.setDescripcion(updated.getDescripcion());
        response.setPrecio(updated.getPrecio());
        response.setStock(updated.getStock());
        return response;
    }

    public ProductoResponseDTO saveProducto(ProductoRequestDTO producto) {
        Integer precioCasteado = producto.getPrecio().setScale(0,RoundingMode.HALF_UP).intValue();

        if (precioCasteado < 0) {
            throw new PrecioNegativoException();
        }

        Producto prod = productoRepository.save(Producto.builder()
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .imagen(producto.getImagen())
                .build());
        ProductoResponseDTO prodDTO = new ProductoResponseDTO();
        prodDTO.setId(prod.getId());
        prodDTO.setNombre(prod.getNombre());
        prodDTO.setDescripcion(prod.getDescripcion());
        prodDTO.setPrecio(prod.getPrecio());
        prodDTO.setStock(prod.getStock());
        prodDTO.setImagen(prod.getImagen());

        return prodDTO;

    }


}