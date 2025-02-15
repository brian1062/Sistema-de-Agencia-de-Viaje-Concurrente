package monitor;
import java.util.concurrent.Semaphore;

import petrinet.PetriNet;
import petrinet.Transition;
import policy.Policy;
import utils.Logger;

/**
 * Monitor class that implements thread-safe operations on a Petri Net. Uses the Singleton pattern
 * to ensure only one monitor instance exists.
 */
public class Monitor implements MonitorInterface {
  private static Monitor monitor = null;
  private static Logger logger = Logger.getLogger();
  private final PetriNet petriNet;
  private final Semaphore mutex;
  private final Policy policy;

  /**
   * Private constructor to enforce Singleton pattern.
   *
   * @param petriNet the PetriNet instance to control.
   */
  private Monitor(PetriNet petriNet, Policy policy) {
    this.mutex = new Semaphore(1, true);
    this.petriNet = petriNet;
    this.policy = policy;
  }

  /**
   * Returns the singleton instance of the Monitor.
   *
   * @param petriNet the PetriNet instance to associate with the Monitor.
   * @return the Monitor instance.
   */
  public static Monitor getMonitor(PetriNet petriNet, Policy policy) {
    if (monitor == null) {
      monitor = new Monitor(petriNet, policy);
    }
    return monitor;
  }

  /**
   * Attempts to fire a transition in the Petri Net. Handles both immediate and timed transitions
   * with proper synchronization.
   *
   * @param transitionIndex Index of the transition to fire
   * @return true if transition fired successfully, false otherwise
   */
  @Override
  public boolean fireTransition(int transitionIndex) {
    // Check if the transition can fire according to the policy
    if (!policy.canFireTransition(transitionIndex)) return false;

    Transition transition;
    try {
      transition = petriNet.getTransitionFromIndex(transitionIndex);
    } catch (IllegalArgumentException e) {
      logger.error(e.getMessage());
      return false;
    }

    try {
      mutex.acquire();
      if (!handleTimedTransition(transition)) return false;

      // If the transition fires successfully, update the policy
      if (executeTransition(transitionIndex)) {
        policy.transitionFired(transitionIndex);
        return true;
      }
      return false;

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
   *
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

  /**
   * Handles timed transition
   *
   * @param transition Transition to handle
   */
  private boolean handleTimedTransition(Transition transition) {
    if (transition.getDelayTime() > 0 && petriNet.isTransitionEnabled(transition.getNumber())) {
      try {
        mutex.release();
        Thread.sleep(transition.getDelayTime());
        try {
          mutex.acquire();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          logger.error("Thread interrupted while acquiring mutex: " + transition.getNumber());
          return false;
        }
        return true;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.error("Thread interrupted during timed transition: " + transition.getNumber());
        return false;
      }
    }
    return true;
  }

  private void logTransitionSuccess(int transitionIndex) {
    String message =
        String.format(
            "Transition fired: {T%d} Marking: {%s}", transitionIndex, petriNet.getStringMarking());
    logger.info(message);
    logger.logTransition(transitionIndex);
  }

  /**
   * Checks if the Petri Net has reached its target number of invariants.
   *
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
