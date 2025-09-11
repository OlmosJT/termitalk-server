package io.olmosjt.user;

import java.time.Instant;
import java.util.Objects;

public record User(
        String username, // TIP: unique key
        UserStatus status,
        Instant joinedAt
) {

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof User user)) return false;
    return Objects.equals(username, user.username);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(username);
  }
}
