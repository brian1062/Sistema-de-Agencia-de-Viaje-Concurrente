import java.util.concurrent.Semaphore;

/**
 * Monitor class for managing synchronized interactions with a Petri Net. Ensures only one instance
 * of Monitor is created using the Singleton pattern.
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

  /*
   * Acquires the mutex to ensure thread safety.
   */
  private void acquireMutex() throws InterruptedException {
    mutex.acquire();
  }

  /**
   * Attempts to fire a transition in the associated Petri Net. If interrupted, restores the
   * interrupt flag and continues execution.
   *
   * @param transitionIndex the index of the transition to fire
   * @return true if the transition was successfully fired, false otherwise
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
      }
    }

    try {
      acquireMutex();
        try {
          if (petriNet.tryFireTransition(transitionIndex)) {
            String message =
              String.format(
                "Transition fired: {T%d} Marking: {%s}",
                transitionIndex, petriNet.getStringMarking());
            logger.info(message);
            return true;
          }
          return false;
        } catch (Exception e) {
          logger.error(e.getMessage());
        } finally {
          mutex.release();
        }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("Thread interrupted while acquiring mutex for transition: " + transitionIndex);
    }
    return false;
  }

  /**
   * Checks if the Petri Net has achieved its invariants target.
   *
   * @return true if the invariants target is achieved, false otherwise.
   */
  public boolean petriNetHasFinished() {
    return petriNet.petriNetHasFinished();
  }
}

/** Interface for Monitor functionality. */
interface MonitorInterface {
  boolean fireTransition(int transition);
}