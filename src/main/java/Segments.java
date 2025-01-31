import java.util.List;

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
    }
    System.out.println(
        "[SUCCESS] Checking invariants target achieved. The Petri net has finished!");
  }
}
