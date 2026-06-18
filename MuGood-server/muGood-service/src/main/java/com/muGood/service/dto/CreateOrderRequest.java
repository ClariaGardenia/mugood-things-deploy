package com.muGood.service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRequest(
        Integer deliveryTimeType,
        Integer payType,
        Integer payChannel,
        String buyerMessage,
        @NotEmpty List<OrderGoodsRequest> goods,
        @NotNull Long addressId
) {
    public record OrderGoodsRequest(@NotNull Long skuId, @NotNull Integer count) {
    }
}
