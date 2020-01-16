package com.codeperfection.shipit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageDto<T> {

    private long totalElements;

    private int totalPages;

    private Collection<T> elements;
}
