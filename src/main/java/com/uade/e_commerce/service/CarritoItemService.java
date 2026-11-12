package com.uade.e_commerce.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.e_commerce.dto.CarritoItemResponseDTO;
import com.uade.e_commerce.exceptions.CantidadInvalidaException;
import com.uade.e_commerce.exceptions.RecursoNoEncontradoException;
import com.uade.e_commerce.model.CarritoItem;
import com.uade.e_commerce.repository.CarritoItemRepository;

@Service
public class CarritoItemService {

    @Autowired
    private CarritoItemRepository carritoItemRepository;

    public BigDecimal calcularSubtotal(CarritoItem item) {
        return item.getProducto().getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
    }

    public CarritoItem obtenerPorId(Long itemId) {
        return carritoItemRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ítem no encontrado"));
    }

    // Cambia la cantidad de un item. Lanza CantidadInvalidaException (400) si es menor a 1 o supera el stock.
    // Para sacar un producto del carrito se usa el DELETE, no cantidad 0.
    public CarritoItem actualizarCantidad(Long itemId, Integer nuevaCantidad) {
        CarritoItem item = obtenerPorId(itemId);
        if (nuevaCantidad == null || nuevaCantidad < 1) {
            throw new CantidadInvalidaException("La cantidad tiene que ser al menos 1");
        }
        if (nuevaCantidad > item.getProducto().getStock()) {
            throw new CantidadInvalidaException("Stock insuficiente para " + item.getProducto().getNombre()
                    + ": quedan " + item.getProducto().getStock() + " unidades");
        }
        item.setCantidad(nuevaCantidad);
        return carritoItemRepository.save(item);
    }

    public List<CarritoItem> obtenerTodos (){
        return carritoItemRepository.findAll();
    }

    public void eliminarItem(Long itemId) {
        carritoItemRepository.deleteById(itemId);
    }

    public CarritoItemResponseDTO convertirADTO(CarritoItem item) {
        CarritoItemResponseDTO dto = new CarritoItemResponseDTO();
        dto.setId(item.getId());
        dto.setProductoId(item.getProducto().getId());
        dto.setNombreProducto(item.getProducto().getNombre());
        dto.setPrecioUnitario(item.getProducto().getPrecio());
        dto.setCantidad(item.getCantidad());
        dto.setSubtotal(calcularSubtotal(item));
        return dto;
    }
}
