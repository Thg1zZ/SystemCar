package com.rodalivre.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePasswordRequest {
    
    @NotBlank(message = "A senha atual é obrigatória")
    private String oldPassword;
    
    @NotBlank(message = "A nova senha é obrigatória")
    private String newPassword;
}
