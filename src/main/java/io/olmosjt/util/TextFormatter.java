package io.olmosjt.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TextFormatter {
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

  // ANSI escape codes
  private static final String RESET = "\u001B[0m";
  private static final String GREEN = "\u001B[32m";
  private static final String CYAN = "\u001B[36m";
  private static final String RED = "\u001B[31m";
  private static final String YELLOW = "\u001B[33m";

  public static String timestamped(String message) {
    String time = LocalDateTime.now().format(TIME_FORMATTER);
    return "[" + time + "] " + message;
  }

  public static String systemMessage(String message) {
    return CYAN + message + RESET;
  }

  public static String errorMessage(String message) {
    return RED + message + RESET;
  }

  public static String successMessage(String message) {
    return GREEN + message + RESET;
  }

  public static String warningMessage(String message) {
    return YELLOW + message + RESET;
  }
}
