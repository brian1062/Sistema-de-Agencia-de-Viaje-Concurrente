package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

/** Utility class for logging application messages and errors. */
public class Logger {
  private static Logger logger = null;
  private static final String LOG_PATH = "/tmp/petriNetResults.txt";
  private static final String TRANSITIONS_LOG_PATH = "/tmp/transitionsSequence.txt";
  private final FileWriter writer;
  private final FileWriter transitionsWriter;

  private Logger() throws IOException {
    this.writer = new FileWriter(LOG_PATH, true);
    this.transitionsWriter = new FileWriter(TRANSITIONS_LOG_PATH, true);
  }

  public static Logger getLogger() {
    if (logger == null) {
      try {
        logger = new Logger();
      } catch (IOException e) {
        throw new RuntimeException("Failed to initialize logger: " + e.getMessage());
      }
    }
    return logger;
  }

  /**
   * Logs an error message to both console and file.
   *
   * @param message The error message to log
   */
  public void error(String message) {
    String formattedMessage = LocalDateTime.now() + " ERROR: " + message;
    System.err.println(formattedMessage);
    writeToFile(formattedMessage);
  }

  /**
   * Logs an info message to both console and file.
   *
   * @param message The info message to log
   */
  public void info(String message) {
    String formattedMessage = LocalDateTime.now() + " INFO: " + message;
    System.out.println(formattedMessage);
    writeToFile(formattedMessage);
  }

  /**
   * Logs a transition to the transitions log file.
   *
   * @param transitionIndex Index of transition to log
   */
  public void logTransition(int transitionIndex) {
    try {
      transitionsWriter.write("T" + transitionIndex);
      transitionsWriter.flush();
    } catch (IOException e) {
      error("Failed to write transition to log: " + e.getMessage());
    }
  }

  private synchronized void writeToFile(String message) {
    try {
      writer.write(message);
      writer.write(System.lineSeparator());
    } catch (IOException e) {
      System.err.println("Failed to write to log file: " + e.getMessage());
    }
  }

  public void close() {
    try {
      if (writer != null) {
        writer.close();
      }
      if (transitionsWriter != null) {
        transitionsWriter.close();
      }
    } catch (IOException e) {
      System.err.println("Failed to close logger: " + e.getMessage());
    }
  }
}
