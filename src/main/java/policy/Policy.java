package policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;
import utils.Logger;

/**
 * Abstract class that defines the structure of a policy to control the firing of transitions in the
 * Petri Net. Provides mutual exclusion using a semaphore to ensure that only one thread can fire a
 * transition at a time.
 */
public abstract class Policy {
  /**
   * Semaphore to ensure thread-safe policy operations. Protects access to policy state during
   * transition evaluation and firing.
   */
  protected final Semaphore policyMutex;

  /** Map to store transition pair counts. */
  protected final Map<Integer, Integer> transitionCounts = new HashMap<>();

  /**
   * Transitions pairs requiring firing. Each sub-array contains two transition indices that should
   * be fired following the firing policy.
   */
  protected static final int[][] PAIRS = {
    {2, 3}, // First pair to balance
    {6, 7} // Second pair to balance
  };

  /** Shared logger instance for logging policy-related events. */
  protected static Logger logger = Logger.getLogger();

  /** Constructor for the Policy class with synchronization mechanism. */
  protected Policy() {
    this.policyMutex = new Semaphore(1, true);
  }

  /**
   * Determines if a transition can be fired based on the policy rules.
   *
   * @param transitionIndex Index of the transition to evaluate.
   * @return true if the transition can be fired, false otherwise.
   */
  public abstract boolean canFireTransition(int transitionIndex);

  /**
   * Updates the policy state after a transition has been fired.
   *
   * @param transitionIndex Index of the transition that was fired.
   */
  public abstract void transitionFired(int transitionIndex);

  /**
   * Checks if a given transition is one of the tracked transitions.
   *
   * @param transitionIndex The transition index to check.
   * @return True if the transition is tracked, false otherwise.
   */
  private boolean isTrackedTransition(int transitionIndex) {
    return transitionCounts.containsKey(transitionIndex);
  }

  /**
   * Returns the next transition to fire based on the policy rules.
   *
   * @param enabledTransitions List of currently enabled transitions.
   * @return Index of the next transition to fire.
   */
  public int getNextTransition(int[] enabledTransitions) {
    int randomTransition = getRandomEnabledIndex(enabledTransitions);
    if (randomTransition == -1) {
      return -1;
    }

    if (!isTrackedTransition(randomTransition)) {
      return randomTransition;
    }

    // Get the paired transition
    int pairedTransition = getPairedTransition(randomTransition);
    if (pairedTransition == -1) {
      return -1;
    }

    // Check if the paired transition is enabled
    if (enabledTransitions[pairedTransition] == 1) {
      // Check against the policy which one to fire
      if (canFireTransition(randomTransition)) {
        return randomTransition;
      } else {
        return pairedTransition;
      }
    }
    return randomTransition; // If paired transition is not enabled, fire the random one
  }

  /**
   * Selects a random index from the array where the value is 1.
   *
   * @param bitwiseAndResult The array resulting from the bitwise AND operation.
   * @return A random index where the value is 1, or -1 if no such index exists.
   */
  protected int getRandomEnabledIndex(int[] bitwiseAndResult) {
    List<Integer> enabledIndices = new ArrayList<>();

    // Collect indices where the value is 1
    for (int i = 0; i < bitwiseAndResult.length; i++) {
      if (bitwiseAndResult[i] == 1) {
        enabledIndices.add(i);
      }
    }

    // If no indices are enabled, return -1
    if (enabledIndices.isEmpty()) {
      return -1;
    }

    // Select a random index from the list of enabled indices
    Random random = new Random();
    return enabledIndices.get(random.nextInt(enabledIndices.size()));
  }

  /**
   * Returns the other transition in the pair for the given transition index.
   *
   * @param transitionIndex The transition index to find its pair.
   * @return The other transition in the pair, or -1 if the transition is not part of any pair.
   */
  protected int getPairedTransition(int transitionIndex) {
    for (int[] pair : PAIRS) {
      if (pair[0] == transitionIndex) {
        return pair[1]; // Return the other transition in the pair
      } else if (pair[1] == transitionIndex) {
        return pair[0]; // Return the other transition in the pair
      }
    }
    return -1; // Return -1 if the transition is not part of any pair
  }
}
