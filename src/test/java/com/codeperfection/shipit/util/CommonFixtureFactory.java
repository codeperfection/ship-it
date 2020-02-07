package com.codeperfection.shipit.util;

import com.codeperfection.shipit.dto.common.PageDto;
import com.codeperfection.shipit.dto.common.PaginationFilterDto;

import java.util.List;

public class CommonFixtureFactory {

    public static PaginationFilterDto createPaginationFilterDto() {
        return new PaginationFilterDto(0, 1);
    }

    public static <T> PageDto<T> createPageDto(T item) {
        return new PageDto<>(1, 1, List.of(item));
    }
}
