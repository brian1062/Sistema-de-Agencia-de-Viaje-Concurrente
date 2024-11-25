import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;

/**
 * Monitor class for managing synchronized interactions with a Petri Net. Ensures only one instance
 * of Monitor is created using the Singleton pattern.
 */
class Monitor implements MonitorInterface {

  // Singleton instance of the Monitor
  private static Monitor monitor = null;
  boolean isFireSuccessful = false;
  PetriNet petriNet; // The associated Petri Net instance
  private final String LOG_PATH = "/tmp/petriNetResults.txt";

  private final Semaphore mutex; // Mutex to ensure thread safety

  /**
   * Private constructor to enforce Singleton pattern.
   *
   * @param petriNet the PetriNet instance to control.
   */
  private Monitor(PetriNet petriNet) {
    this.mutex = new Semaphore(1, true);
    this.petriNet = petriNet;
  }

  /**
   * Returns the singleton instance of the Monitor.
   *
   * @param petriNet the PetriNet instance to associate with the Monitor.
   * @return the Monitor instance.
   */
  public static Monitor getMonitor(PetriNet petriNet) {
    if (monitor == null) {
      monitor = new Monitor(petriNet);
    }
    return monitor;
  }

  /**
   * Attempts to fire a transition in the associated Petri Net.
   *
   * @param transitionIndex the index of the transition to fire.
   * @return true if the transition was successfully fired at least once, false otherwise.
   */
  @Override
  public boolean fireTransition(int transitionIndex) {
    try {
      mutex.acquire();
    } catch (Exception e) {
      e.printStackTrace();
    }
    isFireSuccessful = true;
    while (isFireSuccessful) {
      isFireSuccessful = petriNet.tryFireTransition(transitionIndex);
      if (isFireSuccessful) {
        String outputMessage =
            "Transition fired: {T"
                + transitionIndex
                + "}"
                + " Marking: {"
                + petriNet.getStringMarking()
                + "}";
        System.out.println(outputMessage);
        String timestamp = LocalDateTime.now().toString();
        writeLog(timestamp + ": " + outputMessage);
      }
    }
    mutex.release();
    return false;
  }

  /**
   * Checks if the Petri Net has achieved its invariants target.
   *
   * @return true if the invariants target is achieved, false otherwise.
   */
  public boolean petriNetHasFinished() {
    return petriNet.invariantsTargetAchieved();
  }

  /**
   * Writes a message to the log file.
   *
   * @param message the message to write to the log file.
   */
  private void writeLog(String message) {
    try {
      FileWriter writer = new FileWriter(LOG_PATH, true);
      writer.write(message + "\n");
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

/** Interface for Monitor functionality. */
interface MonitorInterface {
  boolean fireTransition(int transition);
}
