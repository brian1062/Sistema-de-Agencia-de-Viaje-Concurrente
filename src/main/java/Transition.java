/**
 * The Transition class represents a transition in a timed Petri net. Each transition has a unique
 * identifier, associated timing values, and a state that indicates whether it is time-sensitized or
 * immediate.
 */
public class Transition {
  private final String name; // Name of the transition, automatically generated as "T<number>".
  private final int number;
   private final long delayTime;

  /**
   * Constructor for the Transition class.
   *
   * @param number Unique number of the transition.
   * @param delayTime Delay time
   */
  public Transition(int number, long delayTime) {
    this.number = number;
    this.name = "T" + number;
    this.delayTime = delayTime;
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
   * Gets the delay time of the transition.
   *
   * @return The delay time in milliseconds.
   */
  public long getDelayTime() {
    return delayTime;
  }
}
