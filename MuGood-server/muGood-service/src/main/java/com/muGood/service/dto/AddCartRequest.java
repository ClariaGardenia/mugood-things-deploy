package com.muGood.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartRequest(
        @NotNull Long skuId,
        @NotNull
        @Min(1) Integer count
) {
}
