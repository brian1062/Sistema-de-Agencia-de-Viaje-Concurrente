package petrinet;

import java.util.List;
import monitor.Monitor;

public class Segments implements Runnable {
  private final List<Transition> sequence;
  private final Monitor monitor;

  public Segments(List<Transition> sequence, Monitor monitor) {
    this.sequence = sequence;
    this.monitor = monitor;
  }

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
