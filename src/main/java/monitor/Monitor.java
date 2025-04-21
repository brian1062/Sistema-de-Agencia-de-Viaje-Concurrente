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
  private final Semaphore[] transitionsQueue;
  private final PetriNet petriNet;
  private final Semaphore mutex;
  private final Policy policy;

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
    this.transitionsQueue = new Semaphore[petriNet.getNumberOfTransitions()];
    for (int i = 0; i < petriNet.getNumberOfTransitions(); i++) {
      // Initialize each transition's semaphore to 0
      // to block until it is released
      transitionsQueue[i] = new Semaphore(0, true);
    }
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
   * Attempts to fire a transition in the Petri Net. Handles both immediate and timed transitions
   * with proper synchronization.
   *
   * @param transitionIndex Index of the transition to fire.
   * @return true if transition fired successfully, false otherwise.
   */
  /*@Override
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
  }*/

  @Override
  public boolean fireTransition(int transitionIndex){
    try {
      mutex.acquire();
      boolean mutexAcquired = true;
      
      while(mutexAcquired){
        Transition transition;
        try {
          transition = petriNet.getTransitionFromIndex(transitionIndex);
        } catch (IllegalArgumentException e) {
          logger.error(e.getMessage());
          return false;
        }

        // Check if the transition can be fired or needs to wait a certain time
        //handleTimedTransition(transition);

        mutexAcquired = executeTransition(transitionIndex);
        
        if (mutexAcquired) {
          // Update the policy
          policy.transitionFired(transitionIndex);
          
          // Get the enabled waiting transitions in bits
          int[] transitionsForPolicyToChooseFrom = bitwiseAnd(petriNet.getEnabledTransitionsInBits(), getWaitingTransitions());
          
          // If no waiting transitions are enabled, release the mutex and return
          if (!containsOne(transitionsForPolicyToChooseFrom)) {
            mutex.release();
            return true;
          }

          // Get the next transition to fire based on the policy
          int nextTransition = policy.getNextTransition(transitionsForPolicyToChooseFrom);
          if (nextTransition != -1) {
            transitionsQueue[nextTransition].release();
          }
          // Release the mutex for the next transition
          mutex.release();
          return true;
        }
        return false;
      }
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("Thread interrupted while acquiring mutex: " + transitionIndex);
      return false;
    }
    finally {
      mutex.release();
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
  /*private boolean handleTimedTransition(Transition transition) {
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
  }*/
  
  // TODO: should this method be boolean or void?
  private boolean handleTimedTransition(Transition transition) {
    if (transition.getDelayTime() > 0 && petriNet.isTransitionEnabled(transition.getNumber())) {
      try {
        mutex.release();
        Thread.sleep(transition.getDelayTime());
        transitionsQueue[transition.getNumber()].acquire();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.error("Thread interrupted during timed transition: " + transition.getNumber());
        return false;
      }
    }
    return true;
  }

  /**
   * Returns an array of integers indicating whether there are transitions waiting
   * for each semaphore in the transitionsQueue.
   *
   * @return An array of integers where 1 indicates transitions are waiting, and 0 otherwise.
   */
  public int[] getWaitingTransitions() {
    int[] waitingTransitions = new int[transitionsQueue.length];
    for (int i = 0; i < transitionsQueue.length; i++) {
      waitingTransitions[i] = transitionsQueue[i].hasQueuedThreads() ? 1 : 0;
    }
    return waitingTransitions;
  }

  /**
   * Performs a bitwise AND operation on two integer arrays.
   *
   * @param array1 The first integer array.
   * @param array2 The second integer array.
   * @return An array containing the result of the bitwise AND operation.
   * @throws IllegalArgumentException if the arrays have different lengths.
   */
  public int[] bitwiseAnd(int[] array1, int[] array2) {
    if (array1.length != array2.length) {
      throw new IllegalArgumentException("[ERROR] Arrays must have the same length");
    }

    int[] result = new int[array1.length];
    for (int i = 0; i < array1.length; i++) {
      result[i] = array1[i] & array2[i]; // Perform bitwise AND
    }
    return result;
  }

  /**
   * Checks if the array contains at least one 1.
   *
   * @param array The array to check.
   * @return true if the array contains at least one 1, false otherwise.
   */
  public boolean containsOne(int[] array) {
    for (int value : array) {
      if (value == 1) {
        return true; // Found a 1, no need to check further
      }
    }
    return false; // No 1 found
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
