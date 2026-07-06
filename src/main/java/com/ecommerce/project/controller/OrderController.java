package com.ecommerce.project.controller;

import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderRequestDTO;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final AuthUtils authUtil;
    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> orderProducts(@PathVariable String paymentMethod, @RequestBody  OrderRequestDTO orderRequestDTO){
        String emailId = authUtil.loggedInEmail();
        OrderDTO order = orderService.placeOrder(emailId,orderRequestDTO.getAddressId(),paymentMethod,orderRequestDTO.getPgName(),orderRequestDTO.getPgPaymentId(),orderRequestDTO.getPgStatus(),orderRequestDTO.getPgResponseMessage());

        return new ResponseEntity<>(order, HttpStatus.CREATED);

    }
}
