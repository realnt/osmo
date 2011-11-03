package osmo.tester.parser;

import org.junit.Before;
import org.junit.Test;
import osmo.common.log.Logger;
import osmo.tester.annotation.Variable;
import osmo.tester.model.FSM;
import osmo.tester.model.FSMTransition;
import osmo.tester.model.Requirements;
import osmo.tester.model.VariableField;
import osmo.tester.model.dataflow.SearchableInput;
import osmo.tester.testmodels.EmptyTestModel1;
import osmo.tester.testmodels.EmptyTestModel2;
import osmo.tester.testmodels.EmptyTestModel3;
import osmo.tester.testmodels.EmptyTestModel4;
import osmo.tester.testmodels.EmptyTestModel5;
import osmo.tester.testmodels.EmptyTestModel6;
import osmo.tester.testmodels.PartialModel1;
import osmo.tester.testmodels.PartialModel2;
import osmo.tester.testmodels.ValidTestModel3;
import osmo.tester.testmodels.VariableModel1;
import osmo.tester.testmodels.VariableModel2;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;

import static junit.framework.Assert.*;

/** @author Teemu Kanstren */
public class ParserTests {
  private MainParser parser = null;

  @Before
  public void setup() {
    parser = new MainParser();
  }

  private ModelObject mo(Object o) {
    return new ModelObject(o);
  }

  private ModelObject mo(String prefix, Object o) {
    return new ModelObject(prefix, o);
  }


  @Test
  public void testModel1() throws Exception {
    EmptyTestModel1 model = new EmptyTestModel1();
    FSM fsm = parser.parse(mo(model));
    assertEquals("Number of @Before methods", 2, fsm.getBefores().size());
    assertEquals("Number of @BeforeSuite methods", 1, fsm.getBeforeSuites().size());
    assertEquals("Number of @After methods", 1, fsm.getAfters().size());
    assertEquals("Number of @AfterSuite methods", 1, fsm.getAfterSuites().size());
    //these also test for the correct number of guards
    assertTransitionPresent(fsm, "hello", 0, 2);
    assertTransitionPresent(fsm, "world", 3, 1);
    assertTransitionPresent(fsm, "epixx", 2, 3);
    assertEquals("Number of end conditions", 2, fsm.getEndConditions().size());
    assertNotNull("Should have TestLog set", model.getHistory());
    assertNotNull("Should have Requirements set", fsm.getRequirements());
  }


  @Test
  public void testModel2() {
    try {
      FSM fsm = parser.parse(mo(new EmptyTestModel2()));
      fail("Should throw exception");
    } catch (Exception e) {
      String msg = e.getMessage();
      String expected = "Invalid FSM:\n" +
              "Only one @RequirementsField allowed in the model.\n" +
              "Only one @TestSuiteField allowed in the model.\n" +
              "Guard/Pre/Post without transition:foo\n";
      assertEquals(expected, msg);
    }
  }

  @Test
  public void testModel3() {
    try {
      FSM fsm = parser.parse(mo(new EmptyTestModel3()));
      fail("Should throw exception");
    } catch (Exception e) {
      String msg = e.getMessage();
      String expected = "Invalid FSM:\n" +
              "@RequirementsField class must be of type " + Requirements.class.getName() + ". Was " + String.class.getName() + ".\n" +
              "@TestSuiteField class must be of type osmo.tester.generator.testsuite.TestSuite. Was java.lang.String.\n" +
              "Invalid return type for @EndCondition (\"end()\"):void. Should be boolean.\n" +
              "Invalid return type for guard (\"hello()\"):class java.lang.String.\n" +
              "Invalid return type for @EndState (\"toEnd()\"):void. Should be boolean.\n" +
              "Post-methods are allowed to have only one parameter of type Map<String, Object>: \"wrong()\" has one of type class java.lang.String.\n";
      assertEquals(expected, msg);
    }
  }

  @Test
  public void testModel4() {
    try {
      FSM fsm = parser.parse(mo(new EmptyTestModel4()));
      fail("Should throw exception");
    } catch (Exception e) {
      //note that this exception checking will swallow real errors so it can be useful to print them..
//      e.printStackTrace();
      String msg = e.getMessage();
      String expected = "Invalid FSM:\n" +
              "@RequirementsField value was null, which is not allowed.\n" +
              "Guard methods are not allowed to have parameters: \"hello()\" has 1 parameters.\n" +
              "@EndCondition methods are not allowed to have parameters: \"ending()\" has 1 parameters.\n" +
              "@EndState methods are not allowed to have parameters: \"endd()\" has 1 parameters.\n" +
              "";
      assertEquals(expected, msg);
    }
  }

  @Test
  public void testModel5() {
    try {
      FSM fsm = parser.parse(mo(new EmptyTestModel5()));
      fail("Should throw exception");
    } catch (Exception e) {
      String msg = e.getMessage();
      String expected = "Invalid FSM:\n" +
              "Invalid return type for @EndCondition (\"hello()\"):class java.lang.String. Should be boolean.\n" +
              "@EndCondition methods are not allowed to have parameters: \"hello()\" has 1 parameters.\n" +
              "Transition methods are not allowed to have parameters: \"epixx()\" has 1 parameters.\n";
      assertEquals(expected, msg);
    }
  }

  @Test
  public void testModel6() {
    try {
      FSM fsm = parser.parse(mo(new EmptyTestModel6()));
      fail("Should throw exception");
    } catch (Exception e) {
      String msg = e.getMessage();
      String expected = "Invalid FSM:\n" +
              "Invalid return type for guard (\"listCheck()\"):class java.lang.String.\n" +
              "Transition methods are not allowed to have parameters: \"transition1()\" has 1 parameters.\n" +
              "Transition methods are not allowed to have parameters: \"epix()\" has 1 parameters.\n" +
              "Guard/Pre/Post without transition:world\n";
      assertEquals(expected, msg);
    }
  }

  @Test
  public void testPartialModels() {
    Requirements req = new Requirements();
    PartialModel1 model1 = new PartialModel1(req, null);
    PartialModel2 model2 = new PartialModel2(req, null);
    Collection<ModelObject> models = new ArrayList<ModelObject>();
    models.add(mo(model1));
    models.add(mo(model2));
    FSM fsm = parser.parse(models);
    assertEquals("Number of @Before methods", 2, fsm.getBefores().size());
    assertEquals("Number of @BeforeSuite methods", 1, fsm.getBeforeSuites().size());
    assertEquals("Number of @After methods", 2, fsm.getAfters().size());
    assertEquals("Number of @AfterSuite methods", 1, fsm.getAfterSuites().size());
    //these also test for the correct number of guards
    assertTransitionPresent(fsm, "hello", 1, 2);
    assertTransitionPresent(fsm, "world", 3, 2);
    assertTransitionPresent(fsm, "epixx", 2, 2);
    assertEquals("Number of end conditions", 2, fsm.getEndConditions().size());
    assertNotNull("Should have TestSuite set", model1.getHistory());
    String s = "";
    String s1 = "";
    assertNotNull("Should have Requirements set", fsm.getRequirements());
  }

  @Test
  public void noMethods() {
    try {
      FSM fsm = parser.parse(mo(new Object()));
      fsm.checkAndUpdateGenericItems("");
      fail("Should throw exception when no transition methods are available.");
    } catch (Exception e) {
      String msg = e.getMessage();
      String expected = "Invalid FSM:\n" +
              "No transitions found in given model object. Model cannot be processed.\n";
      assertEquals(expected, msg);
    }
  }

  private void assertTransitionPresent(FSM fsm, String name, int guardCount, int oracleCount) {
    FSMTransition transition = fsm.getTransition(name);
    assertNotNull("Transition '" + name + "' should be generated.", transition);
    assertNotNull("Transition '" + name + "' should have valid transition content.", transition.getTransition());
    assertEquals("Transition '" + name + "' should have " + guardCount + " guards.", guardCount, transition.getGuards().size());
    assertEquals("Transition '" + name + "' should have " + oracleCount + " oracles.", oracleCount, transition.getPostMethods().size());
  }

  @Test
  public void variableParsing() {
    VariableModel1 model = new VariableModel1();
    FSM fsm = parser.parse(mo(model));
    Collection<VariableField> variables = fsm.getStateVariables();
    assertEquals("All @" + Variable.class.getSimpleName() + " items should be parsed.", 10, variables.size());
    assertVariablePresent(variables, "i1");
    assertVariablePresent(variables, "f1");
    assertVariablePresent(variables, "d1");
    assertVariablePresent(variables, "b1");
    assertVariablePresent(variables, "i2");
    assertVariablePresent(variables, "f2");
    assertVariablePresent(variables, "d2");
    assertVariablePresent(variables, "b2");
    assertVariablePresent(variables, "testVariable");
    assertVariablePresent(variables, "stringVariable");
  }

  private void assertVariablePresent(Collection<VariableField> variables, String name) {
    for (VariableField variable : variables) {
      if (variable.getName().equals(name)) {
        return;
      }
    }
    fail("Variable " + name + " should be present in the model.");
  }

  @Test
  public void searchableInputParsing() {
    VariableModel2 model = new VariableModel2();
    FSM fsm = parser.parse(mo(model));
    Collection<SearchableInput> inputs = fsm.getSearchableInputs();
    assertEquals("Number of inputs", 2, inputs.size());
    assertSearchableInputPresent(inputs, "range");
    assertSearchableInputPresent(inputs, "set");
  }

  private void assertSearchableInputPresent(Collection<SearchableInput> inputs, String name) {
    for (SearchableInput input : inputs) {
      if (input.getName().equals(name)) {
        return;
      }
    }
    fail("SearchableInput "+name+" should be present in the model.");
  }

  @Test
  public void prefixParsing() {
    ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
    PrintStream ps = new PrintStream(out);
    ValidTestModel3 model = new ValidTestModel3(ps);
    Collection<ModelObject> models = new ArrayList<ModelObject>();
    models.add(mo("ap_", model));
    models.add(mo("ip_", model));
    FSM fsm = parser.parse(models);
    assertTransitionPresent(fsm, "ap_hello", 1, 2);
    assertTransitionPresent(fsm, "ip_hello", 1, 2);
    assertTransitionPresent(fsm, "ap_world", 1, 2);
    assertTransitionPresent(fsm, "ip_world", 1, 2);
    assertTransitionPresent(fsm, "ap_epixx", 1, 3);
    assertTransitionPresent(fsm, "ip_epixx", 1, 3);
    assertEquals("Number of @BeforeTest elements", 2, fsm.getBefores().size());
    assertEquals("Number of @BeforeSuite elements", 2, fsm.getBeforeSuites().size());
    assertEquals("Number of @AfterTest elements", 2, fsm.getAfters().size());
    assertEquals("Number of @AfterSuite elements", 2, fsm.getAfterSuites().size());
  }

}
