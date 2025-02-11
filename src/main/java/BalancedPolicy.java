import java.util.concurrent.Semaphore;

/*
 * Class that implements a policy to balance the firing of transitions T2 and T3 in a Petri Net.
 */
public class BalancedPolicy {
  private int t2Count = 0;
  private int t3Count = 0;
  private final Semaphore policyMutex = new Semaphore(1, true);

  public boolean canFireTransition(int transitionIndex) {
    try {
      policyMutex.acquire();

      // If it's not T2 or T3, allow firing
      if (transitionIndex != 2 && transitionIndex != 3) {
        policyMutex.release();
        return true;
      }

      boolean canFire = false;

      if (transitionIndex == 2) {
        // Allow firing if T3 has more or equal firings than T2
        canFire = t3Count >= t2Count;
      } else if (transitionIndex == 3) {
        // Allow firing if T2 has more firings than T3
        canFire = t2Count >= t3Count;
      }

      policyMutex.release();
      return canFire;

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

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
    }
  }
}
