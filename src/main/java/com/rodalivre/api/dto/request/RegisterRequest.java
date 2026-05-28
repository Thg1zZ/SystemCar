package com.rodalivre.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    private String password;

    @NotBlank(message = "CPF é obrigatório")
    private String cpf;

    @NotBlank(message = "CNH é obrigatória")
    private String cnh;

    @NotNull(message = "Data de vencimento da CNH é obrigatória")
    private LocalDate cnhExpirationDate;

    private String phone;

    @NotNull(message = "Data de nascimento é obrigatória")
    private LocalDate birthDate;
}
