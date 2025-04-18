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

    while (!monitor.petriNetHasFinished()) {
      for (Transition t : sequence) {
        monitor.fireTransition(t.getNumber());
      }
      // If the Petri net has finished, we stop the execution immediately
      if (monitor.petriNetHasFinished()) {
        return;
      }
    }
  }
}
