/**
 * The Transition class represents a transition in a timed Petri net.
 * Each transition has a unique identifier, associated timing values, and a state
 * that indicates whether it is time-sensitized or immediate.
 */
public class Transition {
  private final String name; // Name of the transition, automatically generated as "T<number>".
  private final int number;  // Unique number of the transition.
  private final long delayTime; // Delay time before the transition is enabled (alpha).
  private final long maxTime;   // Maximum time allowed for the transition to fire (beta).
  private long runningTime = 0; // Time when the transition was activated (with tokens in places), used to calculate remaining time.
  private boolean isSensitizeInTime;  // Indicates whether the transition is sensitized in time.
  private boolean isImmediate; // Indicates if the transition is immediate (no delay).

  /**
   * Constructor for the Transition class.
   * 
   * @param number     Unique number of the transition.
   * @param delayTime  Delay time before the transition is enabled (alpha).
   * @param maxTime    Maximum time allowed for the transition to fire (beta).
   */
  public Transition(int number, long delayTime, long maxTime) {
    this.number = number;
    this.name = "T" + number;

    this.delayTime = delayTime;
    this.maxTime = maxTime;
    this.isImmediate = delayTime == 0;
    this.isSensitizeInTime = isImmediate;
  }

  /**
   * Sensitizes the transition in time.
   * If the transition is not immediate, sets the current time as the start time.
   */
  private void sensitizeTime(){
    if(!isImmediate){
      this.runningTime = System.currentTimeMillis();
    }
  }

  /**
   * Desensitizes the transition in time.
   * Resets the start time to zero.
   */
  private void deSensitizeTime(){
      this.runningTime = 0;
  }

  /**
   * Returns whether the transition is immediate (no delay).
   * 
   * @return {@code true} if the transition is immediate, {@code false} otherwise.
   */
  public boolean isImmediate() {
    return isImmediate;
  }

  /**
   * Gets the unique number of the transition.
   * 
   * @return The transition's number.
   */
  public int getNumber() {
    return number;
  }

  /**
   * Gets the name of the transition (format "T<number>").
   * 
   * @return The transition's name.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the delay time (alpha) of the transition.
   * 
   * @return The delay time in milliseconds.
   */
  public long getTime() {
    return delayTime;
  }

  /**
   * Gets the maximum time allowed for the transition to fire (beta).
   * 
   * @return The maximum time in milliseconds.
   */
  public long getMaxTime() {
    return maxTime;
  }

  /**
   * Checks if the transition is sensitized in time.
   * 
   * @return {@code true} if the transition is sensitized in time, {@code false} otherwise.
   */
  public boolean isSensitizeInTime(){
    return isSensitizeInTime;
  }

  /**
   * Calculates the remaining time for the transition to be fired.
   * 
   * @return The remaining time in milliseconds. If negative, the transition has exceeded its allowed time.
   */
  public long getRemainingTime(){
    return delayTime - (System.currentTimeMillis()-runningTime);
  }
}
