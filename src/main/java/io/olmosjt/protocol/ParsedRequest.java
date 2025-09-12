package io.olmosjt.protocol;

import io.olmosjt.command.CommandType;

public record ParsedRequest(CommandType type, String payload) { }
