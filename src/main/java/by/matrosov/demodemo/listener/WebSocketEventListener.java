package by.matrosov.demodemo.listener;

import by.matrosov.demodemo.model.ChatMessage;
import by.matrosov.demodemo.repository.RoomRepository;
import by.matrosov.demodemo.service.GameService;
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

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new web socket connection");
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        /*
        if (!headerAccessor.getDestination().contains("choose")){
            String roomid = headerAccessor.getDestination().split("/")[2];
            ChatMessage chatMessage = new ChatMessage();
            Random random = new Random();
            chatMessage.setContent(String.valueOf(random.nextInt(66)));

            messagingTemplate.convertAndSend(format("/topic/choose/%s", roomid), chatMessage);
        }
         */

        //System.out.println(headerAccessor.getSessionId());
        //System.out.println(headerAccessor.getSubscriptionId());
        //System.out.println(headerAccessor.getDestination());
        //System.out.println(headerAccessor.getUser().getName());
        // pwtgtupt
        // sub-0
        // /topic/2
        // user
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        //notify about left from the room
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String roomId = (String) headerAccessor.getSessionAttributes().get("room_id");
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(ChatMessage.MessageType.LEAVE);
        chatMessage.setSender(username);
        messagingTemplate.convertAndSend(format("/topic/%s", roomId), chatMessage);

        //remove roomId after left the room
        headerAccessor.getSessionAttributes().remove("room_id");
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

            messagingTemplate.convertAndSend(format("/topic/%s", roomId), chatMessage);
        }
    }
}