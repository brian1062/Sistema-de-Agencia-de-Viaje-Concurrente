package monitor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

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
  private final Map<Integer, Integer> transitionPriorityBoost = new ConcurrentHashMap<>();
  private static final int MAX_BOOST = 3;

  /**
   * Private constructor to enforce Singleton pattern.
   *
   * @param petriNet the PetriNet instance to control.
   * @param policy the Policy to use for transition firing.
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
   * @param policy the Policy instance to associate with the Monitor.
   * @return the singleton Monitor instance.
   */
  public static Monitor getMonitor(PetriNet petriNet, Policy policy) {
    if (monitor == null) {
      monitor = new Monitor(petriNet, policy);
    }
    return monitor;
  }

  /**
   * Attempts to fire a transition in the Petri Net.
   *
   * @param transitionIndex Index of the transition to fire.
   * @return true if the transition was successfully fired, false otherwise.
   */
  @Override
  public boolean fireTransition(int transitionIndex) {
    // 1. Check if transition is enabled
    if (!petriNet.isTransitionEnabled(transitionIndex)) {
        return false;
    }
    // 2. Get the transition object
    Transition transition;
    try {
        transition = petriNet.getTransitionFromIndex(transitionIndex);
    } catch (IllegalArgumentException e) {
        logger.error(e.getMessage());
        return false;
    }
  
    // 3. Handle timed transition
    try {
        mutex.acquire();
        if (!handleTimedTransition(transition)) {
            return false;
        }
      
        // 3.1 Check if transition is still enabled
        if (petriNet.isTransitionEnabled(transitionIndex)) {
            List<Integer> enabled = petriNet.getEnabledTransitions()
                .stream()
                .map(t -> t.getNumber())
                .collect(Collectors.toList());
            List<Integer> preferred = policy.getPreferedTransitions(enabled);
        
            // 3.2 Check if transition is preferred
            if (!preferred.isEmpty() && !preferred.contains(transitionIndex)) {
                int boost = transitionPriorityBoost.merge(transitionIndex, 1, Integer::sum);
                if (boost >= MAX_BOOST) {
                    preferred.add(transitionIndex);
                    logger.info("Aging: Transition T" + transitionIndex + " boosted");
                } else {
                    mutex.release();
                    return false;
                }
            }
          
            // 3.3 Execute transition
            if (executeTransition(transitionIndex)) {
                policy.transitionFired(transitionIndex);
                transitionPriorityBoost.remove(transitionIndex);
                mutex.release();
                return true;
            }
        }
        mutex.release();
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.error("Thread interrupted: " + transitionIndex);
    }
    return false;
  }

  /**
   * Executes the transition while holding the mutex.
   *
   * @param transitionIndex Index of transition to execute.
   * @return true if successful, false otherwise.
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
   * Handles timed transition by releasing the mutex, waiting the delay time, and re-acquiring the
   * mutex.
   *
   * @param transition Transition to handle.
   * @return true if successful, false otherwise.
   */
  private boolean handleTimedTransition(Transition transition) throws InterruptedException {
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

  /**
   * Logs the successful firing of a transition.
   *
   * @param transitionIndex Index of transition that fired.
   */
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
   * @return true if target invariants achieved, false otherwise.
   */
  public synchronized boolean petriNetHasFinished() {
    return petriNet.petriNetHasFinished();
  }
}

/** Interface for Monitor functionality. */
interface MonitorInterface {
  /**
   * Attempts to fire a transition in the Petri Net.
   *
   * @param transition Index of the transition to fire.
   * @return true if transition fired successfully, false otherwise.
   */
  boolean fireTransition(int transition);
}
