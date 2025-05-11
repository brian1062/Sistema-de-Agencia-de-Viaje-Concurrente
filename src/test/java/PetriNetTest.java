import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import petrinet.PetriNet;
import petrinet.PetriNetConf;
import petrinet.Transition;

public class PetriNetTest {

  private PetriNet petriNet;
  private static final int[] INITIAL_MARKING = {5, 1, 0, 0, 5, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0};

  @BeforeEach
  public void setUp() {
    PetriNetConf conf = new PetriNetConf();
    petriNet =
        new PetriNet(
            conf.getTransitions(),
            conf.getPlaces(),
            conf.getIncidenceMatrixOut(),
            conf.getIncidenceMatrixIn(),
            conf.getPlacesInvariants(),
            conf.getInitialMarking(),
            10,
            conf.getTimeTransitions());
  }

  @Test
  public void testInitialMarking() {
    assertArrayEquals(INITIAL_MARKING, petriNet.getMarking());
  }

  @Test
  public void testEnabledTransitions() {
    Transition expectedTransition = new Transition(0, 0);
    assertEquals(
        expectedTransition.getName(), petriNet.getEnabledTransitions().getFirst().getName());
  }

  @Test
  public void testFailFireTransitions() {
    int[] actualMarking = petriNet.getMarking();
    petriNet.tryFireTransition(10);
    assertArrayEquals(actualMarking, petriNet.getMarking());
  }

  @Test
  public void testFireTransitions() {
    petriNet.tryFireTransition(0);
    int[] newMarking = {4, 0, 1, 0, 4, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0};
    assertArrayEquals(newMarking, petriNet.getMarking());
  }
}