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

  /**
   * Constructor for the Segments instance with a trnsition sequence.
   *
   * @param sequence The sequence of transitions to execute.
   * @param monitor The monitor instance to control the Petri Net execution.
   */
  public Segments(List<Transition> sequence, Monitor monitor) {
    this.sequence = sequence;
    this.monitor = monitor;
  }

  /**
   * Executes the sequence of transitions repeatedly until the Petri Net reaches a final state.
   * Checks for completion after each transition and exits inmediately if the Petri Net has
   * finished.
   */
  @Override
  public void run() {

    boolean finished = false;

    while (!finished) {
      for (Transition t : sequence) {
        monitor.fireTransition(t.getNumber());

        // Check if the Petri Net has finished after firing the transition
        if (monitor.petriNetHasFinished()) {
          System.out.println("Thread " + Thread.currentThread().getName() + " has finished.");
          finished = true;
          break;
        }
      }
    }
  }
}
