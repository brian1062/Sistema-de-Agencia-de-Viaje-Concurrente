import java.util.Arrays;
import java.util.Scanner;
import monitor.Monitor;
import petrinet.PetriNet;
import petrinet.PetriNetConf;
import petrinet.Segments;
import policy.BalancedPolicy;
import policy.FCFSPolicy;
import policy.Policy;
import policy.PrioritizedPolicy;
import utils.Logger;

/**
 * Main class that runs the Petri Net simulation with a specified policy. Policy can be specified
 * via command-line arguments or console input.
 */
public class Main {
  private static final Logger logger = Logger.getLogger();

  public static void main(String[] args) {
    // Register shutdown hook for logger
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  try {
                    logger.info("Application shutting down...");
                    logger.close();
                  } catch (Exception e) {
                    System.err.println("Error closing logger: " + e.getMessage());
                  }
                }));

    logger.info("Application starting...");

    try {
      Policy policy;
      if (args.length == 1) {
        // If command-line argument is provided, use it
        policy = selectPolicy(args[0]);
      } else {
        // If no argument is provided, ask for input through console
        policy = getPolicyFromConsole();
      }

      PetriNetConf rdPConf = new PetriNetConf();

      PetriNet petriNet =
          new PetriNet(
              rdPConf.getTransitions(),
              rdPConf.getPlaces(),
              rdPConf.getIncidenceMatrixOut(),
              rdPConf.getIncidenceMatrixIn(),
              rdPConf.getPlacesInvariants(),
              rdPConf.getInitialMarking(),
              rdPConf.getTargetInvariants());

      // Initialize monitor with the chosen policy
      Monitor monitor = Monitor.getMonitor(petriNet, policy);

      // Initialize threads array
      Thread[] threads = new Thread[rdPConf.getNumberOfSequences()];

      // Create and start threads
      Arrays.setAll(
          threads, i -> new Thread(new Segments(rdPConf.getTransitionSequence(i), monitor)));

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

  private static Policy getPolicyFromConsole() {
    Scanner scanner = new Scanner(System.in);
    while (true) {
      printUsage();
      System.out.print("Enter your choice (1, 2 or 3): ");

      try {
        String input = scanner.nextLine();
        Policy policy = selectPolicy(input);
        scanner.close();
        return policy;
      } catch (Exception e) {
        logger.error("Error reading policy selection: " + e.getMessage());
      }
    }
  }

  private static void printUsage() {
    System.out.println("\nAvailable policies:");
    System.out.println("1: Balanced Policy (50/50 and 50/50 distributions)");
    System.out.println("2: Prioritized Policy (75/25 and 80/20 distributions)");
    System.out.println("3: FCFS Policy (First-Come-First-Served)");
  }

  private static Policy selectPolicy(String policyArg) {
    return switch (policyArg) {
      case "1" -> {
        logger.info("Selected: Balanced Policy");
        yield new BalancedPolicy();
      }
      case "2" -> {
        logger.info("Selected: Prioritized Policy");
        yield new PrioritizedPolicy();
      }
      case "3" -> {
        logger.info("Selected: FCFS Policy");
        yield new FCFSPolicy();
      }
      default -> {
        logger.error("Invalid policy selection: " + policyArg);
        System.exit(1);
        yield null; // This line will never be reached
      }
    };
  }
}
