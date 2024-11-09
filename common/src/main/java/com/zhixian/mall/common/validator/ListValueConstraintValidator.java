package com.zhixian.mall.common.validator;

import com.zhixian.mall.common.validator.annotation.ListValue;

import javax.validation.ConstraintValidator;
import java.util.HashSet;
import java.util.Set;

public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {

    private Set<Integer> set = new HashSet<>();

    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] vals = constraintAnnotation.vals();
        for (int val : vals) {
            set.add(val);
        }
    }

    @Override
    public boolean isValid(Integer value, javax.validation.ConstraintValidatorContext context) {
        return set.contains(value);
    }
}
