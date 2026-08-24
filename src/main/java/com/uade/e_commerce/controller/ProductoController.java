package com.uade.e_commerce.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce.dto.ProductoRequestDTO;
import com.uade.e_commerce.dto.ProductoResponseDTO;
import com.uade.e_commerce.model.Producto;
import com.uade.e_commerce.service.ProductoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.uade.e_commerce.dto.ProductoUpdateDTO;



/**
 * Encargado de recibir request http desde los clientes
 * y devolver respuestas http con los datos solicitados
 * ProductoController
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {
 
    
    private final ProductoService productoService;

    ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    //get http://localhost:8080/api/productos -> listar todos productos
    @GetMapping
    public List<Producto> getAllProductos() {
        // Lógica para obtener todos los productos
        return productoService.getAllProductos();
    }

    //get http://localhost:8080/api/productos/1 
    @GetMapping("/{id}")
    public Producto getProductoById(@PathVariable Long id) {
        return productoService.getProductoById(id);
    }

    //borra el producto 1 delete http://localhost:8080/api/productos/1 
    @DeleteMapping("/{id}")
    public void deleteProducto(@PathVariable Long id) { 
        productoService.deleteProducto(id);
    }

    // crear un producto nuevo
    // post http://localhost:8080/api/productos (enviar body con datos del producto)
    @PostMapping()
    public ProductoResponseDTO saveProducto(@RequestBody ProductoRequestDTO producto) {
        return productoService.saveProducto(producto);
    }

    @PutMapping("/{id}")
    public ProductoResponseDTO updateProducto(@PathVariable Long id, @RequestBody ProductoUpdateDTO dto) {
        return productoService.updateProducto(id, dto);
    }

}
