package petrinet;

public class TimeTransitions {
    long[] timeTransitions; //TODO: ver sino con un mapa
    long[] systemTime;
    boolean[] sensitizedTransition;     //inicializar con getEnabledTransitionsInBits, ver si combiene cambiar de lugar esto
    boolean[] oldSensitizedTransition;

    public TimeTransitions(long[] timeTransitions, boolean[] sensitizedTransition) {
        this.sensitizedTransition = sensitizedTransition;
        this.oldSensitizedTransition = new boolean[sensitizedTransition.length];
        this.oldSensitizedTransition = sensitizedTransition.clone();
        this.timeTransitions = timeTransitions;
        this.systemTime = new long[timeTransitions.length];
        
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
    

}
