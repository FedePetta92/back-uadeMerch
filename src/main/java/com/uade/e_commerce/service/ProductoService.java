package com.uade.e_commerce.service;

import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.e_commerce.dto.ProductoRequestDTO;
import com.uade.e_commerce.dto.ProductoResponseDTO;
import com.uade.e_commerce.dto.ProductoUpdateDTO;
import com.uade.e_commerce.exceptions.PrecioNegativoException;
import com.uade.e_commerce.exceptions.RecursoNoEncontradoException;
import com.uade.e_commerce.model.Producto;
import com.uade.e_commerce.repository.ProductoRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    

    public List<Producto> getAllProductos() {
        return productoRepository.findAll();
    }

    public Producto getProductoById(Long id) {
        return productoRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
    }
    
    public void deleteProducto(Long id) {
        productoRepository.deleteById(id);
    }

    public ProductoResponseDTO updateProducto(Long id, ProductoUpdateDTO dto) {
        Integer precioCasteado = dto.getPrecio().setScale(0,RoundingMode.HALF_UP).intValue();

        if (precioCasteado < 0) {
            throw new PrecioNegativoException();
        }

        Producto prod = productoRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
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
