package policy;

/**
 * First-Come-First-Served (FCFS) policy. This policy allows transitions to fire in the order they
 * arrive.
 */
public class FCFSPolicy extends Policy {

  /**
   * Always allow transitions to fire (FCFS behavior).
   *
   * @param transitionIndex The index of the transition to check.
   * @return Always returns true, indicating the transition can fire.
   */
  @Override
  public boolean canFireTransition(int transitionIndex) {
    return true;
  }
}
