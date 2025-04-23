package policy;

/**
 * Policy that balances the firing of two transition pairs. For each pair, the other transition can
 * only fire if it has fired more times or equal to the current transition.
 */
public class BalancedPolicy extends Policy {

  /** Constructs a BalancedPolicy instance and initilizes tracking for balanced transitions. */
  public BalancedPolicy() {
    // Initialize counters for all transitions we're tracking
    for (int[] pair : PAIRS) {
      transitionCounts.put(pair[0], 0);
      transitionCounts.put(pair[1], 0);
    }
  }

  /**
   * Determines whether a transition is allowed to fire based on the balancing policy.
   *
   * @param transitionIndex The index of the transition to check.
   * @return True if the transition can fire, false otherwise.
   */
  // @Override
  // public boolean canFireTransition(int transitionIndex) {
  //  // If not a tracked transition, allow firing
  //  if (!isTrackedTransition(transitionIndex)) {
  //    return true;
  //  }
  //
  //  try {
  //    policyMutex.acquire();
  //    boolean canFire = canFireBalancedTransition(transitionIndex);
  //    policyMutex.release();
  //    return canFire;
  //
  //  } catch (InterruptedException e) {
  //    Thread.currentThread().interrupt();
  //    logger.error("Thread interrupted while acquiring policy mutex");
  //    return false;
  //  }
  // }

  /**
   * Updates the transition count when a transition fires.
   *
   * @param transitionIndex The index of the transition that has fired.
   */
  @Override
  public void transitionFired(int transitionIndex) {
    try {
      policyMutex.acquire();// aca no tenemos un semaforo de mas si aca se viene desde el monitor el cual se puede acceder de un hilo a la vez
      transitionCounts.computeIfPresent(transitionIndex, (key, value) -> value + 1);
      policyMutex.release();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("Thread interrupted while updating transition counts");
    }
  }

  /**
   * Determines whether a balanced transition is allowed to fire based on its paired transition
   * count.
   *
   * @param transitionIndex The transition index to check.
   * @return True if the transition can fire under the defined balancing rules, false otherwise.
   */
  /*private boolean canFireBalancedTransition(int transitionIndex) {
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
  }*/

  @Override
  public boolean canFireTransition(int transitionIndex) {
    // Find the pair of the received transition
    int pairedTransition = getPairedTransition(transitionIndex);

    // Find the count of both transitions
    int currentCount = transitionCounts.get(transitionIndex);
    int pairedCount = transitionCounts.get(pairedTransition);

    // Allow firing if the paired transition has fired more times or equal
    return pairedCount >= currentCount;
  }
}
