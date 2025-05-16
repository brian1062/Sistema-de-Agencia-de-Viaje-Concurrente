package policy;

/**
 * Policy that balances the firing of two transition pairs. For each pair, the other transition can
 * only fire if it has fired more times or equal to the current transition.
 */
public class BalancedPolicy extends Policy {

  /**
   * Determines whether a balanced transition is allowed to fire based on its current proportion.
   *
   * @param transitionIndex The transition index to check.
   * @return true if the transition can fire under the defined priority rules, false otherwise.
   */
  @Override
  protected boolean canFireTransition(int transitionIndex) {
    int pairedTransition = getPairedTransition(transitionIndex);

    // Get the counts for the current transition and the other transition
    int currentCount = transitionCounts.get(transitionIndex);
    int otherCount = transitionCounts.get(pairedTransition);

    // Allow firing if the other transition has fired more times or equal
    return otherCount >= currentCount;
  }
}
