package petrinet;

/**
 * Represents a place in a Petri Net. A place holds a certain number of tokens and is connected to
 * transitions via arcs.
 */
public class Place {

  private String name;
  private int tokens;

  /**
   * Constructor for the Place with a specified name and initial number of tokens.
   *
   * @param name The name of the place.
   * @param tokens The initial number of tokens in the place.
   * @throws IllegalArgumentException if tokens are negative.
   */
  public Place(String name, int tokens) {
    this.name = name;
    this.tokens = tokens;
  }

  /* Setters */

  public void setTokens(int tokens) {
    if (tokens < 0) {
      throw new IllegalArgumentException("Tokens cannot be negative");
    }
    this.tokens = tokens;
  }

  /* Getters */

  public int getTokens() {
    return tokens;
  }

  public String getName() {
    return name;
  }
}
