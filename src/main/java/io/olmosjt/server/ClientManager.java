package io.olmosjt.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of active client connections in a thread-safe {@link java.util.concurrent.ConcurrentHashMap}.
 * <p>
 * Each username is unique and mapped to its active {@link io.olmosjt.server.ClientHandler}.
 * <p>
 * This class is responsible only for connection management, not user identity.
 * Use {@link io.olmosjt.user.UserRegistry} for persistent user metadata.
 */

public class ClientManager {
  private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

  /**
   * Registers a new client handler for a given username.
   * @param username The unique username.
   * @param handler The client's handler.
   */
  public void register(String username, ClientHandler handler) {
    if (username == null || handler == null) return;
    clients.put(username, handler);
  }

  /**
   * Removes a client from the active list.
   * @param username The username to unregister.
   */
  public void unregister(String username) {
    if (username == null) return;
    clients.remove(username);
  }

  /**
   * Retrieves a client handler by username.
   * @param username The username to look up.
   * @return The ClientHandler, or null if not found.
   */
  public ClientHandler get(String username) {
    if (username == null) return null;
    return clients.get(username);
  }

  /**
   * Checks if a user is currently online.
   * @param username The username to check.
   * @return true if the user is connected, false otherwise.
   */
  public boolean isOnline(String username) {
    return clients.containsKey(username);
  }

  /**
   * @return An iterable collection of all currently connected client handlers.
   */
  public Iterable<ClientHandler> allClients() {
    return clients.values();
  }

}
