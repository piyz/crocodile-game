package by.matrosov.demodemo.listener;

import by.matrosov.demodemo.model.ChatMessage;
import by.matrosov.demodemo.service.game.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import static java.lang.String.format;

@Component
public class WebSocketEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private GameService gameService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new web socket connection");
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String destination = headerAccessor.getDestination();
        if (destination.contains("public")){
            String username = headerAccessor.getUser().getName();
            String roomid = destination.split("/")[2];
            gameService.addUser(username, roomid);
            //gameService.print();
        }

        //System.out.println(headerAccessor.getSessionId());
        //System.out.println(headerAccessor.getSubscriptionId());
        //System.out.println(headerAccessor.getDestination());
        //System.out.println(headerAccessor.getUser().getName());
        // pwtgtupt
        // sub-0
        // /topic/2/public
        // user
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        if (headerAccessor.getSubscriptionId().contains("sub-4")){
            String username = (String) headerAccessor.getSessionAttributes().get("username");
            String roomId = (String) headerAccessor.getSessionAttributes().get("room_id");

            //remove from the map
            gameService.removeUser(username, roomId);
            //gameService.print();

            //notify about left from the room
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setSender(username);
            messagingTemplate.convertAndSend(format("/topic/%s/public", roomId), chatMessage);

            //remove roomId after left the room
            headerAccessor.getSessionAttributes().remove("room_id");
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String roomId = (String) headerAccessor.getSessionAttributes().get("room_id");
        if (username != null) {
            logger.info("User Disconnected: " + username);

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setSender(username);

            messagingTemplate.convertAndSend(format("/topic/%s/public", roomId), chatMessage);
        }
    }
}