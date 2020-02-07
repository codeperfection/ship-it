package com.codeperfection.shipit.dto.transporter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransporterDto {

    private UUID uuid;

    private String name;

    private Integer capacity;
}
