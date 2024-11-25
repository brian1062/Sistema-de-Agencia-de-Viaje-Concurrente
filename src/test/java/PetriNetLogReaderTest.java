import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.junit.jupiter.api.Test;

public class PetriNetLogReaderTest {

  @Test
  void testLogValidation() {
    String logFilePath = "/tmp/petriNetResults.txt";
    PetriNetLogReader reader = new PetriNetLogReader();
    PetriNetConf petriNetConf = new PetriNetConf();

    try {
      reader.validateLogFile(logFilePath, petriNetConf);
      System.out.println("Log validation passed.");
    } catch (IOException e) {
      fail("Error reading log file: " + e.getMessage());
    } catch (AssertionError e) {
      fail("Log validation failed: " + e.getMessage());
    }
  }
}
