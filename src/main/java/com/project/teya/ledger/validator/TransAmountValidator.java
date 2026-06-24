package com.project.teya.ledger.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class TransAmountValidator implements ConstraintValidator<ValidAmount, BigDecimal> {

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        return true;
    }
}
