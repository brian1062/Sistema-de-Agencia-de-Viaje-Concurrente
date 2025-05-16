import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import petrinet.PetriNetConf;
import petrinet.Place;
import petrinet.Transition;

class PetriNetConfTest {
  private PetriNetConf petriNetConf;

  @BeforeEach
  void setUp() {
    petriNetConf = new PetriNetConf();
  }

  @Test
  void testInitialMarking() {
    int[] expectedInitialMarking = {5, 1, 0, 0, 5, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0};
    assertThat(petriNetConf.getInitialMarking()).containsExactly(expectedInitialMarking);
  }

  @Test
  void testIncidenceMatrixOut() {
    int[][] incidenceMatrixOut = petriNetConf.getIncidenceMatrixOut();
    assertThat(incidenceMatrixOut).isNotNull();
    assertThat(incidenceMatrixOut.length).isEqualTo(15); // Number of places
    assertThat(incidenceMatrixOut[0].length).isEqualTo(12); // Number of transitions
  }

  @Test
  void testIncidenceMatrixIn() {
    int[][] incidenceMatrixIn = petriNetConf.getIncidenceMatrixIn();
    assertThat(incidenceMatrixIn).isNotNull();
    assertThat(incidenceMatrixIn.length).isEqualTo(15); // Number of places
    assertThat(incidenceMatrixIn[0].length).isEqualTo(12); // Number of transitions
  }

  @Test
  void testPlacesInitialization() {
    List<Place> places = petriNetConf.getPlaces();
    assertThat(places).hasSize(15);
    assertThat(places.get(0).getName()).isEqualTo("P0");
    assertThat(places.get(0).getTokens()).isEqualTo(5);
    assertThat(places.get(1).getName()).isEqualTo("P1");
    assertThat(places.get(1).getTokens()).isEqualTo(1);
  }

  @Test
  void testTransitionsInitialization() {
    List<Transition> transitions = petriNetConf.getTransitions();
    assertThat(transitions).hasSize(12);
    assertThat(transitions.get(0).getNumber()).isEqualTo(0);
    assertThat(transitions.get(0).getDelayTime()).isEqualTo(0);
    assertThat(transitions.get(1).getNumber()).isEqualTo(1);
  }

  @Test
  void testGetSequenceValidIndex() {
    List<Transition> sequence = petriNetConf.getTransitionSequence(0);
    assertThat(sequence).hasSize(2);
    assertThat(sequence.get(0).getNumber()).isEqualTo(0); // T0
    assertThat(sequence.get(1).getNumber()).isEqualTo(1); // T1
  }

  @Test
  void testGetSequenceInvalidIndex() {
    assertThatThrownBy(() -> petriNetConf.getTransitionSequence(-1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Index for TRANSITIONS_THREADS invalid");

    assertThatThrownBy(() -> petriNetConf.getTransitionSequence(15))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Index for TRANSITIONS_THREADS invalid");
  }

  @Test
  void testNumberOfSequences() {
    int numberOfSequences = petriNetConf.getNumberOfSequences();
    assertThat(numberOfSequences).isEqualTo(10); // 10 threads in TRANSITIONS_THREADS
  }
}
