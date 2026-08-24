package com.uade.e_commerce.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.uade.e_commerce.dto.ProductoRequestDTO;
import com.uade.e_commerce.dto.ProductoResponseDTO;
import com.uade.e_commerce.dto.ProductoUpdateDTO;
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

    public ProductoResponseDTO updateProducto(Long id, ProductoUpdateDTO dto) {
        Producto prod = productoRepository.findById(id).orElse(null);
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
