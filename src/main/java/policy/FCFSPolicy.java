package policy;

/**
 * First-Come-First-Served (FCFS) policy. This policy allows transitions to fire in the order they
 * arrive.
 */
public class FCFSPolicy extends Policy {

  /**
   * Always permits transitions to fire (FCFS behavior).
   *
   * @param transitionIndex The index of the transition to check.
   * @return Always returns true, indicating the transition can fire.
   */
  @Override
  public boolean canFireTransition(int transitionIndex) {
    return true;
  }

  /**
   * No-op required for the FCFS policy.
   *
   * @param transitionIndex The index of the transition that was fired.
   */
  @Override
  public void transitionFired(int transitionIndex) {}
}
