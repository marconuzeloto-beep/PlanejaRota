package com.logicore.execution.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logicore.execution.application.usecase.LiveExecutionService.LiveExecutionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket handler for live route execution updates.
 * Sessions are grouped by organizationId extracted from the URL path:
 * ws://host/ws/live/{organizationId}
 */
@Component
public class LiveMapWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(LiveMapWebSocketHandler.class);

    private final Map<UUID, Set<WebSocketSession>> sessionsByOrg = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public LiveMapWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        UUID orgId = extractOrgId(session);
        if (orgId != null) {
            sessionsByOrg.computeIfAbsent(orgId, k -> new CopyOnWriteArraySet<>()).add(session);
            log.debug("WebSocket connected: session={} org={}", session.getId(), orgId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UUID orgId = extractOrgId(session);
        if (orgId != null) {
            Set<WebSocketSession> sessions = sessionsByOrg.get(orgId);
            if (sessions != null) sessions.remove(session);
        }
    }

    public void broadcastUpdate(UUID organizationId, LiveExecutionDTO dto) {
        Set<WebSocketSession> sessions = sessionsByOrg.getOrDefault(organizationId, Set.of());
        if (sessions.isEmpty()) return;
        try {
            String json = objectMapper.writeValueAsString(Map.of("type", "EXECUTION_UPDATE", "data", dto));
            TextMessage message = new TextMessage(json);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (Exception ex) {
                        log.warn("Falha ao enviar WebSocket para session {}: {}", session.getId(), ex.getMessage());
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Falha ao serializar atualização WebSocket: {}", ex.getMessage(), ex);
        }
    }

    private static UUID extractOrgId(WebSocketSession session) {
        String path = session.getUri() != null ? session.getUri().getPath() : null;
        if (path == null) return null;
        String[] parts = path.split("/");
        try {
            return UUID.fromString(parts[parts.length - 1]);
        } catch (Exception e) {
            return null;
        }
    }
}
