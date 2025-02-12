/*
 * Class that implements a policy to prioritize the firing of transitions T2 and T6 in the Petri Net
 */
public class PrioritizedPolicy extends Policy {
  private int t2Count = 0;
  private int t3Count = 0;
  private int t6Count = 0;
  private int t7Count = 0;

  private static final double T2_TARGET_PERCENTAGE = 0.75; // 75% for T2
  private static final double T6_TARGET_PERCENTAGE = 0.80; // 80% for T6

  @Override
  public boolean canFireTransition(int transitionIndex) {
    try {
      policyMutex.acquire();

      boolean canFire = false;

      // Handle T2/T3 priority
      if (transitionIndex == 2 || transitionIndex == 3) {
        int totalT2T3 = t2Count + t3Count;

        if (totalT2T3 == 0) {
          canFire = true;  // Allow first firing
        } else {
          double currentT2Percentage = (double) t2Count / totalT2T3;

          if (transitionIndex == 2) {
            canFire = currentT2Percentage < T2_TARGET_PERCENTAGE;
          } else {  // T3
            canFire = currentT2Percentage >= T2_TARGET_PERCENTAGE;
          }
        }
      }
      // Handle T6/T7 priority
      else if (transitionIndex == 6 || transitionIndex == 7) {
        int totalT6T7 = t6Count + t7Count;

        if (totalT6T7 == 0) {
          canFire = true;  // Allow first firing
        } else {
          double currentT6Percentage = (double) t6Count / totalT6T7;

          if (transitionIndex == 6) {
            canFire = currentT6Percentage < T6_TARGET_PERCENTAGE;
          } else {  // T7
            canFire = currentT6Percentage >= T6_TARGET_PERCENTAGE;
          }
        }
      }
      // Any other transition can fire freely
      else {
        canFire = true;
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

      switch (transitionIndex) {
        case 2 -> t2Count++;
        case 3 -> t3Count++;
        case 6 -> t6Count++;
        case 7 -> t7Count++;
      }

      policyMutex.release();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("Thread interrupted while updating transition counts");
    }
  }
}