package policy;

import java.util.HashMap;
import java.util.Map;

/**
 * Policy that prioritizes certain transitions in a Petri Net based on predefined target
 * percentages. This policy ensures that specific transitions fire with a target proportion relative
 * to their paired transitions.
 */
public class PrioritizedPolicy extends Policy {

  private static final Map<Integer, Float> transitionPercentage = new HashMap<>();

  /** Constructs a PrioritizedPolicy and initializes transitionPercentage map. */
  public PrioritizedPolicy() {
    transitionPercentage.put(2, 0.75f);
    transitionPercentage.put(3, 0.25f);
    transitionPercentage.put(6, 0.8f);
    transitionPercentage.put(7, 0.2f);
  }

  /**
   * Determines whether the current transition is allowed to fire based on its current percentage.
   *
   * @param transitionIndex The transition index to check.
   * @return true if the transition can fire under the defined priority rules, false otherwise.
   */
  @Override
  protected boolean canFireTransition(int transitionIndex) {
    // Find which pair this transition belongs to
    int pairedTransition = getPairedTransition(transitionIndex);

    float targetPercentage = transitionPercentage.get(transitionIndex);

    int currentCount = transitionCounts.get(transitionIndex);
    int pairedCount = transitionCounts.get(pairedTransition);
    int totalCount = currentCount + pairedCount;

    // Allow first firing
    if (totalCount == 0) {
      return true;
    }

    float currentPriorityPercentage = (float) currentCount / totalCount;

    return currentPriorityPercentage <= targetPercentage;
  }
}
