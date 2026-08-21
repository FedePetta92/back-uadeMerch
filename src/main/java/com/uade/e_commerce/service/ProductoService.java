package com.uade.e_commerce.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.uade.e_commerce.dto.ProductoRequestDTO;
import com.uade.e_commerce.dto.ProductoResponseDTO;
import com.uade.e_commerce.model.Producto;
import com.uade.e_commerce.repository.ProductoRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductoService {

    
    private final ProductoRepository productoRepository;

    ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> getAllProductos() {
        return productoRepository.findAll();
    }

    public Producto getProductoById(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public void deleteProducto(Long id) {
        productoRepository.deleteById(id);
    }

    public ProductoResponseDTO saveProducto(ProductoRequestDTO producto) {
        Producto prod = productoRepository.save(new Producto(null, producto.getNombre(), producto.getDescripcion(), producto.getPrecio(), producto.getStock(), null));    
        ProductoResponseDTO prodDTO = new ProductoResponseDTO();
        prodDTO.setId(prod.getId());
        prodDTO.setNombre(prod.getNombre());
        prodDTO.setDescripcion(prod.getDescripcion());
        prodDTO.setPrecio(prod.getPrecio());
        prodDTO.setStock(prod.getStock());
        
        return prodDTO;

    }
     
    
}
