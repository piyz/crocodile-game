package by.matrosov.demodemo.service.rooms;

import by.matrosov.demodemo.model.Room;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RoomService {
    List<Room> getRooms();
}
