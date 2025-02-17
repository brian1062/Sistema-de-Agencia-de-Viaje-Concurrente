package policy;

/**
 * First-Come-First-Served (FCFS) policy. This policy allows transitions to fire in the order they
 * arrive.
 */
public class FCFSPolicy extends Policy {

  /**
   * Always returns true
   *
   * @param transitionIndex The index of the transition to check.
   * @return true.
   */
  @Override
  public boolean canFireTransition(int transitionIndex) {
    return true;
  }

  @Override
  public void transitionFired(int transitionIndex) {} // No-op
}
