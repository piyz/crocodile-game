package by.matrosov.demodemo.service.user;

import by.matrosov.demodemo.exception.UserExistException;
import by.matrosov.demodemo.model.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    User registerNewUserAccount(User user) throws UserExistException;
}
