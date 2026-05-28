package com.rodalivre.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CPFValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CPFValid {
    String message() default "CPF inválido ou malformado";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
