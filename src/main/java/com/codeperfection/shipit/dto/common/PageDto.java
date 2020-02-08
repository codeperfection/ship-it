package com.codeperfection.shipit.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
public class PageDto<T> {

    private long totalElements;

    private int totalPages;

    private Collection<T> elements;
}
