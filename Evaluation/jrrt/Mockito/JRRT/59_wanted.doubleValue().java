package org.mockito.internal.matchers;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;
import java.io.Serializable;

public class EqualsWithDelta extends ArgumentMatcher<Number> implements Serializable  {
  final private static long serialVersionUID = 5066980489920383664L;
  final private Number wanted;
  final private Number delta;
  public EqualsWithDelta(Number value, Number delta) {
    super();
    this.wanted = value;
    this.delta = delta;
  }
  public boolean matches(Object actual) {
    Number actualNumber = (Number)actual;
    if(wanted == null ^ actual == null) {
      return false;
    }
    if(wanted == actual) {
      return true;
    }
    double var_59 = wanted.doubleValue();
    return var_59 - delta.doubleValue() <= actualNumber.doubleValue() && actualNumber.doubleValue() <= wanted.doubleValue() + delta.doubleValue();
  }
  public void describeTo(Description description) {
    description.appendText("eq(" + wanted + ", " + delta + ")");
  }
}