package petrinet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PetriNet {
  private List<Transition> transitions;
  private List<Place> places;
  private List<Transition> enabledTransitions = new ArrayList<>();
  private int invariantsCount = 0;
  private boolean invariantsTargetAchieved = false;
  private final int invariantsCountTarget;
  private int[][] incidenceMatrixOut;
  private int[][] incidenceMatrixIn;
  private int[][] placesInvariants;
  private int[] marking;
  private final int placesLength;
  private final int LAST_TRANSITION = 11;

  /**
   * Constructor for the PetriNet class.
   *
   * @param transitions
   * @param places
   * @param incidenceMatrixOut
   * @param incidenceMatrixIn
   * @param placesInvariants
   * @param marking
   * @param invariantsCountTarget
   */
  public PetriNet(
      List<Transition> transitions,
      List<Place> places,
      int[][] incidenceMatrixOut,
      int[][] incidenceMatrixIn,
      int[][] placesInvariants,
      int[] marking,
      int invariantsCountTarget) {
    this.transitions = transitions;
    this.places = places;
    this.incidenceMatrixOut = incidenceMatrixOut;
    this.incidenceMatrixIn = incidenceMatrixIn;
    this.placesInvariants = placesInvariants;
    this.marking = marking;
    this.placesLength = places.size();
    this.invariantsCountTarget = invariantsCountTarget;
    updateEnabledTransitions(); // Initialize the enabled transitions
  }

  public boolean tryFireTransition(int transitionIndex) {
    if (!isTransitionEnabled(transitionIndex)) {
      return false;
    }

    // Iterate over all places in the Petri net
    IntStream.range(0, places.size())
        .forEach(
            placeIndex -> {
              // If there is an input arc from the place to the transition
              if (incidenceMatrixIn[placeIndex][transitionIndex] > 0) {
                marking[placeIndex] =
                    marking[placeIndex]
                        - incidenceMatrixIn[placeIndex][
                            transitionIndex]; // Remove tokens from the input places
              }
              // If there is an output arc from the transition to the place
              if (incidenceMatrixOut[placeIndex][transitionIndex] > 0) {
                marking[placeIndex] =
                    marking[placeIndex]
                        + incidenceMatrixOut[placeIndex][
                            transitionIndex]; // Add tokens to the output places
              }
            });

    try {
      checkPlacesInvariants();
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }

    if (transitionIndex == LAST_TRANSITION) {
      invariantsCount++;
      if (invariantsCount == invariantsCountTarget) {
        invariantsTargetAchieved = true;
      }
    }

    // Update the enabled transitions after firing the transition
    updateEnabledTransitions();
    return true;
  }

  /** Returns the current marking of the Petri net. */
  public String getStringMarking() {
    String markingString =
        IntStream.range(0, marking.length)
            .mapToObj(
                placeIndex -> {
                  // String message = "P" + placeIndex + ": " + marking[placeIndex];
                  String message = "" + marking[placeIndex];
                  if (placeIndex != marking.length - 1) {
                    message += ",";
                  }
                  return message;
                })
            .collect(Collectors.joining(" "));

    return markingString;
  }

  /** Updates the list of enabled transitions in the Petri net. */
  private void updateEnabledTransitions() {
    // Clear the enabledTransitions list to remove any previously stored transitions
    enabledTransitions.clear();

    // Iterate over all transitions in the incidence matrix
    IntStream.range(0, incidenceMatrixIn[0].length)
        .filter(
            transitionIndex ->
                IntStream.range(0, placesLength)
                    .allMatch(
                        placeIndex ->
                            marking[placeIndex] >= incidenceMatrixIn[placeIndex][transitionIndex]))
        .mapToObj(transitions::get)
        .forEach(enabledTransitions::add);
  }

  public void checkPlacesInvariants() throws Exception {
    for (int row = 0; row < placesInvariants.length; row++) {
      int sum = 0;
      for (int column = 0; column < placesLength; column++) {
        sum += marking[column] * placesInvariants[row][column];
      }
      if (sum == placesInvariants[row][placesLength]) {
        continue;
      }
      String msgEx = "Fail place invariant " + row + " in Marking: " + getStringMarking();
      throw new Exception(msgEx);
    }
  }

  /**
   * Check if the transition is enabled
   *
   * @param transitionIndex
   * @return true if the transition is enabled, false otherwise
   */
  public boolean isTransitionEnabled(int transitionIndex) {
    validateTransitionIndex(transitionIndex);

    return enabledTransitions.contains(transitions.get(transitionIndex));
  }

  /**
   * Validates the transition index
   *
   * @param transitionIndex
   */
  private void validateTransitionIndex(int transitionIndex) {
    if (transitionIndex < 0 || transitionIndex >= transitions.size()) {
      throw new IllegalArgumentException("Invalid transition index: " + transitionIndex);
    }
  }

  /* Getters */
  public int[] getMarking() {
    return marking;
  }

  public List<Transition> getEnabledTransitions() {
    return enabledTransitions;
  }

  public boolean petriNetHasFinished() {
    return invariantsTargetAchieved;
  }

  public Transition getTransitionFromIndex(int transitionIndex) {
    validateTransitionIndex(transitionIndex);
    return transitions.get(transitionIndex);
  }
}
