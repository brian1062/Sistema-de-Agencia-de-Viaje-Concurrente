import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TransitionTest {
  private Transition transition;

  @BeforeEach
  public void setUp() {
    transition = new Transition(0, 10, 6000);
  }

  @Test
  public void testGetName() {
    assertEquals("T0", transition.getName());
  }

  @Test
  public void testGetTime() {
    assertEquals(10, transition.getTime());
  }

  @Test
  public void testGetMaxTime() {
    assertEquals(6000, transition.getMaxTime());
  }

  // @Test
  // public void testSetTime() {
  //   transition.setTime(30);
  //   assertEquals(30, transition.getTime());
  // }

  // @Test
  // public void testSetTimeError() {
  //   try {
  //     transition.setTime(-1);
  //   } catch (IllegalArgumentException e) {
  //     assertEquals("Time cannot be negative", e.getMessage());
  //   }
  // }
}
