package com.rodalivre.api.validation;

import com.rodalivre.util.Validador;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CPFValidator implements ConstraintValidator<CPFValid, String> {

    @Override
    public void initialize(CPFValid constraintAnnotation) {
    }

    @Override
    public boolean isValid(String cpfField, ConstraintValidatorContext context) {
        if (cpfField == null || cpfField.isBlank()) {
            return true; // Deixa para o @NotBlank validar campos nulos/vazios
        }
        return Validador.isCpfValido(cpfField);
    }
}
