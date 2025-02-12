import java.util.Arrays;
import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    Policy policy = selectPolicy(scanner);
    scanner.close();

    PetriNetConf rdPConf = new PetriNetConf();
    
    PetriNet petriNet = new PetriNet(
      rdPConf.getTransitions(),
      rdPConf.getPlaces(),
      rdPConf.getIncidenceMatrixOut(),
      rdPConf.getIncidenceMatrixIn(),
      rdPConf.getPlacesInvariants(),
      rdPConf.getInitialMarking(),
      rdPConf.getTargetInvariants()
    );
    
    // Initialize monitor with the chosen policy
    Monitor monitor = Monitor.getMonitor(petriNet, policy);

    // Initialize threads array
    Thread[] threads = new Thread[rdPConf.getNumberOfSequences()];

    // Create and start threads
    Arrays.setAll(
      threads, 
      i -> new Thread(new Segments(rdPConf.getTransitionSequence(i), monitor))
    );
    Arrays.stream(threads).forEach(Thread::start);
  }

  private static Policy selectPolicy(Scanner scanner) {
    while (true) {
      System.out.println("\nSelect the policy to use:");
      System.out.println("1. Balanced Policy (50/50 distribution)");
      System.out.println("2. Prioritized Policy (75/25 and 80/20 distributions)");
      System.out.print("Enter your choice (1 or 2): ");
      
      try {
        String input = scanner.nextLine();
        return switch (input) {
          case "1" -> {
            System.out.println("Selected: Balanced Policy");
            yield new BalancedPolicy();
          }
          case "2" -> {
            System.out.println("Selected: Prioritized Policy");
            yield new PrioritizedPolicy();
          }
          default -> {
            System.out.println("Invalid choice. Please enter 1 or 2.");
            yield selectPolicy(scanner);
          }
        };
      } catch (Exception e) {
        System.out.println("Error reading input. Please try again.");
      }
    }
  }
}


/*public class Main {

  public static void main(String[] args) {

    PetriNetConf rdPConf = new PetriNetConf(); // Load configuration

    PetriNet petriNet =
        new PetriNet(
            rdPConf.getTransitions(),
            rdPConf.getPlaces(),
            rdPConf.getIncidenceMatrixOut(),
            rdPConf.getIncidenceMatrixIn(),
            rdPConf.getPlacesInvariants(),
            rdPConf.getInitialMarking(),
            rdPConf.getTargetInvariants());

    // Initialize threads array
    Thread[] threads =
        new Thread
            [rdPConf.getNumberOfSequences()]; // We'll have one thread per sequence of transitions

    // Create the desired policy
    Policy policy = new PrioritizedPolicy();  // or new BalancedPolicy()

    Monitor monitor = Monitor.getMonitor(petriNet, policy);

    // Create and start threads
    Arrays.setAll(
        threads, i -> new Thread(new Segments(rdPConf.getTransitionSequence(i), monitor)));
    Arrays.stream(threads).forEach(Thread::start);
  }
}*/
