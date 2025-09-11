package io.olmosjt.room;

import io.olmosjt.message.Message;
import io.olmosjt.server.ClientHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Represents a single chat room, holding a set of connected clients.
 * This class is thread-safe.
 */
public class ChatRoom {
  private final int id;
  private final String name;
  private final Set<ClientHandler> members = ConcurrentHashMap.newKeySet();


  public ChatRoom(int id, String name) {
    this.id = id;
    this.name = name;
  }

  /**
   * Adds a client to the room and notifies other members.
   * @param client The client to add.
   */
  public void addMember(ClientHandler client) {
    members.add(client);
    broadcast(Message.system("'" + client.getUser().username() + "' has joined the room."));
  }

  /**
   * Removes a client from the room and notifies other members.
   * @param client The client to remove.
   */
  public void removeMember(ClientHandler client) {
    boolean removed = members.remove(client);
    if (removed) {
      broadcast(Message.system("'" + client.getUser().username() + "' has left the room."));
    }
  }

  /**
   * Sends a message to all members of the room.
   * @param message The message to send.
   */
  public void broadcast(Message message) {
    for (ClientHandler member : members) {
      member.send(message);
    }
  }

  /**
   * Gets a list of usernames of all members in the room.
   * @return A set of member usernames.
   */
  public Set<String> getMemberNames() {
    return members.stream()
            .filter(ClientHandler::isLoggedIn)
            .map(client -> client.getUser().username())
            .collect(Collectors.toSet());
  }

  public String getName() {
    return name;
  }

  public int getId() {
    return id;
  }
}
