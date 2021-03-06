package osmo.tester.examples.calendar.testmodel;

import osmo.tester.annotation.Description;
import osmo.tester.annotation.Guard;
import osmo.tester.annotation.TestStep;
import osmo.tester.examples.calendar.scripter.CalendarScripter;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/** @author Teemu Kanstren */
public class CalendarFailureModel {
  /** The global model state, shared across test models. */
  private final ModelState state;
  /** The scripter for creating/executing the test cases. */
  private final CalendarScripter scripter;
  private final PrintStream out;

  public CalendarFailureModel(ModelState state, CalendarScripter scripter) {
    this.state = state;
    this.scripter = scripter;
    this.out = System.out;
  }

  public CalendarFailureModel(ModelState state, CalendarScripter scripter, PrintStream out) {
    this.state = state;
    this.scripter = scripter;
    this.out = out;
  }

  @Description("This is always true")
  @Guard("Failing Assertion")
  public boolean shallWeAllowIt() {
    return true;
  }

  @TestStep("Failing Assertion")
  public void giefFailure() {
    Collection<ModelEvent> events = new ArrayList<>();
    User failingUser = new User("persikka", -1);
    events.add(new ModelEvent(failingUser, new Date(0), new Date(0), null, null));
//    out.println("--MANSIKKA");
    scripter.assertUserEvents(state.randomUser().getId(), events);
    out.println("--MUSTIKKA");
  }
}
