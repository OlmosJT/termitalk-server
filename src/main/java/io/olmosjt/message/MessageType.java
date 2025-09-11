package io.olmosjt.message;

public enum MessageType {
  SYSTEM,    // server/system notifications
  USER,      // normal chat messages in rooms
  PRIVATE,   // private messages between users
  ERROR,     // error messages
  COMMAND    // commands sent from client to server
}
