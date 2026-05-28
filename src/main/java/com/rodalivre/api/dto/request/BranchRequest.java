package com.rodalivre.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BranchRequest {
    @NotBlank(message = "O nome da filial é obrigatório")
    private String name;

    private String street;
    private String city;
    private String state;
    private String zipCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
}
