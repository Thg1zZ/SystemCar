package com.rodalivre.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.rodalivre.api.validation.CPFValid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {
    @NotBlank(message = "Nome completo é obrigatório")
    private String fullName;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 12, message = "A senha deve ter pelo menos 12 caracteres")
    @jakarta.validation.constraints.Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{12,}$", 
             message = "A senha deve ter pelo menos 12 caracteres, incluindo uma letra maiúscula, um número e um caractere especial.")
    private String password;

    @NotBlank(message = "CPF é obrigatório")
    @CPFValid(message = "CPF inválido. Certifique-se de digitar um CPF com dígitos verificadores corretos.")
    private String cpf;

    @NotBlank(message = "CNH é obrigatória")
    @com.rodalivre.api.validation.CNHValid(message = "CNH inválida ou malformada")
    private String cnh;

    @NotNull(message = "Data de vencimento da CNH é obrigatória")
    private LocalDate cnhExpirationDate;

    private String phone;

    @NotNull(message = "Data de nascimento é obrigatória")
    private LocalDate birthDate;

    @NotNull(message = "O aceite dos termos é obrigatório")
    @jakarta.validation.constraints.AssertTrue(message = "Você precisa aceitar os termos de uso e política de privacidade")
    private Boolean termsAccepted;
}
