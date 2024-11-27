import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
  private BufferedWriter logWriter;
  private final int invariantsCountTarget;
  private int[][] incidenceMatrixOut;
  private int[][] incidenceMatrixIn;
  private int[][] placesInvariants;
  private int[] marking;
  private final int placesLength;
  private final int LAST_TRANSITION = 11;
  private final String LOG_PATH = "/tmp/transitionsSequence.txt";

  // PolicyFactory policyFactory = new PolicyFactory(new PolicyBalancedType());
  // PetriNet petriNet;

  /**
   * Constructor for the PetriNet class.
   *
   * @param transitions
   * @param places
   * @param incidenceMatrixOut
   * @param incidenceMatrixIn
   * @param marking
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

    try {
        this.logWriter = new BufferedWriter(new FileWriter(LOG_PATH, true));
    } catch (IOException e) {
        throw new RuntimeException("Failed to initialize log writer: " + e.getMessage());
    }
  }

  /**
   * Fires a transition in the Petri net.
   *
   * @param transitionIndex The index of the transition to fire in the input incidence matrix
   */
  public boolean tryFireTransition(int transitionIndex) {
    Transition transitionFromIndex = transitions.get(transitionIndex);

    // If not enabled, return false
    if (!enabledTransitions.contains(transitionFromIndex)) {
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

    // Write the transition number to the log file
    writeLog(transitionIndex);

    if (transitionIndex == LAST_TRANSITION) {
      invariantsCount++;
      if (invariantsCount >= invariantsCountTarget) {
        closeLogWriter();
        invariantsTargetAchieved = true;
      }
    }

    // Update the enabled transitions after firing the transition
    updateEnabledTransitions();
    return true;
  }

  /** Prints the current marking of the Petri net. */
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

  /** Prints the names of the currently enabled transitions in the Petri net. */
  public void printEnabledTransitions() {
    // Iterate over the enabledTransitions list and print each transition's name
    enabledTransitions.stream().map(Transition::getName).forEach(System.out::println);
  }

  public void writeLog(int transition) {
    try {
        logWriter.write("T" + transition);
        logWriter.flush();
    } catch (IOException e) {
        System.err.println("An error occurred while writing to the log: " + e.getMessage());
    }
  }

  public void closeLogWriter() {
    try {
        if (logWriter != null) {
            logWriter.close();
        }
    } catch (IOException e) {
        System.err.println("Failed to close log writer: " + e.getMessage());
    }
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

  /* Getters */
  public int[] getMarking() {
    return marking;
  }

  public List<Transition> getEnabledTransitions() {
    return enabledTransitions;
  }

  public boolean invariantsTargetAchieved() {
    return invariantsTargetAchieved;
  }
}
