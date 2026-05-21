package com.weekify.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 64;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext content){
        // null 이나 빈 값 검증은 이미 기본 Bean Validator의 @NotBlank가 담당하기 떄문에 true 반환
        if(password == null || password.isBlank())return true;

        if(password.length() < MIN_LENGTH || password.length() > MAX_LENGTH)return false;

        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecialCharacter = password.chars()
                .anyMatch(ch -> !Character.isLetterOrDigit(ch));

        return hasLetter && hasDigit && hasSpecialCharacter;
    }
}
