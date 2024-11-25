import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.junit.jupiter.api.Test;

public class PetriNetLogReaderTest {

  @Test
  void testLogValidation() {
    String logFilePath = "/tmp/petriNetResults.txt";
    PetriNetConf petriNetConf = new PetriNetConf();

    // Check if file exists
    assertTrue(PetriNetLogReader.fileExists(logFilePath));

    try {
      PetriNetLogReader.validateLogFile(logFilePath, petriNetConf);
      System.out.println("Log validation passed.");
    } catch (IOException e) {
      fail("Error reading log file: " + e.getMessage());
    } catch (AssertionError e) {
      fail("Log validation failed: " + e.getMessage());
    }
  }
}
