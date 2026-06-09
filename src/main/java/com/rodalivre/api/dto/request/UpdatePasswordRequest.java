package com.rodalivre.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePasswordRequest {
    
    @NotBlank(message = "A senha atual é obrigatória")
    private String oldPassword;
    
    @NotBlank(message = "A nova senha é obrigatória")
    @jakarta.validation.constraints.Size(min = 12, message = "A nova senha deve ter pelo menos 12 caracteres")
    @jakarta.validation.constraints.Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{12,}$", 
             message = "A nova senha deve ter pelo menos 12 caracteres, incluindo uma letra maiúscula, um número e um caractere especial.")
    private String newPassword;
}
