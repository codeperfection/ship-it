package com.codeperfection.shipit.util;

import com.codeperfection.shipit.dto.PaginationFilterDto;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class TestUtil {

    public static MultiValueMap<String, String> toMultiValueMap(PaginationFilterDto paginationFilterDto) {
        final var multiValueMap = new LinkedMultiValueMap<String, String>();
        multiValueMap.add("page", paginationFilterDto.getPage().toString());
        multiValueMap.add("size", paginationFilterDto.getSize().toString());
        return multiValueMap;
    }
}
