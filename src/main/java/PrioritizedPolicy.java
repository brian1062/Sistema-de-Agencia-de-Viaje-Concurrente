import java.util.HashMap;
import java.util.Map;

public class PrioritizedPolicy extends Policy {
  private final Map<Integer, Integer> transitionCounts = new HashMap<>();

  // Define transition pairs and their target percentages
  // The first transition in each pair is prioritized
  private static final int[][] PRIORITY_PAIRS = {
    {2, 3},  // First pair (T2/T3)
    {6, 7}   // Second pair (T6/T7)
  };

  private static final double[] TARGET_PERCENTAGES = {
    0.75,  // T2 should be 75% of T2+T3
    0.80   // T6 should be 80% of T6+T7
  };

  public PrioritizedPolicy() {
    // Initialize counters for all transitions we're tracking
    for (int[] pair : PRIORITY_PAIRS) {
      transitionCounts.put(pair[0], 0);
      transitionCounts.put(pair[1], 0);
    }
  }

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

  private boolean isTrackedTransition(int transitionIndex) {
    return transitionCounts.containsKey(transitionIndex);
  }

  private boolean canFirePrioritizedTransition(int transitionIndex) {
    // Find which pair this transition belongs to
    for (int i = 0; i < PRIORITY_PAIRS.length; i++) {
      int[] pair = PRIORITY_PAIRS[i];
      if (pair[0] == transitionIndex || pair[1] == transitionIndex) {
        int priorityTransition = pair[0];  // First transition in pair is prioritized
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
    return false;  // Should never reach here if isTrackedTransition was true
  }
}
