package by.matrosov.demodemo.validation;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UsernameValidator implements ConstraintValidator<ValidUsername,String> {
    @Override
    public void initialize(ValidUsername constraintAnnotation) {

    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return validateUsername(s);
    }

    private boolean validateUsername(String username){
        return username.length() >= 3;
    }
}
