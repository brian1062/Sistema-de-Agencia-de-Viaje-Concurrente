package petrinet;

import java.util.Arrays;
/**
 * Manages timing constraints for time transitions in the Petri net. Keeps track of the activation
 * time of transitions and determines whether a transition's required waiting time (alpha) has
 * elapsed.
 */
public class TimeTransitions {
  /** Array storing the required waiting time (alpha) for each transition, in milliseconds. */
  long[] timeTransitions;

  /** Array storing the system timestamp when each transition was last enabled. */
  long[] systemTime;

  /**
   * Array that stores the previous state of enabled transitions to detect changes in sensitivity.
   * Used to determine when to start or reset a transition's timer.
   */
  boolean[] oldEnabledTransitions;

  /**
   * Constructs a new {TimeTransitions} instance with the specified waiting times per transition.
   *
   * @param timeTransitions Array representing the waiting time (alpha) for each transition.
   */
  public TimeTransitions(long[] timeTransitions) {
    this.oldEnabledTransitions = new boolean[timeTransitions.length];
    this.systemTime = new long[timeTransitions.length];
    this.timeTransitions = timeTransitions;

    Arrays.fill(systemTime, Long.MAX_VALUE);
    Arrays.fill(oldEnabledTransitions, false);
  }

  /**
   * Sets the system time for the specified transition to the current time.
   *
   * @param transitionIndex Index of the transition to update.
   */
  public void setSystemTime(int transitionIndex) {
    systemTime[transitionIndex] = System.currentTimeMillis();
  }

  /**
   * Sets the system time of the specified transition to the maximum possible value. Used to
   * indicate that the transition is currently not sensitive.
   *
   * @param transitionIndex Index of the transition to reset.
   */
  public void setMaxTime(int transitionIndex) {
    systemTime[transitionIndex] = Long.MAX_VALUE;
  }

  /**
   * Checks if the time for a specific transition has elapsed.
   *
   * @param transitionIndex Index of the transition to check.
   * @return true if the time has elapsed, false otherwise.
   */
  public boolean checkTime(int transitionIndex) {
    return System.currentTimeMillis() - systemTime[transitionIndex] >= timeTransitions[transitionIndex];
  }

  /**
   * Checks if the time for a specific transition has elapsed and returns the remaining time.
   *
   * @param transitionIndex Index of the transition to check.
   * @return Remaining time in milliseconds.
   */
  public long getRemainingTime(int transitionIndex) {
    return timeTransitions[transitionIndex] - (System.currentTimeMillis() - systemTime[transitionIndex]);
  }

  /**
   * Updates the timing logic based on the newly enabled transitions. Starts the timer for newly
   * enabled transitions and resets the timer for transitions that were disabled.
   *
   * @param enabledTransitions Boolean array indicating currently enabled transitions.
   */
  public void updateEnabledTransitionsTimer(boolean[] enabledTransitions) {

    for (int i = 0; i < timeTransitions.length; i++) {
      if (!oldEnabledTransitions[i] && enabledTransitions[i]) {
        // 0 1 -> 1 1 // Transition just became enabled — start timer
        setSystemTime(i);
      } else if (oldEnabledTransitions[i] && !enabledTransitions[i]) {
        // Transition just became disabled — reset timer
        systemTime[i] = Long.MAX_VALUE;
      }
    }
    oldEnabledTransitions = enabledTransitions.clone();
  }

  /**
   * Returns the alpha value (waiting time) for a specific transition.
   *
   * @param transitionIndex Index of the transition.
   * @return The alpha value in milliseconds.
   */
  public long getAlpha(int transitionIndex) {
    return timeTransitions[transitionIndex];
  }
}
