import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import petrinet.Place;

public class PlaceTest {
  private Place place;

  @BeforeEach
  public void setUp() {
    place = new Place("P0", 3);
  }

  @Test
  public void testGetTokens() {
    assertEquals(3, place.getTokens());
  }

  @Test
  public void testSetTokens() {
    place.setTokens(30);
    assertEquals(30, place.getTokens());
  }

  @Test
  public void testSetTokensError() {
    try {
      place.setTokens(-1);
    } catch (IllegalArgumentException e) {
      assertEquals("Tokens cannot be negative", e.getMessage());
    }
  }

  @Test
  public void testGetName() {
    assertEquals("P0", place.getName());
  }
}
