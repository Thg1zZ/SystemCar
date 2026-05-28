package com.rodalivre.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PlacaValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PlacaValid {
    String message() default "Placa do veículo inválida (Use o padrão clássico AAA-9999 ou Mercosul AAA9A99)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
