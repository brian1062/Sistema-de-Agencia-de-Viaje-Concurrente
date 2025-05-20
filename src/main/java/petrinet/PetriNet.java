package petrinet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import monitor.Monitor;
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
   * @param placesInvariants Matrix representing the invariants of the petri net.
   * @param marking Array representing the current marking of the petri net.
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
    this.timeTransitions = new TimeTransitions(alphas);
  }

  /**
   * Attempts to fire a transition in the Petri Net if it is enabled.
   *
   * @param transitionIndex Index of the transition to fire.
   * @return true if transition fired successfully, false otherwise.
   */
  public boolean tryFireTransition(int transitionIndex) {
    // Check if the transition index is valid
    validateTransitionIndex(transitionIndex);

    if (petriNetHasFinished()) {
      // Return true so that the waiting threads can finish executing
      return true; // TODO: check if this is correct
    }

    // Can't fire the transition if it is not enabled
    if (!isTransitionEnabled(transitionIndex)) {
      return false;
    }

    // Update the marking of the Petri net
    updateMarking(transitionIndex);

    // Verify the marking after firing the transition
    verifyMarking();

    // Log the transition firing
    logger.logTransition(transitionIndex);
    logger.logCurrentMarking(transitionIndex, getStringMarking());

    // Check if the Petri net has finished using the invariants target
    checkAndHandleInvariantsTarget(transitionIndex);

    // Update the enabled transitions after firing the transition
    updateEnabledTransitions();

    // Update timeTransitions
    timeTransitions.updateEnabledTransitionsTimer(getEnabledTransitionsInBits());

    // las nuevas sensibilizadas setele sistemtime // TODO: ??
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
   * Checks if there are any negative tokens in the marking of the Petri net.
   *
   * @throws Exception if the marking contains negative tokens.
   */
  public void currentMarkingNegativeTokens() throws Exception {
    for (int i = 0; i < marking.length; i++) {
      if (marking[i] < 0) {
        String msgEx = "Negative tokens in marking: " + getStringMarking();
        throw new Exception(msgEx);
      }
    }
  }

  /**
   * Checks if a specific transition is enabled in the Petri net. A transition is enabled if all its
   * input places have enough tokens and the transition is enabled in the time transitions.
   *
   * @param transitionIndex Index of the transition to check.
   * @return true if the transition is enabled, false otherwise.
   */
  public boolean isTransitionEnabled(int transitionIndex) {
    // Check if the transition is enabled in the incidence matrix
    if (!enabledTransitions.contains(transitions.get(transitionIndex))) {
      return false;
    }

    // If the transition has no time constraint or is within the time window, it can be fired
    if (timeTransitions.getAlpha(transitionIndex) == 0
        || timeTransitions.checkTime(transitionIndex)) {
      return true;
    }

    // If the transition is not enabled due to time, wait for the time to elapse
    waitForTransitionTime(transitionIndex);

    // Check again if the transition is enabled after waiting
    return enabledTransitions.contains(transitions.get(transitionIndex));
  }

  /**
   * Waits for the time constraint of a transition to elapse.
   *
   * @param transitionIndex The index of the transition to wait for.
   */
  private void waitForTransitionTime(int transitionIndex) {
    Monitor.getMonitor().getMutex().release();

    long timeToWait = timeTransitions.getRemainingTime(transitionIndex);

    try {
      if (timeToWait > 0) {
        Thread.sleep(timeToWait);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Thread interrupted while waiting for transition time", e);
    }

    // Reacquire the mutex after waiting
    try {
      Monitor.getMonitor().getMutex().acquire();
      logger.info("Transition " + transitionIndex + " has been awaken after waiting.");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Thread interrupted while acquiring mutex", e);
    }
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

  /**
   * Checks if the invariants target has been achieved and performs the necessary actions.
   *
   * @param transitionIndex The index of the transition that was fired.
   */
  private void checkAndHandleInvariantsTarget(int transitionIndex) {
    if (transitionIndex == LAST_TRANSITION) {
      invariantsCount++;
      if (invariantsCount == invariantsCountTarget) {
        invariantsTargetAchieved = true;
        System.out.println("[SUCCESS] Invariants target achieved. Terminating program.");
      }
    }
  }

  /**
   * Updates the marking of the Petri net based on the given transition index.
   *
   * @param transitionIndex The index of the transition to fire.
   */
  private void updateMarking(int transitionIndex) {
    IntStream.range(0, places.size())
        .forEach(
            placeIndex -> {
              // If there is an input arc from the place to the transition
              if (incidenceMatrixIn[placeIndex][transitionIndex] > 0) {
                marking[placeIndex] -= incidenceMatrixIn[placeIndex][transitionIndex];
              }
              // If there is an output arc from the transition to the place
              if (incidenceMatrixOut[placeIndex][transitionIndex] > 0) {
                marking[placeIndex] += incidenceMatrixOut[placeIndex][transitionIndex];
              }
            });
  }

  /**
   * Verifies the place invariants and checks for negative tokens in the current marking.
   *
   * @throws RuntimeException if any of the checks fail.
   */
  private void verifyMarking() {
    try {
      checkPlacesInvariants();
    } catch (Exception e) {
      throw new RuntimeException("Place invariants check failed: " + e.getMessage(), e);
    }

    try {
      currentMarkingNegativeTokens();
    } catch (Exception e) {
      throw new RuntimeException("Negative tokens detected in marking: " + e.getMessage(), e);
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

  public boolean[] getEnabledTransitionsInBits() {
    boolean[] enabledTransitionsInBits = new boolean[transitions.size()];
    for (int i = 0; i < transitions.size(); i++) {
      enabledTransitionsInBits[i] = enabledTransitions.contains(transitions.get(i));
    }
    return enabledTransitionsInBits;
  }
}
