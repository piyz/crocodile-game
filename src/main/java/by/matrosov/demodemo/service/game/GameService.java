package by.matrosov.demodemo.service.game;

import org.springframework.stereotype.Service;

@Service
public interface GameService {
    void addUser(String username, String roomid);
    void removeUser(String username, String roomid);
    void addScore(String drawer, String guesser, String roomid);
    void print();
}
