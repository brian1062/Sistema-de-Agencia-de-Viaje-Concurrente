package petrinet;

import java.util.List;
import monitor.Monitor;

/**
 * Represents a runnable segment of transitions in a Petri Net. Executes a sequence of transitions
 * in a loop until the Petri Net reaches a final state.
 */
public class Segments implements Runnable {
  private final List<Transition> sequence;
  private final Monitor monitor;
  private final PetriNet petriNet;

  /**
   * Constructor for the Segments instance with a trnsition sequence.
   *
   * @param sequence The sequence of transitions to execute.
   * @param monitor The monitor instance to control the Petri Net execution.
   * @param petriNet The Petri Net instance to be controlled.
   */
  public Segments(List<Transition> sequence, Monitor monitor, PetriNet petriNet) {
    this.sequence = sequence;
    this.monitor = monitor;
    this.petriNet = petriNet;
  }

  /**
   * Executes the sequence of transitions repeatedly until the Petri Net reaches a final state.
   * Checks for completion after each transition and exits inmediately if the Petri Net has
   * finished.
   */
  @Override
  public void run() {
    while (!petriNet.petriNetHasFinished()) {
      for (Transition t : sequence) {
        monitor.fireTransition(t.getNumber());

        // Check if the Petri Net has finished after firing the transition
        if (petriNet.petriNetHasFinished()) {
          System.out.println("Thread " + Thread.currentThread().getName() + " has finished.");
          return; // Exit the method immediately
        }
      }
    }
  }
}
