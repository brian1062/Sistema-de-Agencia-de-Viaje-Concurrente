import java.util.Arrays;
import java.util.Scanner;

public class Main {
  private static final Logger logger = Logger.getLogger();

  public static void main(String[] args) {
    // Register shutdown hook for logger
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        logger.info("Application shutting down...");
        logger.close();
      } catch (Exception e) {
        System.err.println("Error closing logger: " + e.getMessage());
      }
    }));

    logger.info("Application starting...");

    try {
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

      logger.info("Starting Petri net execution...");
      Arrays.stream(threads).forEach(Thread::start);

      // Wait for all threads to complete
      for (Thread thread : threads) {
        thread.join();
      }

      logger.info("Petri net execution completed successfully");

    } catch (Exception e) {
      logger.error("Fatal error in application: " + e.getMessage());
      System.exit(1);
    }
  }

  private static Policy selectPolicy(Scanner scanner) {
    while (true) {
      System.out.println("\nSelect the policy to use:");
      System.out.println("1. Balanced Policy (50/50 and 50/50 distributions)");
      System.out.println("2. Prioritized Policy (75/25 and 80/20 distributions)");
      System.out.print("Enter your choice (1 or 2): ");

      try {
        String input = scanner.nextLine();
        return switch (input) {
          case "1" -> {
            logger.info("Selected: Balanced Policy");
            yield new BalancedPolicy();
          }
          case "2" -> {
            logger.info("Selected: Prioritized Policy");
            yield new PrioritizedPolicy();
          }
          default -> {
            logger.error("Invalid policy selection: " + input);
            yield selectPolicy(scanner);
          }
        };
      } catch (Exception e) {
        logger.error("Error reading policy selection: " + e.getMessage());
      }
    }
  }
}
