package by.matrosov.demodemo.controller;

import by.matrosov.demodemo.model.ChatMessage;
import by.matrosov.demodemo.model.DrawMessage;
import by.matrosov.demodemo.service.game.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;

import static java.lang.String.format;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private GameService gameService;

    @MessageMapping("/chat/{roomId}/sendMessage")
    public void sendMessage(@DestinationVariable String roomId, @Payload ChatMessage chatMessage) {
        messagingTemplate.convertAndSend(format("/topic/%s/public", roomId), chatMessage);
    }

    @MessageMapping("/chat/{roomId}/changeDrawUser")
    public void changeDrawUser(@DestinationVariable String roomId, @Payload ChatMessage chatMessage, Principal principal){

        //get prev user
        String prevUser = chatMessage.getContent();

        //send message to the chat
        messagingTemplate.convertAndSend(format("/topic/%s/public", roomId), chatMessage);

        //send modal window
        chatMessage.setContent("first,second,third");
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/sendModal", chatMessage);

        //set prev user to disable canvas
        if (prevUser != null){
            messagingTemplate.convertAndSendToUser(prevUser, "/queue/canvas", chatMessage);

            //add score
            gameService.addScore(prevUser, principal.getName(), roomId);
            gameService.print();
        }

        //take next user from map && replace current on next
        //if prev = null -> get[0]

        // set current user to DRAWING
        messagingTemplate.convertAndSend(format("/topic/%s/changeDrawUser", roomId), chatMessage);

        //set current user to enable canvas
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/canvas", chatMessage);
    }

    @MessageMapping("/chat/{roomId}/addUser")
    public void addUser(@DestinationVariable String roomId, @Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String currentRoomId = (String) headerAccessor.getSessionAttributes().put("room_id", roomId);
        if (currentRoomId != null) {
            ChatMessage leaveMessage = new ChatMessage();
            leaveMessage.setType(ChatMessage.MessageType.LEAVE);
            leaveMessage.setSender(chatMessage.getSender());
            messagingTemplate.convertAndSend(format("/topic/%s/public", currentRoomId), leaveMessage);
        }
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        messagingTemplate.convertAndSend(format("/topic/%s/public", roomId), chatMessage);
    }

    @MessageMapping("/chat/{roomId}/changeGuess")
    public void changeGuess(@DestinationVariable String roomId, @Payload ChatMessage chatMessage){
        messagingTemplate.convertAndSend(format("/topic/%s/changeGuess", roomId), chatMessage);
    }

    @MessageMapping("/chat/{roomId}/draw")
    public void draw(@DestinationVariable String roomId, @Payload ChatMessage chatMessage){
        DrawMessage drawMessage = new DrawMessage();
        drawMessage.setSender(chatMessage.getSender());
        drawMessage.setX1(Float.parseFloat(chatMessage.getContent().split("#")[0].split(",")[0]));
        drawMessage.setY1(Float.parseFloat(chatMessage.getContent().split("#")[0].split(",")[1]));
        drawMessage.setX2(Float.parseFloat(chatMessage.getContent().split("#")[1].split(",")[0]));
        drawMessage.setY2(Float.parseFloat(chatMessage.getContent().split("#")[1].split(",")[1]));

        messagingTemplate.convertAndSend(format("/topic/%s/draw", roomId), drawMessage);
    }
}
