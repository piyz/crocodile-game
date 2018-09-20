package by.matrosov.demodemo.service.rooms;

import by.matrosov.demodemo.model.Room;
import by.matrosov.demodemo.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomServiceImpl implements RoomService{

    @Autowired
    private RoomRepository roomRepository;

    @Override
    public List<Room> getRooms() {
        return roomRepository.findAll();
    }

    @Override
    public void changeRoomState(int roomid) {
        Room room2update = roomRepository.getRoomByRoomId(roomid);

        if (room2update.isOpen()){
            room2update.setOpen(false);
        }else {
            room2update.setOpen(true);
        }

        roomRepository.save(room2update);
    }
}
