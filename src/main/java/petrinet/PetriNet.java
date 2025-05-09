package petrinet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import utils.Logger;

/**
 * Represents a Petri Net structure with places, transitions. Manages the state of the Petri Net
 * including the marking and enabled transitions.
 */
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
  private static Logger logger = Logger.getLogger();
  private TimeTransitions timeTransitions;

  /**
   * Constructor for the PetriNet class with the specified parameters.
   *
   * @param transitions List of transitions in the Petri net.
   * @param places List of places in the Petri net.
   * @param incidenceMatrixOut Output incidence matrix ofthe Petri net.
   * @param incidenceMatrixIn Input incidence matrix of the Petri net.
   * @param placesInvariants Matrix representing the invariants of the Petri net.
   * @param marking Array representing the current marking of the Petri net.
   * @param invariantsCountTarget Target count of invariants to achieve.
   */
  public PetriNet(
      List<Transition> transitions,
      List<Place> places,
      int[][] incidenceMatrixOut,
      int[][] incidenceMatrixIn,
      int[][] placesInvariants,
      int[] marking,
      int invariantsCountTarget,
      long[] alphas) {
    this.transitions = transitions;
    this.places = places;
    this.incidenceMatrixOut = incidenceMatrixOut;
    this.incidenceMatrixIn = incidenceMatrixIn;
    this.placesInvariants = placesInvariants;
    this.marking = marking;
    this.placesLength = places.size();
    this.invariantsCountTarget = invariantsCountTarget;
    updateEnabledTransitions(); // Initialize the enabled transitions
    this.timeTransitions =
        new TimeTransitions(
            alphas, getEnabledTransitionsInBitsBooleans()); // TODO cambiar por lo que va
  }

  /**
   * Attempts to fire a transition in the Petri Net if it is enabled.
   *
   * @param transitionIndex Index of the transition to fire.
   * @return true if transition fired successfully, false otherwise.
   */
  public boolean tryFireTransition(int transitionIndex) {
    if (!isTransitionEnabled(transitionIndex)) {
      return false;
    }

    // Check if the time for the transition has elapsed
    if (!timeTransitions.checkTime(transitionIndex)) {
      // TODO: cambiar esto
      // mutex.release();
      // sleep(timeTransitions.getRemainingTime(transitionIndex));
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

    logger.logTransition(transitionIndex);
    logger.logCurrentMarking(transitionIndex, getStringMarking());

    if (transitionIndex == LAST_TRANSITION) {
      invariantsCount++;
      if (invariantsCount == invariantsCountTarget) {
        invariantsTargetAchieved = true;
        System.out.println("[SUCCESS] Invariants target achieved. Terminating program.");
        System.exit(0); // TODO: Handle this more gracefully
      }
    }
    // Update the enabled transitions after firing the transition
    updateEnabledTransitions();

    // update timeTransitions
    timeTransitions.updateEnabledTransitionsTimer(getEnabledTransitionsInBitsBooleans());
    // las nuevas sensibilizadas setele sistemtime
    return true;
  }

  /**
   * Gets the current marking of the Petri net as a formatted string.
   *
   * @return String representing the current marking.
   */
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

  /** Updates the list of enabled transitions in the Petri net based on the current marking. */
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

  /**
   * Checks the place invariants of the Petri net based on the current marking.
   *
   * @throws Exception if the place invariant check fails.
   */
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
   * Check if the transition is currently enabled.
   *
   * @param transitionIndex Index of the transition to check.
   * @return true if the transition is enabled, false otherwise.
   */
  public boolean isTransitionEnabled(int transitionIndex) {
    validateTransitionIndex(transitionIndex);

    return enabledTransitions.contains(transitions.get(transitionIndex));
  }

  /**
   * Validates the transition index is within the bounds.
   *
   * @param transitionIndex Index of the transition to validate.
   * @throws IllegalArgumentException if the transition index is invalid.
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

  public int getNumberOfTransitions() {
    return transitions.size();
  }

  public int getPlacesLength() {
    return placesLength;
  }

  public int[] getEnabledTransitionsInBits() {
    int[] enabledTransitionsInBits = new int[transitions.size()];
    for (int i = 0; i < transitions.size(); i++) {
      enabledTransitionsInBits[i] = isTransitionEnabled(i) ? 1 : 0;
    }
    return enabledTransitionsInBits;
  }

  public boolean[] getEnabledTransitionsInBitsBooleans() {
    boolean[] enabledTransitionsInBits = new boolean[transitions.size()];
    for (int i = 0; i < transitions.size(); i++) {
      enabledTransitionsInBits[i] = isTransitionEnabled(i);
    }
    return enabledTransitionsInBits;
  }
}
