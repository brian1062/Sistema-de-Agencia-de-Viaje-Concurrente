package petrinet;

/**
 * Represents a transition in a timed Petri net. Each transition has a unique identifier, a name,
 * and a delay time.
 */
public class Transition {
  private final String name; // Name of the transition, automatically generated as "T<number>".
  private final int number; // Unique number of the transition.
  private final long delayTime; // Delay time.

  /**
   * Constructor for the Transition class.
   *
   * @param number Unique number of the transition.
   * @param delayTime Delay time.
   */
  public Transition(int number, long delayTime) {
    this.number = number;
    this.name = "T" + number;
    this.delayTime = delayTime;
  }

  /* Getters */

  /**
   * Gets the unique numberical identifier of the transition.
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
