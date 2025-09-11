package io.olmosjt.room;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Manages all chat rooms on the server.
 * This class is thread-safe.
 */
public class ChatRoomManager {
  private final ConcurrentMap<Integer, ChatRoom> rooms = new ConcurrentHashMap<>();
  private final AtomicInteger roomIdCounter = new AtomicInteger(100);

  /**
   * Creates a new chat room with a unique ID.
   * @param name The non-unique name of the room.
   * @return The newly created ChatRoom instance.
   */
  public ChatRoom createRoom(String name) {
    if (name == null || name.isBlank()) return null;
    int newId = roomIdCounter.getAndIncrement();
    ChatRoom newRoom = new ChatRoom(newId, name);
    rooms.put(newId, newRoom);
    return newRoom;
  }

  /**
   * Finds a chat room by its unique ID.
   * @param id The unique ID of the room.
   * @return An Optional containing the ChatRoom if found.
   */
  public Optional<ChatRoom> getRoom(int id) {
    return Optional.ofNullable(rooms.get(id));
  }

  /**
   * @return A List of strings representing all rooms in the format "[ID] Name".
   */
  public Set<String> listRoomNames() {
    return rooms.values().stream()
            .map(room -> String.format("[#%d] %s", room.getId(), room.getName()))
            .collect(Collectors.toSet());
  }

}
