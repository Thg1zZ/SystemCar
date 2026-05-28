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
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    private String password;

    @NotBlank(message = "CPF é obrigatório")
    @CPFValid(message = "CPF inválido. Certifique-se de digitar um CPF com dígitos verificadores corretos.")
    private String cpf;

    @NotBlank(message = "CNH é obrigatória")
    @Size(min = 11, max = 11, message = "A CNH deve ter exatamente 11 dígitos")
    private String cnh;

    @NotNull(message = "Data de vencimento da CNH é obrigatória")
    private LocalDate cnhExpirationDate;

    private String phone;

    @NotNull(message = "Data de nascimento é obrigatória")
    private LocalDate birthDate;
}
