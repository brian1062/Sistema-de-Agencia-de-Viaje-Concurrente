import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TransitionTest {
  private Transition transition;

  @BeforeEach
  public void setUp() {
    transition = new Transition(0, 10);
  }

  @Test
  public void testGetName() {
    assertEquals("T0", transition.getName());
  }

  @Test
  public void testGetTime() {
    assertEquals(10, transition.getDelayTime());
  }
}
