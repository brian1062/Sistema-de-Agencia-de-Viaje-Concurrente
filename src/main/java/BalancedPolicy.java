import java.util.HashMap;
import java.util.Map;

/**
 * Policy that balances the firing of two transition pairs. For each pair, the other transition can
 * only fire if it has fired more times or equal to the current transition.
 */
public class BalancedPolicy extends Policy {
  // Map to store transition pair counts
  private final Map<Integer, Integer> transitionCounts = new HashMap<>();

  // Define transition pairs that need to be balanced
  private static final int[][] BALANCED_PAIRS = {
    {2, 3}, // First pair to balance
    {6, 7} // Second pair to balance
  };

  /** Constructs a {@code BalancedPolicy} and initializes transition count tracking. */
  public BalancedPolicy() {
    // Initialize counters for all transitions we're tracking
    for (int[] pair : BALANCED_PAIRS) {
      transitionCounts.put(pair[0], 0);
      transitionCounts.put(pair[1], 0);
    }
  }

  /**
   * Determines whether a transition is allowed to fire based on the balancing policy.
   *
   * @param transitionIndex The index of the transition to check.
   * @return {@code true} if the transition can fire, {@code false} otherwise.
   */
  @Override
  public boolean canFireTransition(int transitionIndex) {
    // If not a tracked transition, allow firing
    if (!isTrackedTransition(transitionIndex)) {
      return true;
    }

    try {
      policyMutex.acquire();
      boolean canFire = canFireBalancedTransition(transitionIndex);
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
   * @return {@code true} if the transition is tracked, {@code false} otherwise.
   */
  private boolean isTrackedTransition(int transitionIndex) {
    return transitionCounts.containsKey(transitionIndex);
  }

  /**
   * Determines whether a balanced transition is allowed to fire based on its paired transition
   * count.
   *
   * @param transitionIndex The transition index to check.
   * @return {@code true} if the transition can fire under the defined balancing rules, {@code
   *     false} otherwise.
   */
  private boolean canFireBalancedTransition(int transitionIndex) {
    // Find the pair this transition belongs to
    for (int[] pair : BALANCED_PAIRS) {
      if (pair[0] == transitionIndex || pair[1] == transitionIndex) {
        // Find the other transition in the pair (!= transitionIndex)
        int otherTransition = (pair[0] == transitionIndex) ? pair[1] : pair[0];

        // Get the counts for the current transition and the other transition
        int currentCount = transitionCounts.get(transitionIndex);
        int otherCount = transitionCounts.get(otherTransition);

        // Allow firing if the other transition has fired more times or equal
        return otherCount >= currentCount;
      }
    }
    return false; // Should never reach here if isTrackedTransition was true
  }
}
