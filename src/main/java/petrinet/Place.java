package petrinet;
public class Place {

  private String name;
  private int tokens;

  public Place(String name, int tokens) {
    this.name = name;
    this.tokens = tokens;
  }

  // Setters
  public void setTokens(int tokens) {
    if (tokens < 0) {
      throw new IllegalArgumentException("Tokens cannot be negative");
    }
    this.tokens = tokens;
  }

  // Getters
  public int getTokens() {
    return tokens;
  }

  public String getName() {
    return name;
  }
}
