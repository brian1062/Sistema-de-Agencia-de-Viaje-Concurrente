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

  /**
   * Attempts to fire a transition in the Petri Net. Handles both immediate and timed transitions
   * with proper synchronization.
   *
   * @param transitionIndex Index of the transition to fire.
   * @return true if transition fired successfully, false otherwise.
   */
  @Override
  public boolean fireTransition(int transitionIndex) {
    try {
      // If the mutex is not available, waits for it in the mutex's queue
      mutex.acquire();
      boolean mutexAcquired = true;

      while (mutexAcquired) {
        // Check if the transition can be fired or needs to wait a certain time
        handleTimedTransition(transitionIndex);

        mutexAcquired = executeTransition(transitionIndex);

        if (mutexAcquired) {
          // Update the policy
          policy.transitionFired(transitionIndex);

          int[] transitionsForPolicyToChooseFrom =
              bitwiseAnd(petriNet.getEnabledTransitionsInBits(), getWaitingTransitions());

          /* LOGS FOR DEBUGGING // TODO delete later */
          System.out.println("Enabled transitions in the Petri net: ");
          printArray(petriNet.getEnabledTransitionsInBits());
          System.out.println("Waiting transitions in transitionsQueue: ");
          printArray(getWaitingTransitions());
          System.out.println("AND Operation between enabled and waiting: ");
          printArray(transitionsForPolicyToChooseFrom);

          // If no waiting transitions are enabled, release the mutex and return
          if (!containsOne(transitionsForPolicyToChooseFrom)) {
            logger.info("No waiting transitions are enabled, releasing mutex.");
            mutex.release();
            return true;
          }

          /* Since there are transitions enabled and waiting,
          get the next one to fire based on the current policy */
          int nextTransition = policy.getNextTransition(transitionsForPolicyToChooseFrom);
          if (nextTransition != -1) {
            logger.info("Transition received from policy: " + nextTransition);
            // Wake up the next transition in the queue
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
   * Handles timed transition by releasing the mutex, waiting the delay time, and re-acquiring the
   * mutex.
   *
   * @param transitionIndex Index of the transition to handle.
   * @throws InterruptedException if the thread is interrupted while sleeping.
   * @throws RuntimeException if the transition index is invalid.
   */
  private void handleTimedTransition(int transitionIndex) {
    Transition transition;
    try {
      transition = petriNet.getTransitionFromIndex(transitionIndex);
    } catch (IllegalArgumentException e) {
      logger.error(e.getMessage());
      throw new RuntimeException("Invalid transition index: " + transitionIndex);
    }

    // Check if the transition is timed and enabled
    if (transition.getDelayTime() > 0 && petriNet.isTransitionEnabled(transition.getNumber())) {
      try {
        logger.info("Timed transition {" + transitionIndex + "} is now sleeping...");
        mutex.release();
        Thread.sleep(transition.getDelayTime());
        logger.info("Timed transition {" + transitionIndex + "} woke up!");

        // Check if the mutex is being requested by other threads. If not, acquire it.
        if (!mutex.hasQueuedThreads()) {
          mutex.acquire();
          return;
        }

        // If the mutex is being requested by other threads, wait in the transitionsQueue to be
        // called
        // by another thread already holding the mutex
        logger.info("Timed transition {" + transitionIndex + "} is waiting in the queue...");
        transitionsQueue[transitionIndex].acquire();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.error("Thread interrupted during timed transition: " + transition.getNumber());
      }
    }
  }

  /**
   * Returns an array of integers indicating whether there are transitions waiting for each
   * semaphore in the transitionsQueue.
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
   * Checks if the Petri Net has reached its target number of invariants.
   *
   * @return true if target invariants achieved, false otherwise.
   */
  public synchronized boolean petriNetHasFinished() {
    /* TODO: no me gusta estoooooo */
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
