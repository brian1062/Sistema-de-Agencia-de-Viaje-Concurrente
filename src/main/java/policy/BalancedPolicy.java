package policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Policy that balances the firing of two transition pairs. For each pair, the other transition can
 * only fire if it has fired more times or equal to the current transition.
 */
public class BalancedPolicy extends Policy {
  /** Map to store transition pair counts. */
  private final Map<Integer, Integer> transitionCounts = new HashMap<>();

  /**
   * Transitions pairs requiring balancing firing. Each sub-array contains two transition indices
   * that should be balanced.
   */
  private static final int[][] BALANCED_PAIRS = {
    {2, 3}, // First pair to balance
    {6, 7} // Second pair to balance
  };

  /** Constructs a BalancedPolicy instance and initilizes tracking for balanced transitions. */
  public BalancedPolicy() {
    // Initialize counters for all transitions we're tracking
    for (int[] pair : BALANCED_PAIRS) {
      transitionCounts.put(pair[0], 0);
      transitionCounts.put(pair[1], 0);
    }
  }

  /**
   * Returns a list of preferred transitions based on the current policy. This method filters the
   * enabled transitions to include only those that are tracked and can fire according to the
   * balancing rules.
   *
   * @param enabledTransitions List of currently enabled transition indices.
   * @return List of preferred transition indices.
   */
  @Override
  public List<Integer> getPreferedTransitions(List<Integer> enabledTransitions) {
    List<Integer> preferred = new ArrayList<>();
    try {
      policyMutex.acquire();
      for (int t : enabledTransitions) {
        if (isTrackedTransition(t) && canFireBalancedTransition(t)) {
          preferred.add(t);
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("Thread interrupted while getting preferred transitions");
    } finally {
      policyMutex.release();
    }
    return preferred;
  }

  /**
   * Determines whether a transition is allowed to fire based on the balancing policy.
   *
   * @param transitionIndex The index of the transition to check.
   * @return True if the transition can fire, false otherwise.
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
   * @return True if the transition is tracked, false otherwise.
   */
  private boolean isTrackedTransition(int transitionIndex) {
    return transitionCounts.containsKey(transitionIndex);
  }

  /**
   * Determines whether a balanced transition is allowed to fire based on its paired transition
   * count.
   *
   * @param transitionIndex The transition index to check.
   * @return True if the transition can fire under the defined balancing rules, false otherwise.
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
