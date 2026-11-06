
package com.example.E_Commerce.dto;

import jakarta.validation.constraints.NotNull;

public class OrderRequest {

    @NotNull(message = "User must be authenticated")
    private Long userId;

    public OrderRequest() {}

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
