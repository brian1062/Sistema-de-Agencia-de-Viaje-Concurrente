package petrinet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Configuration class for a specific Petri Net implementation. Contains all the static definitions
 * for places, transitions, matrices, and sequences needed to construct the Petri Net.
 */
public class PetriNetConf {
  private static final int[] INITIAL_MARKING = {5, 1, 0, 0, 5, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0};
  private final List<Place> places = new ArrayList<>();
  private final List<Transition> transitions = new ArrayList<>();
  // Target number of invariants to reach.
  private final int TARGET_INVARIANTS = 186;

  private static final int[][] INCIDENCE_MATRIX_OUT = { // I+
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, // P0
    {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // P1
    {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // P2
    {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // P3
    {0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0}, // P4
    {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // P5
    {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0}, // P6
    {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0}, // P7
    {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0}, // P8
    {0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0}, // P9
    {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0}, // P10
    {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, // P11
    {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0}, // P12
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0}, // P13
    {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0} // P14
  };

  private static final int[][] INCIDENCE_MATRIX_IN = { // I-
    {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // P0
    {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // P1
    {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // P2
    {0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0}, // P3
    {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // P4
    {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0}, // P5
    {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // P6
    {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0}, // P7
    {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0}, // P8
    {0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0}, // P9
    {0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0}, // P10
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0}, // P11
    {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0}, // P12
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0}, // P13
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1} // P14
  };

  /**
   * Matrix defining the places invariants for the Petri net. Each row represents an invariant, with
   * the columns being the expected sum.
   */
  private static final int[][] INVARIANTS_P_MATRIX = {
    {0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, // M(P1) + M(P2) = 1
    {0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5}, // M(P2) + M(P3) + M(P4) = 5
    {0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1}, // M(P5) + M(P6) = 1
    {0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1}, // M(P7) + M(P8) = 1
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 1}, // M(P10) + M(P11) + M(P12) + M(P13) = 1
    {
      1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 1, 1, 1, 1, 5
    } // M(P0)+M(P2)+M(P3)+M(P5)+M(P8)+M(P9)+M(P11)+M(P12)+M(P13)+M(P14)=5
  };

  /** Time delay (in minutes) for each transition. Index corresponds to the transition number. */
  private static final long[] TIME_TRANSITION = {
    0, // T0 (inmediate)
    2, // T1 (2 minutes getting in the agency)
    0, // T2 (inmediate)
    0, // T3 (inmediate)
    15, // T4 (15 minutes making the reservation)
    15, // T5 (15 minutes making the reservation)
    0, // T6 (inmediate)
    0, // T7 (inmediate)
    5, // T8 (5 minutes receiving the cancellation)
    5, // T9 (5 minutes receiving confirmation of the reservation)
    15, // T10 (15 minutes paying the reservation)
    0 // T11 (inmediate)
  };

  // Threads recommended by algorithm
  // private static final int[][] TRANSITIONS_THREADS = {
  //   {0, 1}, // Thread 0
  //   {2, 5}, // Thread 1
  //   {3, 4}, // Thread 2
  //   {6, 9, 10}, // Thread 3
  //   {7, 8}, // Thread 4
  //   {11} // Thread 5
  // };

  // One thread per transition
  // private static final int[][] TRANSITIONS_THREADS = {
  //   {0}, // Thread 0
  //   {1}, // Thread 1
  //   {2}, // Thread 2
  //   {5}, // Thread 3
  //   {3}, // Thread 4
  //   {4}, // Thread 5
  //   {6}, // Thread 6
  //   {9}, // Thread 7
  //   {10}, // Thread 8
  //   {7}, // Thread 9
  //   {8}, // Thread 10
  //   {11} // Thread 11
  // };

  // Add threads on conflicts
  private static final int[][] TRANSITIONS_THREADS = {
    {0, 1}, // Thread 0
    {2}, // Thread 1
    {5}, // Thread 2
    {3}, // Thread 3
    {4}, // Thread 4
    {9, 10}, // Thread 5
    {6}, // Thread 6
    {7}, // Thread 7
    {8}, // Thread 8
    {11} // Thread 9
  };

  /**
   * Constructor for the PetriNetConf instance. Initializes the places and transitions based on the
   * configuration matrices.
   */
  public PetriNetConf() {
    // Initialize places list with their name and corresponding number of tokens
    IntStream.range(0, INITIAL_MARKING.length)
        .mapToObj(i -> new Place("P" + i, INITIAL_MARKING[i]))
        .forEach(places::add);

    // Initialize transitions list
    IntStream.range(0, INCIDENCE_MATRIX_IN[0].length)
        .mapToObj(i -> new Transition(i, TIME_TRANSITION[i]))
        .forEach(transitions::add);
  }

  /* Getters */

  public int[] getInitialMarking() {
    return INITIAL_MARKING.clone();
  }

  public int[][] getIncidenceMatrixOut() {
    return INCIDENCE_MATRIX_OUT.clone();
  }

  public int[][] getIncidenceMatrixIn() {
    return INCIDENCE_MATRIX_IN.clone();
  }

  public int[][] getPlacesInvariants() {
    return INVARIANTS_P_MATRIX.clone();
  }

  public List<Place> getPlaces() {
    return places;
  }

  public List<Transition> getTransitions() {
    return transitions;
  }

  public List<Transition> getTransitionSequence(int sequenceNumber) {
    if (sequenceNumber < 0 || sequenceNumber >= TRANSITIONS_THREADS.length) {
      throw new IllegalArgumentException("Index for TRANSITIONS_THREADS invalid");
    }

    List<Transition> sequence = new ArrayList<>();
    int[] transitionIndex = TRANSITIONS_THREADS[sequenceNumber];

    for (int tIndex : transitionIndex) {
      sequence.add(transitions.get(tIndex));
    }

    return sequence;
  }

  public int getNumberOfSequences() {
    return TRANSITIONS_THREADS.length;
  }

  public int getTargetInvariants() {
    return TARGET_INVARIANTS;
  }

  public long[] getTimeTransitions() {
    return TIME_TRANSITION;
  }
}
