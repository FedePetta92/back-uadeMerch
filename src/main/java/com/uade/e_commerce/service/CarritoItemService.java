package com.uade.e_commerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.e_commerce.dto.CarritoItemResponseDTO;
import com.uade.e_commerce.exceptions.RecursoNoEncontradoException;
import com.uade.e_commerce.model.CarritoItem;
import com.uade.e_commerce.repository.CarritoItemRepository;

@Service
public class CarritoItemService {

    @Autowired
    private CarritoItemRepository carritoItemRepository;

    public Double calcularSubtotal(CarritoItem item) {
        return item.getProducto().getPrecio() * item.getCantidad();
    }

    public CarritoItem obtenerPorId(Long itemId) {
        return carritoItemRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ítem no encontrado"));
    }

    public CarritoItem actualizarCantidad(Long itemId, Integer nuevaCantidad) {
        CarritoItem item = obtenerPorId(itemId);
        item.setCantidad(nuevaCantidad);
        return carritoItemRepository.save(item);
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
