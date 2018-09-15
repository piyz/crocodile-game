package by.matrosov.demodemo.service.game;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class GameServiceImpl implements GameService{

    private static Map<String, Map<String, Integer>> mapMap = Collections.synchronizedMap(new HashMap<>());

    @Override
    public synchronized void addUser(String username, String roomid) {
        Map<String, Integer> innerMap = mapMap.get(roomid);

        if (innerMap == null){
            innerMap = new LinkedHashMap<>();
            innerMap.put(username, 0);
            mapMap.put(roomid, innerMap);
        }else {
            innerMap.put(username, 0);
        }
    }

    @Override
    public synchronized void removeUser(String username, String roomid) {
        Map<String, Integer> innerMap = mapMap.get(roomid);

        innerMap.remove(username);
    }

    @Override
    public synchronized void addScore(String drawer, String guesser, String roomid) {
        Map<String, Integer> innerMap = mapMap.get(roomid);

        innerMap.put(drawer, innerMap.get(drawer) + 5);
        innerMap.put(guesser, innerMap.get(guesser) + 6);
    }

    @Override
    public synchronized void print() {
        mapMap.entrySet().forEach(System.out::println);
    }
}
