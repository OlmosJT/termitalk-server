package io.olmosjt.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerUtil {
  public enum Level {
    INFO,
    WARN,
    ERROR,
    DEBUG,
    TRACE
  }

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private static volatile PrintWriter fileWriter;
  private static volatile String logFilePath = "action.log";

  public static synchronized void setLogFile(String path) {
    if (path == null || path.isBlank()) return;
    logFilePath = path;
    reopenFileWriter();
  }

  private static synchronized void reopenFileWriter() {
    closeFileWriter();
    try {
      fileWriter = new PrintWriter(new FileWriter(Path.of(logFilePath).toFile(), true), true);
    } catch (IOException e) {
      fileWriter = null; // fall back to console only
      System.err.println("[LoggerUtil] Failed to open log file '" + logFilePath + "': " + e.getMessage());
    }
  }

  private static synchronized void closeFileWriter() {
    if (fileWriter != null) {
      try { fileWriter.flush(); fileWriter.close(); } catch (Exception ignored) {}
      fileWriter = null;
    }
  }


  public static void log(Level level, String message) {
    String timestamp = LocalDateTime.now().format(FORMATTER);
    String line = String.format("[%s] [%s] %s", timestamp, level, message);

    // Console
    System.out.println(line);

    // File (lazy-open)
    PrintWriter writer = fileWriter;
    if (writer == null) {
      synchronized (LoggerUtil.class) {
        if (fileWriter == null) reopenFileWriter();
        writer = fileWriter;
      }
    }
    if (writer != null) {
      synchronized (LoggerUtil.class) {
        writer.println(line);
      }
    }
  }

  public static void info(String message) {
    log(Level.INFO, message);
  }

  public static void warn(String message) {
    log(Level.WARN, message);
  }

  public static void error(String message) {
    log(Level.ERROR, message);
  }

  public static void debug(String message) {
    log(Level.DEBUG, message);
  }

}
