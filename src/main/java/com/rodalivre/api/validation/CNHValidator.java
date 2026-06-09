package com.rodalivre.api.validation;

import com.rodalivre.util.Validador;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CNHValidator implements ConstraintValidator<CNHValid, String> {

    @Override
    public void initialize(CNHValid constraintAnnotation) {
    }

    @Override
    public boolean isValid(String cnhField, ConstraintValidatorContext context) {
        if (cnhField == null || cnhField.isBlank()) {
            return true; // Deixa para o @NotBlank validar campos nulos/vazios
        }
        return Validador.isCnhValida(cnhField);
    }
}
