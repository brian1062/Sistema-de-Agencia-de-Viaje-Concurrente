import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Segments implements Runnable {
  private final List<Transition> sequence;
  private final Monitor monitor;

  public Segments(List<Transition> sequence, Monitor monitor) {
    this.sequence = sequence;
    this.monitor = monitor;
  }

  @Override
  public void run() {
    AtomicBoolean isRunning = new AtomicBoolean(true);

    while (isRunning.get()) {
      for (Transition t : sequence) {
        monitor.fireTransition(t.getNumber());
        
        //while (!monitor.fireTransition(t)){}
        //monitor.fireTransition(t);

        if (monitor.petriNetHasFinished()) {
          isRunning.set(false);
          break;
        }
      }
    }
  }
}
