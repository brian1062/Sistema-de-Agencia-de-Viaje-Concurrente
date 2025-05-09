package petrinet;

public class TimeTransitions {
  long[] timeTransitions; // TODO: ver sino con un mapa alfa
  long[] systemTime;
//   boolean[]
//       sensitizedTransition; // inicializar con getEnabledTransitionsInBits, ver si combiene cambiar
//                             // de lugar esto
  boolean[] oldSensitizedTransition;

  public TimeTransitions(long[] timeTransitions, boolean[] sensitizedTransition) {
    // this.sensitizedTransition = sensitizedTransition;
    // this.oldSensitizedTransition = new boolean[sensitizedTransition.length];
    this.oldSensitizedTransition = sensitizedTransition.clone();
    this.timeTransitions = timeTransitions;
    this.systemTime = new long[timeTransitions.length];
    //TODO inicializar tambien el contador de systemTime
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
   */  
  public void updateEnabledTransitionsTimer(boolean[] sensitizedTransition) {
    for (int i = 0; i < timeTransitions.length; i++) {
      //todo: AGREGAR caso de T inmediatas que no se le setea tiempo.
      if (!oldSensitizedTransition[i] && sensitizedTransition[i]) { // 0 1 -> 1 1 (estaba desensibilizada, pasa a sensibilizada) timer tiene que empezar
        setSystemTime(i);
      }
      else if (oldSensitizedTransition[i] && !sensitizedTransition[i]) { // 1 0 (estaba sensibilizada, pasa a desensibilizada) timer tiene que ir a 0
        systemTime[i] = Long.MAX_VALUE;
      }

    }
    oldSensitizedTransition = sensitizedTransition.clone();
  }
  
}
