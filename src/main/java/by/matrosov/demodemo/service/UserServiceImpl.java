package by.matrosov.demodemo.service;

import by.matrosov.demodemo.exception.UserExistException;
import by.matrosov.demodemo.model.User;
import by.matrosov.demodemo.repository.RoleRepository;
import by.matrosov.demodemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public User registerNewUserAccount(User user) throws UserExistException {
        if (userExist(user.getUsername())){
            throw new UserExistException(
                    "There is an account with that username:" + user.getUsername());
        }
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setUsername(user.getPassword());
        newUser.setRoles(new HashSet<>(Arrays.asList(roleRepository.findByRoleName("USER"))));
        return userRepository.save(newUser);
    }

    private boolean userExist(String username){
        User user = userRepository.findByUsername(username);
        return user != null;
    }
}
