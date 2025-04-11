package policy;

import java.util.List;
import java.util.concurrent.Semaphore;
import utils.Logger;

/**
 * Abstract class that defines the structure of a policy to control the firing of transitions in the
 * Petri Net. Provides mutual exclusion using a semaphore to ensure that only one thread can fire a
 * transition at a time.
 */
public abstract class Policy {
  /**
   * Semaphore to ensure thread-safe policy operations. Protects access to policy state during
   * transition evaluation and firing.
   */
  protected final Semaphore policyMutex;

  /** Shared logger instance for logging policy-related events. */
  protected static Logger logger = Logger.getLogger();

  /** Constructor for the Policy class with synchronization mechanism. */
  protected Policy() {
    this.policyMutex = new Semaphore(1, true);
  }

  /**
   * Determines if a transition can be fired based on the policy rules.
   *
   * @param transitionIndex Index of the transition to evaluate.
   * @return true if the transition can be fired, false otherwise.
   */
  public abstract boolean canFireTransition(int transitionIndex);

  /**
   * Updates the policy state after a transition has been fired.
   *
   * @param transitionIndex Index of the transition that was fired.
   */
  public abstract void transitionFired(int transitionIndex);

/**
 * Abstract class that defines the structure of a policy to control the firing of transitions.
 * @param enabledTransitions
 * @return
 */
public abstract List<Integer> getPreferedTransitions(List<Integer> enabledTransitions);
}