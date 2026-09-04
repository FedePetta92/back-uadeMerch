package com.uade.e_commerce.service;

import org.springframework.stereotype.Service;
import com.uade.e_commerce.model.CarritoItem;

@Service
public class CarritoItemService {

    public Double calcularSubtotal(CarritoItem item) {
        return item.getProducto().getPrecio() * item.getCantidad();
    }
}
