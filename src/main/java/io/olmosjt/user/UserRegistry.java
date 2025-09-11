package io.olmosjt.user;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Keeps connected users in {@link java.util.concurrent.ConcurrentHashMap} and tracks them.
 * <p>
 * Each username is unique and mapped to a {@link io.olmosjt.server.ClientHandler}.
 */
public class UserRegistry {
  private final ConcurrentMap<String, User> users = new ConcurrentHashMap<>();

  /**
   * Creates a new user with an initial status of {@link UserStatus#ONLINE} and sets the joined time to the current instant.
   *
   * @param username The unique username for the new user.
   * @return {@code true} if the user was successfully added; otherwise, {@code false}.
   */
  public boolean register(String username) {
    if (username == null || username.isBlank()) return false;

    String key = username.trim();
    User user = new User(key, UserStatus.ONLINE, Instant.now());
    return users.putIfAbsent(key, user) == null;
  }

  public void unregister(String username) {
    if (username == null) return;
    users.remove(username.trim());
  }

  public Optional<User> find(String username) {
    if (username == null) return Optional.empty();
    return Optional.ofNullable(users.get(username.trim()));
  }

  public boolean rename(String oldName, String newName) {
    if (oldName == null || newName == null) return false;

    String oldKey = oldName.trim();
    String newKey = newName.trim();

    if (newKey.isBlank() || users.containsKey(newKey)) return false;

    synchronized (this) {
      User oldUser = users.remove(oldKey);
      if (oldUser == null) return false;

      User renamed = new User(newKey, oldUser.status(), oldUser.joinedAt());
      users.put(newKey, renamed);
    }
    return true;
  }

  public void updateStatus(String username, UserStatus newStatus) {
    if (username == null || newStatus == null) return;

    users.computeIfPresent(username.trim(), (key, user) ->
            new User(user.username(), newStatus, user.joinedAt())
    );
  }

  public int size() {
    return users.size();
  }

  public Map<String, User> allUsers() {
    return Map.copyOf(users);
  }

}
