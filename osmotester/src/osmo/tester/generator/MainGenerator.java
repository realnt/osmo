package osmo.tester.generator;

import osmo.common.log.Logger;
import osmo.tester.generator.algorithm.FSMTraversalAlgorithm;
import osmo.tester.generator.endcondition.EndCondition;
import osmo.tester.generator.testsuite.TestCase;
import osmo.tester.generator.testsuite.TestStep;
import osmo.tester.generator.testsuite.TestSuite;
import osmo.tester.model.FSM;
import osmo.tester.model.FSMTransition;
import osmo.tester.model.InvocationTarget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The main test generator class.
 * Takes as input the finite state machine model parsed by {@link osmo.tester.parser.MainParser}.
 * Runs test generation on this model using the defined algorithms, exit strategies, etc.
 * 
 * @author Teemu Kanstren
 */
public class MainGenerator {
  private static Logger log = new Logger(MainGenerator.class);
  /** Test generation history. */
  private TestSuite suite;
  /** The set of enabled transitions in the current state is passed to this algorithm to pick one to execute. */
  private FSMTraversalAlgorithm algorithm;
  /** Defines when test suite generation should be stopped. Invoked between each test case. */
  private Collection<EndCondition> suiteEndConditions;
  /** Defines when test case generation should be stopped. Invoked between each test step. */
  private Collection<EndCondition> testCaseEndConditions;
  /** The list of listeners to be notified of new events as generation progresses. */
  private GenerationListenerList listeners;
  /** This is set when the test should end but @EndState is not yet achieved to signal ending ASAP. */
  private boolean testEnding = false;

  /**
   * Constructor.
   */
  public MainGenerator() {
  }

  /**
   *
   * @param algorithm The set of enabled transitions in the current state is passed to this algorithm to pick one to execute.
   */
  public void setAlgorithm(FSMTraversalAlgorithm algorithm) {
    this.algorithm = algorithm;
  }

  /**
   *
   * @param suiteEndConditions Defines when test suite generation should be stopped. Invoked between each test case.
   */
  public void setSuiteEndConditions(Collection<EndCondition> suiteEndConditions) {
    this.suiteEndConditions = suiteEndConditions;
  }

  /**
   *
   * @param testCaseEndConditions Defines when test case generation should be stopped. Invoked between each test step.
   */
  public void setTestCaseEndConditions(Collection<EndCondition> testCaseEndConditions) {
    this.testCaseEndConditions = testCaseEndConditions;
  }

  /**
   * 
   * @param listeners Listeners to be notified about generation events.
   */
  public void setListeners(GenerationListenerList listeners) {
    this.listeners = listeners;
  }

  /**
   * Invoked to start the test generation using the configured parameters.
   *
   * @param fsm Describes the test model in an FSM format.
   */
  public void generate(FSM fsm) {
    suite = fsm.getSuite();
    log.debug("Starting test suite generation");
    beforeSuite(fsm);
    while (!checkSuiteEndConditions(fsm)) {
      log.debug("Starting new test generation");
      beforeTest(fsm);
      while (!checkTestCaseEndConditions(fsm)) {
        List<FSMTransition> enabled = getEnabled(fsm);
        FSMTransition next = algorithm.choose(suite, enabled);
        log.debug("Taking transition "+next.getName());
        execute(fsm, next);
        if (checkModelEndConditions(fsm)) {
          //stop this test case generation if any end condition returns true
          break;
        }
      }
      afterTest(fsm);
      log.debug("Finished new test generation");
    }
    afterSuite(fsm);
    log.debug("Finished test suite generation");
  }

  private boolean checkSuiteEndConditions(FSM fsm) {
    for (EndCondition ec : suiteEndConditions) {
      if (!ec.endSuite(suite, fsm)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check if generation of current test case should stop based on given end conditions.
   *
   * @param fsm The model being used in test generation.
   * @return True if this test generation should stop.
   */
  private boolean checkTestCaseEndConditions(FSM fsm) {
    if (testEnding) {
      //allow ending only if end state annotations are not present or return true
      return checkEndStates(fsm);
    }
    for (EndCondition ec : testCaseEndConditions) {
      //check if all end conditions are met
      if (!ec.endTest(suite, fsm)) {
        return false;
      }
    }
    testEnding = true;
    if (fsm.getEndStates().size() > 0) {
      return checkEndStates(fsm);
    }
    return true;
  }

  private boolean checkEndStates(FSM fsm) {
    Collection<InvocationTarget> endStates = fsm.getEndStates();
    for (InvocationTarget es : endStates) {
      Boolean endable = (Boolean)es.invoke();
      if (endable) {
        return true;
      }
    }
    return false;
  }

  /**
   * Calls every defind end condition and if any return true, also returns true. Otherwise, false.
   *
   * @param fsm The model object on which to invoke the methods.
   * @return true if current test case (not suite) generation should be stopped.
   */
  private boolean checkModelEndConditions(FSM fsm) {
    Collection<InvocationTarget> endConditions = fsm.getEndConditions();
    for (InvocationTarget ec : endConditions) {
      Boolean result = (Boolean)ec.invoke();
      if (result) {
        return true;
      }
    }
    return false;
  }

  private void beforeSuite(FSM fsm) {
    listeners.suiteStarted(suite);
    Collection<InvocationTarget> befores = fsm.getBeforeSuites();
    invokeAll(befores);
  }

  private void afterSuite(FSM fsm) {
    Collection<InvocationTarget> afters = fsm.getAfterSuites();
    invokeAll(afters);
    listeners.suiteEnded(suite);
  }

  private void beforeTest(FSM fsm) {
    //update history
    suite.startTest();
    listeners.testStarted(suite.getCurrentTest());
    Collection<InvocationTarget> befores = fsm.getBefores();
    invokeAll(befores);
  }

  private void afterTest(FSM fsm) {
    Collection<InvocationTarget> afters = fsm.getAfters();
    invokeAll(afters);
    TestCase current = suite.getCurrentTest();
    //update history
    suite.endTest();
    listeners.testEnded(current);
    testEnding = false;
  }

  /**
   * Invokes the given set of methods on the target test object.
   *
   * @param targets The methods to be invoked.
   */
  private void invokeAll(Collection<InvocationTarget> targets) {
    for (InvocationTarget target : targets) {
      target.invoke();
    }
  }

  /**
   * Invokes the given set of methods on the target test object.
   *
   * @param targets The methods to be invoked.
   * @param arg Argument to methods invoked.
   * @param element Type of model element (pre or post)
   * @param transition Transition to which the invocations are related.
   */
  private void invokeAll(Collection<InvocationTarget> targets, Object arg, String element, FSMTransition transition) {
    for (InvocationTarget target : targets) {
      if (element.equals("pre")) {
        listeners.pre(transition);
      }
      if (element.equals("post")) {
        listeners.post(transition);
      }
      target.invoke(arg);
    }
  }

  /**
   * Goes through all {@link osmo.tester.annotation.Transition} tagged methods in the given test model object,
   * invokes all associated {@link osmo.tester.annotation.Guard} tagged methods matching those transitions,
   * returning the set of {@link osmo.tester.annotation.Transition} methods that have no guards returning a value
   * of {@code false}.
   *
   * @param fsm Describes the test model.
   * @return The list of enabled {@link osmo.tester.annotation.Transition} methods.
   */
  private List<FSMTransition> getEnabled(FSM fsm) {
    Collection<FSMTransition> allTransitions = fsm.getTransitions();
    List<FSMTransition> enabled = new ArrayList<FSMTransition>();
    enabled.addAll(allTransitions);
    for (FSMTransition transition : allTransitions) {
      for (InvocationTarget guard : transition.getGuards()) {
        listeners.guard(transition);
        Boolean result = (Boolean)guard.invoke();
        if (!result) {
          enabled.remove(transition);
        }
      }
    }
    if (enabled.size() == 0) {
      throw new IllegalStateException("No transition available.");
    }
    return enabled;
  }

  /**
   * Executes the given transition on the given model.
   *
   * @param fsm The FSM model to which the transition belongs.
   * @param transition  The transition to be executed.
   */
  public void execute(FSM fsm, FSMTransition transition) {
    transition.reset();
    //we have to add this first or it will produce failures..
    TestStep step = suite.addStep(transition);
    //store state variable values for pre-methods
    transition.storeState(fsm);
    //store into test step the current state
    step.storeStateBefore(fsm);
    invokeAll(transition.getPreMethods(), transition.getPrePostParameter(), "pre", transition);
    listeners.transition(transition);
    InvocationTarget target = transition.getTransition();
    target.invoke();
    //store into test step the current state
    step.storeStateAfter(fsm);
    //re-store state into transition to update parameters for post-methods
    transition.storeState(fsm);
    invokeAll(transition.getPostMethods(), transition.getPrePostParameter(), "post", transition);
  }
}
