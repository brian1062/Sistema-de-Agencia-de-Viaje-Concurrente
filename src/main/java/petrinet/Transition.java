package petrinet;

/**
 * Represents a transition in a timed Petri net. Each transition has a unique identifier, a name,
 * and a delay time.
 */
public class Transition {
  private final String name;
  private final int number;
  private final long delayTime;

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

  public int getNumber() {
    return number;
  }

  public String getName() {
    return name;
  }

  public long getDelayTime() {
    return delayTime;
  }
}
