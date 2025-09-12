package io.olmosjt.command.cmds;

import io.olmosjt.ServerContext;
import io.olmosjt.command.Command;
import io.olmosjt.message.Message;
import io.olmosjt.server.ClientHandler;

public class PrivateMessageCommand implements Command {
  @Override
  public void execute(ServerContext context, ClientHandler client, String payload) {
    if (!client.isLoggedIn()) {
      client.send(Message.serverNok(client.getUser().username(),"You must be logged in to send private messages."));
      return;
    }
    String[] parts = payload == null ? new String[0] : payload.split(" ", 2);
    if (parts.length < 2 || parts[1].isBlank()) {
      client.send(Message.serverNok(client.getUser().username(),"Usage: REQ|PRIVMSG|<recipient> <message>"));
      return;
    }
    String recipient = parts[0];
    String content = parts[1];
    if (client.isLoggedIn() && recipient.equalsIgnoreCase(client.getUser().username())) {
      client.send(Message.serverNok(client.getUser().username(),"You cannot send a private message to yourself."));
      return;
    }
    Message privateMessage = Message.privateMsg(client.getUser().username(), recipient, content);
    context.dispatcher().dispatch(privateMessage);
  }
}
