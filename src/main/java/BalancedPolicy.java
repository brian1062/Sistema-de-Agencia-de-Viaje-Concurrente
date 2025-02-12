/*
 * Class that implements a policy to balance the firing of transitions T2 and T3 in a Petri Net.
 */
public class BalancedPolicy extends Policy {
  private int t2Count = 0;
  private int t3Count = 0;

  @Override
  public boolean canFireTransition(int transitionIndex) {
    try {
      policyMutex.acquire();

      // If not T2 or T3, allow firing
      if (transitionIndex != 2 && transitionIndex != 3) {
        policyMutex.release();
        return true;
      }

      boolean canFire = false;

      if (transitionIndex == 2) {
        // Allow T2 if T3 has more or equal firings
        canFire = t3Count >= t2Count;
      } else if (transitionIndex == 3) {
        // Allow T3 if T2 has more or equal firings
        canFire = t2Count >= t3Count;
      }

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

      if (transitionIndex == 2) {
        t2Count++;
      } else if (transitionIndex == 3) {
        t3Count++;
      }

      policyMutex.release();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("Thread interrupted while updating transition counts");
    }
  }
}
