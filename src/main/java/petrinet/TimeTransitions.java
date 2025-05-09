package petrinet;

import java.util.Arrays;

public class TimeTransitions {
  long[] timeTransitions; // TODO: ver sino con un mapa alfa
  long[] systemTime;
  //   boolean[]
  //       enabledTransitions; // inicializar con getEnabledTransitionsInBits, ver si combiene
  // cambiar
  //                             // de lugar esto
  boolean[] oldEnabledTransitions;

  public TimeTransitions(long[] timeTransitions) { // , boolean[] enabledTransitions) {
    // this.enabledTransitions = enabledTransitions;
    // this.oldEnabledTransitions = new boolean[enabledTransitions.length];

    this.oldEnabledTransitions = new boolean[timeTransitions.length];
    this.systemTime = new long[timeTransitions.length];
    this.timeTransitions = timeTransitions;

    Arrays.fill(systemTime, Long.MAX_VALUE);
    Arrays.fill(oldEnabledTransitions, false);
  }

  public void setSystemTime(int transitionIndex) {
    systemTime[transitionIndex] = System.currentTimeMillis();
  }

  /**
   * Checks if the time for a specific transition has elapsed.
   *
   * @param transitionIndex Index of the transition to check.
   * @return true if the time has elapsed, false otherwise.
   */
  public boolean checkTime(int transitionIndex) {
    long currentTime = System.currentTimeMillis();
    long elapsedTime = currentTime - systemTime[transitionIndex];
    return elapsedTime >= timeTransitions[transitionIndex];
  }

  /**
   * Checks if the time for a specific transition has elapsed and returns the remaining time.
   *
   * @param transitionIndex Index of the transition to check.
   * @return Remaining time in milliseconds.
   */
  public long getRemainingTime(int transitionIndex) {
    long currentTime = System.currentTimeMillis();
    long elapsedTime = currentTime - systemTime[transitionIndex];
    return timeTransitions[transitionIndex] - elapsedTime;
  }

  /**
   * Starts the timer only for the transitions that have been enabled and were not enabled before.
   *
   * @param enabledTransitions Array indicating which transitions are currently enabled.
   */
  public void updateEnabledTransitionsTimer(boolean[] enabledTransitions) {
    // imprimi el enabledTransitions y el oldEnabledTransitions
    System.out.println("Enabled transitions: " + Arrays.toString(enabledTransitions));
    System.out.println("Old enabled transitions: " + Arrays.toString(oldEnabledTransitions));

    for (int i = 0; i < timeTransitions.length; i++) {
      // todo: AGREGAR caso de T inmediatas que no se le setea tiempo.
      if (!oldEnabledTransitions[i]
          && enabledTransitions[
              i]) { // 0 1 -> 1 1 (estaba desensibilizada, pasa a sensibilizada) timer tiene que
        // empezar
        setSystemTime(i);
      } else if (oldEnabledTransitions[i]
          && !enabledTransitions[
              i]) { // 1 0 (estaba sensibilizada, pasa a desensibilizada) timer tiene que ir a
        // infinito (long.MAX_VALUE)
        systemTime[i] = Long.MAX_VALUE;
      }
    }
    oldEnabledTransitions = enabledTransitions.clone();
  }

  public long getAlpha(int transitionIndex) {
    return timeTransitions[transitionIndex];
  }
}
