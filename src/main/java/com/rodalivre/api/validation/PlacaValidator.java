package com.rodalivre.api.validation;

import com.rodalivre.util.Validador;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PlacaValidator implements ConstraintValidator<PlacaValid, String> {

    @Override
    public void initialize(PlacaValid constraintAnnotation) {
    }

    @Override
    public boolean isValid(String placaField, ConstraintValidatorContext context) {
        if (placaField == null || placaField.isBlank()) {
            return true; // Deixa para o @NotBlank validar
        }
        return Validador.isPlacaValida(placaField);
    }
}
