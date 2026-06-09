package com.rodalivre.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CNHValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CNHValid {
    String message() default "CNH inválida ou malformada";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
