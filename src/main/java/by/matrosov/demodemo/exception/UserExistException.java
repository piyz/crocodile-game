package by.matrosov.demodemo.exception;

public class UserExistException extends Throwable{
    public UserExistException(String message) {
        System.out.println(message);
    }
}
