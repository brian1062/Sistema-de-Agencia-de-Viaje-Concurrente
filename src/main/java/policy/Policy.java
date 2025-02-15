package policy;
import java.util.concurrent.Semaphore;

import utils.Logger;

/*
 * Abstract class that defines the structure of a policy to control the firing of transitions in a Petri Net.
 */
public abstract class Policy {
  /** Semaphore to ensure mutual exclusion in transition firing policies. */
  protected final Semaphore policyMutex;

  protected static Logger logger = Logger.getLogger();

  protected Policy() {
    this.policyMutex = new Semaphore(1, true);
  }

  public abstract boolean canFireTransition(int transitionIndex);

  public abstract void transitionFired(int transitionIndex);
}
