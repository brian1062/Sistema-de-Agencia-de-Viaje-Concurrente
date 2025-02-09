import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Utility class for logging application messages and errors.
 * Implements a singleton pattern to ensure a single logging instance across the application.
 */

public class Logger {
  private static Logger logger = null;
  private static final String LOG_PATH = "/tmp/petriNetResults.txt";
  
  private Logger() {} // Private constructor to enforce singleton pattern
  
  public static Logger getLogger() {
    if (logger == null) {
      logger = new Logger();
    }
    return logger;
  }
  
  /**
   * Logs an error message to both console and file.
   * @param message The error message to log
   */
  public void error(String message) {
    String errorMessage = LocalDateTime.now() + " ERROR: " + message;
    System.err.println(errorMessage);
    writeToFile(errorMessage);
  }
  
  /**
   * Logs an info message to both console and file.
   * @param message The info message to log
   */
  public void info(String message) {
    String infoMessage = LocalDateTime.now() + " INFO: " + message;
    System.out.println(infoMessage);
    writeToFile(infoMessage);
  }
  
  /**
   * Writes a message to the log file with proper resource handling.
   * @param message The message to write to the file
   */
  private void writeToFile(String message) {
    try (FileWriter writer = new FileWriter(LOG_PATH, true)) {
      writer.write(message + "\n");
    } catch (IOException e) {
      System.err.println("Failed to write to log file: " + e.getMessage());
    }
  }
}