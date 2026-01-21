package com.automation.zepto.controller;

import com.automation.zepto.dto.OrderRequest;
import com.automation.zepto.service.ZeptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private ZeptoService zeptoService;

    @PostMapping
    public ResponseEntity<java.util.Map<String, Object>> placeOrder(@RequestBody OrderRequest request) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            String result = zeptoService.executeOrder(request);
            response.put("status", "success");
            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Order failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
