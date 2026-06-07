package com.example.demo.controller;

import com.example.demo.application.customer.dto.CustomerResponse;
import com.example.demo.application.customer.dto.RegisterCustomerRequest;
import com.example.demo.application.customer.dto.UpdateCustomerRequest;
import com.example.demo.application.port.CustomerUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 客户 Web 适配器 — 将 HTTP 请求适配到 CustomerUseCase 入站端口。
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerUseCase customerUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse register(@Valid @RequestBody RegisterCustomerRequest request) {
        return customerUseCase.register(request);
    }

    @GetMapping("/{id}")
    public CustomerResponse getById(@PathVariable Long id) {
        return customerUseCase.findById(id);
    }

    @PutMapping("/{id}")
    public CustomerResponse updateProfile(@PathVariable Long id,
                                          @Valid @RequestBody UpdateCustomerRequest request) {
        return customerUseCase.updateProfile(id, request);
    }
}
