package com.muGood.common.api;

import java.util.List;

public record PageResult<T>(List<T> items, long counts, long page, long pageSize) {
}
