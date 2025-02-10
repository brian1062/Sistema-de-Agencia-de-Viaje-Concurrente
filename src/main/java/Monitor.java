import java.util.concurrent.Semaphore;

/**
 * Monitor class that implements thread-safe operations on a Petri Net.
 * Uses the Singleton pattern to ensure only one monitor instance exists.
 */
class Monitor implements MonitorInterface {
  private static Monitor monitor = null;
  private final PetriNet petriNet;
  private final Semaphore mutex;
  private final Logger logger;

  /**
   * Private constructor to enforce Singleton pattern.
   *
   * @param petriNet the PetriNet instance to control.
   */
  private Monitor(PetriNet petriNet) {
    this.mutex = new Semaphore(1, true);
    this.petriNet = petriNet;
    this.logger = Logger.getLogger();
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
   * Attempts to fire a transition in the Petri Net.
   * Handles both immediate and timed transitions with proper synchronization.
   * @param transitionIndex Index of the transition to fire
   * @return true if transition fired successfully, false otherwise
   */
  @Override
  public boolean fireTransition(int transitionIndex) {
    Transition transition;
    try {
      transition = petriNet.getTransitionFromIndex(transitionIndex);
    } catch (IllegalArgumentException e) {
      logger.error(e.getMessage());
      return false;
    }

    // Handle timed transitions
    if (transition.getTime() > 0) {
      try {
        Thread.sleep(transition.getTime());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.error("Thread interrupted during timed transition: " + transitionIndex);
        return false;
      }
    }

    try {
      mutex.acquire();
      return executeTransition(transitionIndex);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("Thread interrupted while acquiring mutex: " + transitionIndex);
      return false;
    } finally {
      mutex.release();
    }
  }

  /**
   * Executes the transition while holding the mutex.
   * @param transitionIndex Index of transition to execute
   * @return true if successful, false otherwise
   */
  private boolean executeTransition(int transitionIndex) {
    try {
      if (petriNet.tryFireTransition(transitionIndex)) {
        logTransitionSuccess(transitionIndex);
        return true;
      }
      return false;
    } catch (Exception e) {
      logger.error(e.getMessage());
      return false;
    }
  }

  private void logTransitionSuccess(int transitionIndex) {
    String message = String.format(
      "Transition fired: {T%d} Marking: {%s}",
      transitionIndex, 
      petriNet.getStringMarking()
    );
    logger.info(message);
  }

  /**
   * Checks if the Petri Net has reached its target number of invariants.
   * @return true if target invariants achieved, false otherwise
   */
  public boolean petriNetHasFinished() {
    return petriNet.petriNetHasFinished();
  }
}

/** Interface for Monitor functionality. */
interface MonitorInterface {
  boolean fireTransition(int transition);
}