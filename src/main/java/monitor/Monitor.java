package monitor;

import java.util.concurrent.Semaphore;
import petrinet.PetriNet;
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
  private final Semaphore[] transitionsQueue;
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

  public static Monitor getMonitor() {
    return monitor;
  }

  public Semaphore getMutex() {
    return mutex;
  }

  /**
   * Attempts to fire a transition in the Petri Net. Handles both immediate and timed transitions
   * with proper synchronization.
   *
   * @param transitionIndex Index of the transition to fire.
   * @return true if transition fired successfully, false otherwise.
   */
  @Override
  public boolean fireTransition(int transitionIndex) { // todo B: es alpedo el return boolean
    try {
      // If the mutex is not available, waits for it in the mutex's queue
      mutex.acquire();
      boolean mutexAcquired = true;

      while (mutexAcquired) {

        mutexAcquired = executeTransition(transitionIndex);

        if (mutexAcquired) {
          // Update the policy
          policy.transitionFired(transitionIndex);

          boolean[] transitionsForPolicyToChooseFrom =
              bitwiseAnd(petriNet.getEnabledTransitionsInBits(), getWaitingTransitions());


          // If the Petri net has finished, then release the waiting threads
          if (petriNet.petriNetHasFinished()) {
            transitionsForPolicyToChooseFrom = getWaitingTransitions();
          }
          // If no waiting transitions are enabled, release the mutex and return
          if (!containsOne(transitionsForPolicyToChooseFrom)) {
            mutex.release();
            return true;
          }

          /* Since there are transitions enabled and waiting,
          get the next one to fire based on the current policy */
          int nextTransition = policy.getNextTransition(transitionsForPolicyToChooseFrom);
          if (nextTransition != -1) {
            logger.info("Transition received from policy: " + nextTransition);
            // Wake up the next transition in the queue
            logger.info("Transicion " + transitionIndex + "Waking up transition " + nextTransition);
            transitionsQueue[nextTransition].release();
          }

          // Exit the monitor with a successful transition firing
          return true;
        } else {
          logger.info("Transition " + transitionIndex + " could not be executed.");
          // Release the mutex if the transition could not be executed
          mutex.release();
          transitionsQueue[transitionIndex].acquire();
          mutexAcquired = true;
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("Thread interrupted while acquiring mutex: " + transitionIndex);
    }
    return false; // Transition could not be executed
  }

  /**
   * Prints the elements of an integer array in the format: {0, 1, 0, 1, 1, 0, 3, 1}.
   *
   * @param array The integer array to print.
   */
  public void printArray(int[] array) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    for (int i = 0; i < array.length; i++) {
      sb.append(array[i]);
      if (i < array.length - 1) {
        sb.append(", ");
      }
    }
    sb.append("}");
    System.out.println(sb.toString());
  }

  /**
   * Executes the transition while holding the mutex.
   *
   * @param transitionIndex Index of transition to execute.
   * @return true if successful, false otherwise.
   */
  private boolean executeTransition(int transitionIndex) {
    try {
      return petriNet.tryFireTransition(transitionIndex);
    } catch (Exception e) {
      logger.error(e.getMessage());
      return false;
    }
  }

/**
 * Returns a boolean array indicating which transitions are currently waiting in their semaphores.
 *
 * @return true at index i if transition i is waiting; false otherwise.
 */
  public boolean[] getWaitingTransitions() {
    boolean[] waitingTransitions = new boolean[transitionsQueue.length];
    for (int i = 0; i < transitionsQueue.length; i++) {
      waitingTransitions[i] = transitionsQueue[i].hasQueuedThreads() ? true : false;
    }
    return waitingTransitions;
  }

  /**
   * Performs a bitwise AND operation on two boolean arrays.
   *
   * @param array1 The first boolean array.
   * @param array2 The second boolean array.
   * @return An array containing the result of the bitwise AND operation.
   * @throws IllegalArgumentException if the arrays have different lengths.
   */
  public boolean[] bitwiseAnd(boolean[] array1, boolean[] array2) {
    if (array1.length != array2.length) {
      throw new IllegalArgumentException("[ERROR] Arrays must have the same length");
    }

    boolean[] result = new boolean[array1.length];
    for (int i = 0; i < array1.length; i++) {
      result[i] = array1[i] & array2[i]; // Perform bitwise AND
    }
    return result;
  }

  /**
   * Checks if the array contains at least one true value.
   *
   * @param array The array to check.
   * @return true if the array contains at least one true, false otherwise.
   */
  public boolean containsOne(boolean[] array) {
    for (boolean value : array) {
      if (value) {
        return true; // Found a 1, no need to check further
      }
    }
    return false; // No 1 found
  }

  /**
   * Checks if the Petri Net has reached its target number of invariants.
   *
   * @return true if target invariants achieved, false otherwise.
   */
  public boolean petriNetHasFinished() {
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
