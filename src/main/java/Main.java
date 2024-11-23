import java.util.Arrays;

public class Main {

  public static void main(String[] args) {

    // Variables
    final int TARGET_INVARIANTS = 186; // Number of invariants to reach

    PetriNetConf rdPConf = new PetriNetConf(); // Load configuration

    PetriNet petriNet =
        new PetriNet(
            rdPConf.getTransitions(),
            rdPConf.getPlaces(),
            rdPConf.getIncidenceMatrixOut(),
            rdPConf.getIncidenceMatrixIn(),
            rdPConf.getInitialMarking(),
            TARGET_INVARIANTS);

    // Initialize threads array
    Thread[] threads =
        new Thread[rdPConf.getNumberOfSequences()]; // We'll have one thread per sequence

    Monitor monitor = Monitor.getMonitor(petriNet);

    // Create and start threads
    Arrays.setAll(
        threads, i -> new Thread(new Segments(rdPConf.getTransitionSequence(i), monitor)));
    Arrays.stream(threads).forEach(Thread::start);
  }
}
