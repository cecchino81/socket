package it.socket.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    // Mappa per tenere traccia delle sessioni e dei nomi degli utenti
    private static final Map<WebSocketSession, String> userSessions = new HashMap<>();
    private static final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Recupera il nome dell'utente dalla query string
        String username = session.getUri().getQuery().split("=")[1]; // Estrae il parametro 'username' dalla query string
        userSessions.put(session, username); // Associa il nome alla sessione
        sessions.add(session);
        
        // Invia un messaggio di benvenuto al client
        session.sendMessage(new TextMessage("🔹 Ciao " + username + ", sei connesso al WebSocket Server!"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String receivedMessage = message.getPayload(); // Riceve il messaggio dal client
        String senderName = userSessions.get(session); // Recupera il nome del client dalla sessione
        
        System.out.println("📩 Messaggio ricevuto da " + senderName + ": " + receivedMessage);

        // Invia il messaggio a tutti i client connessi tranne quello che lo ha inviato
        for (WebSocketSession s : sessions) {
            if (s.isOpen() && !s.equals(session)) {  // Non inviare al client che ha inviato il messaggio
                String recipientName = userSessions.get(s); // Nome del destinatario
                s.sendMessage(new TextMessage(senderName + ": " + receivedMessage));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String username = userSessions.remove(session); // Rimuove la sessione dalla mappa
        sessions.remove(session); // Rimuove la sessione dalla lista
        System.out.println("❌ " + username + " ha chiuso la connessione.");
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // Gestione degli errori durante la comunicazione WebSocket
        String username = userSessions.get(session);
        System.err.println("❌ Errore di trasporto per " + username + ": " + exception.getMessage());
    }
}