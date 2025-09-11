package io.olmosjt.command;

import io.olmosjt.ServerContext;
import io.olmosjt.server.ClientHandler;

/**
 * Represents a single executable command from a client.
 * <p>
 * Each command knows how to execute itself, given:
 * <ul>
 * <li>The server context (for access to user registry, dispatcher, etc.)</li>
 * <li>The client who sent it</li>
 * <li>The raw argument payload (text after the command keyword)</li>
 * </ul>
 */
@FunctionalInterface
public interface Command {
  void execute(ServerContext context, ClientHandler client, String payload);
}
