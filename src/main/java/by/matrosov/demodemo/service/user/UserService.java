package by.matrosov.demodemo.service.user;

import by.matrosov.demodemo.model.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    User findByUsername(String username);
    void save(User user);
}
