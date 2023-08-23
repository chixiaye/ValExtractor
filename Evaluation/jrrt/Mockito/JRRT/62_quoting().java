package org.mockito.internal.matchers;
import org.hamcrest.Description;
import org.hamcrest.SelfDescribing;
import org.mockito.ArgumentMatcher;
import java.io.Serializable;

public class Equals extends ArgumentMatcher<Object> implements ContainsExtraTypeInformation, Serializable  {
  final private static long serialVersionUID = -3395637450058086891L;
  final private Object wanted;
  public Equals(Object wanted) {
    super();
    this.wanted = wanted;
  }
  final protected Object getWanted() {
    return wanted;
  }
  public SelfDescribing withExtraTypeInfo() {
    return new SelfDescribing() {
        public void describeTo(Description description) {
          description.appendText(describe("(" + wanted.getClass().getSimpleName() + ") " + wanted));
        }
    };
  }
  public String describe(Object object) {
    String var_62 = quoting();
    return var_62 + object + quoting();
  }
  private String quoting() {
    if(wanted instanceof String) {
      return "\"";
    }
    else 
      if(wanted instanceof Character) {
        return "\'";
      }
      else {
        return "";
      }
  }
  @Override() public boolean equals(Object o) {
    if(o == null || !this.getClass().equals(o.getClass())) {
      return false;
    }
    Equals other = (Equals)o;
    return this.wanted == null && other.wanted == null || this.wanted != null && this.wanted.equals(other.wanted);
  }
  public boolean matches(Object actual) {
    return Equality.areEqual(this.wanted, actual);
  }
  public boolean typeMatches(Object object) {
    return wanted != null && object != null && object.getClass() == wanted.getClass();
  }
  @Override() public int hashCode() {
    return 1;
  }
  public void describeTo(Description description) {
    description.appendText(describe(wanted));
  }
}