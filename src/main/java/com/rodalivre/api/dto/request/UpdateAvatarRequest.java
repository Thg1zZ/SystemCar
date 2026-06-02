package com.rodalivre.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAvatarRequest {
    
    @NotBlank(message = "A imagem do avatar é obrigatória")
    private String avatar;
}
