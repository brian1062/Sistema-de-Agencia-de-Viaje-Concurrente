import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;

/**
 * Monitor class for managing synchronized interactions with a Petri Net. Ensures only one instance
 * of Monitor is created using the Singleton pattern.
 */
class Monitor implements MonitorInterface {

  // Singleton instance of the Monitor
  private static Monitor monitor = null;
  // boolean isFireSuccessful = false;
  PetriNet petriNet; // The associated Petri Net instance
  private final String LOG_PATH = "/tmp/petriNetResults.txt";

  private final Semaphore mutex; // Mutex to ensure thread safety

  /**
   * Private constructor to enforce Singleton pattern.
   *
   * @param petriNet the PetriNet instance to control.
   */
  private Monitor(PetriNet petriNet) {
    this.mutex = new Semaphore(1, true);
    this.petriNet = petriNet;
    //this.transitionsMap = new HashMap<>();
    //for (Transition transition : petriNet.getTransitionList()) {
    //  // Initialize a semaphore for each transition
    //  this.transitionsMap.put(transition, new Semaphore(0));
    //}
  }

  /**
   * Returns the singleton instance of the Monitor.
   *
   * @param petriNet the PetriNet instance to associate with the Monitor.
   * @return the Monitor instance.
   */
  public static Monitor getMonitor(PetriNet petriNet) {
    if (monitor == null) {
      monitor = new Monitor(petriNet);
    }
    return monitor;
  }

  /**
   * Attempts to fire a transition in the associated Petri Net.
   *
   * @param transitionIndex the index of the transition to fire.
   * @return true if the transition was successfully fired at least once, false otherwise.
   */
  @Override
<<<<<<< HEAD
  public void fireTransition(int transitionIndex) {
=======
<<<<<<< HEAD
  public boolean fireTransition(int transitionIndex) {
  //public boolean fireTransition(Transition transition) { // por que retornamos boolean si despues no lo usamos? por que no retornar void?
=======
  public void fireTransition(int transitionIndex) {
>>>>>>> f39a947 (removed unnecesary while block in Monitor's class fireTransition method)
>>>>>>> 0d4efab (fixed conflicts)
    try {
      mutex.acquire();
    } catch (Exception e) {
      System.err.println("[ERROR] While acquiring mutex\n");
      e.printStackTrace();
      return;
    }

<<<<<<< HEAD
    //isFireSuccessful = true;
    //while (isFireSuccessful) {
    //  isFireSuccessful = petriNet.tryFireTransition(transitionIndex);
    //  if (isFireSuccessful) {
    //    System.out.println(
    //        "Transition fired: " + transitionIndex + " Marking: " + petriNet.getStringMarking());
=======
<<<<<<< HEAD
    // if alpha > 0 then the transition is timed, else is immediate
    // Transition t = petriNet.getTransitionPerIndex(transitionIndex);
    // if (t.getTime() > 0) {
    //  // Release the mutex and sleep for the time of the transition
    //  mutex.release();
    //
    //  try {
    //    System.out.println("Sleeping for " + t.getTime() + "ms");
    //    Thread.sleep(t.getTime());
    //  } catch (InterruptedException e) {
    //    System.err.println("[ERROR] While sleeping\n");
    //    e.printStackTrace();
    //    return false;
>>>>>>> 0d4efab (fixed conflicts)
    //  }
    //}
    
    if (petriNet.tryFireTransition(transitionIndex)) {
      System.out.println(
        "Transition fired: " + transitionIndex + " Marking: " + petriNet.getStringMarking());
    }
<<<<<<< HEAD
    
=======
    // }

=======
    //isFireSuccessful = true;
    //while (isFireSuccessful) {
    //  isFireSuccessful = petriNet.tryFireTransition(transitionIndex);
    //  if (isFireSuccessful) {
    //    System.out.println(
    //        "Transition fired: " + transitionIndex + " Marking: " + petriNet.getStringMarking());
    //  }
    //}
    
    if (petriNet.tryFireTransition(transitionIndex)) {
      System.out.println(
        "Transition fired: " + transitionIndex + " Marking: " + petriNet.getStringMarking());
    }
    
>>>>>>> f39a947 (removed unnecesary while block in Monitor's class fireTransition method)
>>>>>>> 0d4efab (fixed conflicts)
    mutex.release();
  }

  /**
   * Checks if the Petri Net has achieved its invariants target.
   *
   * @return true if the invariants target is achieved, false otherwise.
   */
  public boolean petriNetHasFinished() {
    return petriNet.invariantsTargetAchieved();
  }

  /**
   * Writes a message to the log file.
   *
   * @param message the message to write to the log file.
   */
  private void writeLog(String message) {
    try (FileWriter writer = new FileWriter(LOG_PATH, true)) {
      writer.write(message + "\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

/** Interface for Monitor functionality. */
interface MonitorInterface {
  void fireTransition(int transition); // TODO: entiendo que necesitamos que sea bool por el enunciado pero no lo estamos usando
}
