### TermiTalk (Server)

#### Status
Finalized as of Sep 13, 2025.

<img width="1713" height="891" alt="image" src="https://github.com/user-attachments/assets/3723371c-805f-4059-a85b-09ade33de1a8" />

---

### What is TermiTalk?
TermiTalk is a lightweight, line-oriented TCP chat server designed for terminal-based clients. It supports user logins, chat rooms, and private messaging. The protocol is simple text over TCP, making it easy to script and integrate.

---

### Key Features
- Line-oriented TCP protocol (UTF-8)
- Simple, explicit request/response framing
- User login and presence tracking
- Room creation, listing, join/leave, and room-scoped broadcasting
- Private (direct) messaging between users
- Helpful built-in HELP command
- Extensible command handler architecture
- Virtual-thread concurrency (Project Loom), highly scalable on JDK 21+

---

### Architecture Overview
- Entrypoint: `io.olmosjt.TermiTalkServer`
- Core context: `ServerContext` aggregates registries and services
  - `UserRegistry` – user metadata/presence (in-memory)
  - `ClientManager` – active connections by username
  - `MessageDispatcher` – routes messages (broadcast, room, private)
  - `CommandHandler` – parses and dispatches commands to handlers
  - `ChatRoomManager` – in-memory room registry (IDs auto-increment from 100)
- Per-connection worker: `ClientHandler` (runs in a virtual thread)
- Message model: `Message` with types `OK`, `NOK`, `SYSTEM`, `USER`, `PRIVATE`

On startup, a default room named `general` is created.

---

### Requirements
- Java 21 or newer (uses `Thread.startVirtualThread`)
- A terminal TCP client for testing (e.g., `nc`/`netcat`, `telnet`, or a custom client)

---

### Build and Run
This is a standard Java project. You can run it from your IDE or build it with your preferred tool.

- Main class: `io.olmosjt.TermiTalkServer`
- Default bind: `127.0.0.1:9000`

Example (from IDE):
- Run configuration → Main class: `io.olmosjt.TermiTalkServer`

Example (via command line after compilation):
- `java io.olmosjt.TermiTalkServer`

Configuration is provided by `ServerConfig.defaultConfig()` and currently hardcodes host/port. If you plan to externalize settings, add a custom `ServerConfig` and pass it to the `TermiTalkServer` constructor.

---

### Protocol

#### Transport
- TCP socket
- UTF-8, newline-delimited messages (one request or response per line)

#### Request format (Client → Server)
- Canonical format parsed by the server:
  - `REQ|COMMAND:PAYLOAD`
- Notes:
  - The `REQ|` prefix is mandatory for requests. e.g `REQ|LOGIN|<username>`
  - If a command has no payload, send `REQ|COMMAND|` or `REQ|COMMAND`.

Examples:
- `REQ|LOGIN|Alice`
- `REQ|LIST_ROOMS`
- `REQ|CREATE_ROOM|devs`
- `REQ|JOIN:100`
- `REQ|MSG|Hello room!`
- `REQ|PRIVMSG|Bob Hey Bob`
- `REQ|QUIT`

#### Response format (Server → Client)
- All responses/events are pipe-delimited with 4 fields:
  - `TYPE|SENDER|RECIPIENT|CONTENT`
- `TYPE` ∈ {`OK`, `NOK`, `SYSTEM`, `USER`, `PRIVATE`}
- `SENDER` is typically `SYSTEM` for `OK`/`NOK`/`SYSTEM` messages; for `USER`/`PRIVATE`, it is the username of the sender.
- `RECIPIENT` is a username only for private messages, otherwise empty (`''`).
- `CONTENT` is unescaped free text. Avoid sending `|` in your messages unless your client can handle it safely.

Examples:
- `OK|SYSTEM|Alice|Welcome, Alice!`
- `NOK|SYSTEM|Alice|You are not in a room.`
- `SYSTEM|SYSTEM||'Alice' has joined the room.`
- `USER|Alice||Hello everyone!`
- `PRIVATE|Alice|Bob Hey Bob`

Parsing tip: split inbound lines into at most 4 parts by the first three `|` delimiters; treat any extra `|` characters as part of the content.

---

### Commands
Send all commands as requests with the `REQ|` prefix.

- LOGIN
  - `REQ|LOGIN:<username>`
  - Username regex: `^[a-zA-Z0-9_]{3,15}$`
  - Errors: already logged in, invalid username, username taken
  - Success: you’ll receive an `OK` welcome; other users may see room/system notifications when you join rooms

- NICK
  - `REQ|NICK:<new_username>`
  - Changes your nickname (subject to the same validation and uniqueness). If not available/implemented in this version, server returns `NOK`.

- LIST_ROOMS
  - `REQ|LIST_ROOMS:`
  - Lists rooms as `[#<id>] <name>` joined by commas in the response content

- CREATE_ROOM
  - `REQ|CREATE_ROOM:<room_name>`
  - Room name regex: `^[a-zA-Z0-9_-]{3,20}$`
  - Creates a new room with a unique numeric ID

- JOIN
  - `REQ|JOIN:<room_id>`
  - Leaves user current room (if any) and joins the specified room

- LEAVE
  - `REQ|LEAVE:`
  - Leaves current room

- WHO
  - `REQ|WHO:`
  - Lists usernames in your current room

- MSG
  - `REQ|MSG:<text>`
  - Sends a message to current room; delivered as a `USER` response to room members

- PRIVMSG
  - `REQ|PRIVMSG:<recipient> <text>`
  - First space separates the recipient and the message body
  - Error if sending to self or recipient offline

- HELP
  - `REQ|HELP:`
  - Returns a list of supported commands and short descriptions

- QUIT
  - `REQ|QUIT:`
  - Cleanly disconnects you from the server

General rules:
- One command per line.
- Commands are case-insensitive (normalized to uppercase internally).
- Some commands require you to be logged in and/or be in a room.

---

### Rooms
- Rooms are identified by integer IDs, auto-incremented from 100.
- A default room named `general` is created at startup (typically ID 100 on a fresh run).
- Joining/leaving a room emits `SYSTEM`/`OK` notifications to room members.

---

### Example Session (with `nc`)
1) Connect
- `nc 127.0.0.1 9000`
- Server: `OK|SYSTEM||Welcome! Please log in with: REQ|LOGIN:<username>`

2) Login
- Client: `REQ|LOGIN:Alice`
- Server: `OK|SYSTEM|Alice|Welcome, Alice!`

3) List rooms
- Client: `REQ|LIST_ROOMS:`
- Server: `OK|SYSTEM|Alice|Available rooms: [#100] general`

4) Join room 100
- Client: `REQ|JOIN:100`
- Server to room: `SYSTEM|SYSTEM||'Alice' has joined the room.`

5) Send message
- Client: `REQ|MSG:Hello!`
- Server to room: `USER|Alice||Hello!`

6) Private message
- Client: `REQ|PRIVMSG:Bob Hey Bob`
- Server to Bob: `PRIVATE|Alice|Bob Hey Bob`
- Echo to Alice: `PRIVATE|Alice|Bob Hey Bob`

7) Leave and quit
- Client: `REQ|LEAVE:`
- Server to room: `SYSTEM|SYSTEM||'Alice' has left the room.`
- Client: `REQ|QUIT:`
- Connection closes.

---

### Validation & Limits
- Username: 3–15 chars; letters, digits, underscore
- Room name: 3–20 chars; letters, digits, underscore, hyphen
- JOIN expects a numeric room ID
- Messages are single-line; multiline is not supported
- No server-side rate limiting configured
- Timeout value exists in `ServerConfig` but is not currently enforced in I/O

---

### Logging
The server logs:
- Incoming raw lines and parsed requests (from whom, size/previews)
- Dispatch routing (broadcast, room, private)
- Room join/leave events and member counts
- Connection lifecycle (connected, disconnected)

These logs are intended to help during development and debugging.

---

### Security Notes
- Plaintext TCP; no TLS/SSL or auth beyond unique usernames
- In-memory state only; no persistence
- The server does not escape the `|` delimiter in content; client parsers should be robust (split into at most 4 parts)
- Recommended for trusted networks and development/testing use

---

### Roadmap / Known Gaps
- Optional: enforce client timeouts
- Optional: room name uniqueness and richer metadata
- Optional: persistence of users and rooms
- Optional: authentication beyond username-only
- The `NICK` command may be present but not implemented

---

### Contributing
- Fork and open a PR
- Keep commands self-contained (`Command` implementations) and register in `TermiTalkServer#registerCommands()`
- Add tests or reproducible steps for protocol changes

---

### License
Add your preferred license here (e.g., MIT, Apache-2.0).

---

### Contact
It is just a pet project I did. Just feel free to modify as much as you want if you liked it.
