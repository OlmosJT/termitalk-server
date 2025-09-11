package io.olmosjt;

import io.olmosjt.command.CommandHandler;
import io.olmosjt.message.MessageDispatcher;
import io.olmosjt.room.ChatRoomManager;
import io.olmosjt.server.ClientManager;
import io.olmosjt.user.UserRegistry;

public record ServerContext(
        UserRegistry userRegistry,
        ClientManager clientManager,
        MessageDispatcher dispatcher,
        CommandHandler commandHandler,
        ChatRoomManager roomManager
) {
}
