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
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;

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
    //get http://localhost:8080/api/productos?nombre=mate -> buscador: solo los que contienen "mate" en el nombre
    @GetMapping
    public List<Producto> getAllProductos(@RequestParam(required = false) String nombre) {
        return productoService.buscarProductos(nombre);
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
    // Solo ADMIN (SecurityConfig). @Valid devuelve 400 si falta el nombre, el precio o el stock.
    @PostMapping()
    public ProductoResponseDTO saveProducto(@Valid @RequestBody ProductoRequestDTO producto) {
        return productoService.saveProducto(producto);
    }

    // Solo ADMIN (SecurityConfig).
    @PutMapping("/{id}")
    public ProductoResponseDTO updateProducto(@PathVariable Long id, @Valid @RequestBody ProductoUpdateDTO dto) {
        return productoService.updateProducto(id, dto);
    }

}
