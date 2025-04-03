package policy;

import java.util.HashMap;
import java.util.Map;

/**
 * Policy that prioritizes certain transitions in a Petri Net based on predefined target
 * percentages.
 * This policy ensures that specific transitions fire with a target proportion relative to their
 * paired transitions. It tracks transition counts and enforces priority constraints accordingly.
 */
public class PrioritizedPolicy extends Policy {
  /** A map storing the count of fired occurrences for each tracked transition. */
  private final Map<Integer, Integer> transitionCounts = new HashMap<>();

  /**
   * Transition pairs that require priority handling.
   * Each sub-array contains two transition indices where the first transition is prioritized over
   * the second.
   */
  private static final int[][] PRIORITY_PAIRS = {
    {2, 3}, // First pair (T2/T3)
    {6, 7}  // Second pair (T6/T7)
  };

  /**
   * Target percentages for the prioritized transitions.
   * The first value corresponds to the first pair (T2/T3) and the second to the second pair
   * (T6/T7).
   */
  private static final double[] TARGET_PERCENTAGES = {
    0.75, // T2 should be 75% of T2+T3
    0.80 // T6 should be 80% of T6+T7
  };

  /** 
   * Constructs a PrioritizedPolicy and initializes transition count tracking. 
   */
  public PrioritizedPolicy() {
    // Initialize counters for all transitions we're tracking
    for (int[] pair : PRIORITY_PAIRS) {
      transitionCounts.put(pair[0], 0);
      transitionCounts.put(pair[1], 0);
    }
  }

  /**
   * Determines whether a transition is allowed to fire based on the priority policy.
   *
   * @param transitionIndex The index of the transition to check.
   * @return true if the transition can fire, false otherwise.
   */
  @Override
  public boolean canFireTransition(int transitionIndex) {
    // If not a tracked transition, allow firing
    if (!isTrackedTransition(transitionIndex)) {
      return true;
    }

    try {
      policyMutex.acquire();
      boolean canFire = canFirePrioritizedTransition(transitionIndex);
      policyMutex.release();
      return canFire;

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("Thread interrupted while acquiring policy mutex");
      return false;
    }
  }

  /**
   * Updates the transition count when a transition fires.
   *
   * @param transitionIndex The index of the transition that has fired.
   */
  @Override
  public void transitionFired(int transitionIndex) {
    try {
      policyMutex.acquire();
      transitionCounts.computeIfPresent(transitionIndex, (key, value) -> value + 1);
      policyMutex.release();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("Thread interrupted while updating transition counts");
    }
  }

  /**
   * Checks if a given transition is one of the tracked transitions.
   *
   * @param transitionIndex The transition index to check.
   * @return true if the transition is tracked, false otherwise.
   */
  private boolean isTrackedTransition(int transitionIndex) {
    return transitionCounts.containsKey(transitionIndex);
  }

  /**
   * Determines whether a prioritized transition is allowed to fire based on its current proportion.
   *
   * @param transitionIndex The transition index to check.
   * @return true if the transition can fire under the defined priority rules, false otherwise.
   */
  private boolean canFirePrioritizedTransition(int transitionIndex) {
    // Find which pair this transition belongs to
    for (int i = 0; i < PRIORITY_PAIRS.length; i++) {
      int[] pair = PRIORITY_PAIRS[i];
      if (pair[0] == transitionIndex || pair[1] == transitionIndex) {
        int priorityTransition = pair[0]; // First transition in pair is prioritized
        int otherTransition = pair[1];
        double targetPercentage = TARGET_PERCENTAGES[i];

        int priorityCount = transitionCounts.get(priorityTransition);
        int otherCount = transitionCounts.get(otherTransition);
        int totalCount = priorityCount + otherCount;

        // Allow first firing
        if (totalCount == 0) {
          return true;
        }

        double currentPriorityPercentage = (double) priorityCount / totalCount;

        // If this is the priority transition
        if (transitionIndex == priorityTransition) {
          return currentPriorityPercentage < targetPercentage;
        } else { // If this is the other transition
          return currentPriorityPercentage >= targetPercentage;
        }
      }
    }
    return false; // Should never reach here if isTrackedTransition was true
  }
}
