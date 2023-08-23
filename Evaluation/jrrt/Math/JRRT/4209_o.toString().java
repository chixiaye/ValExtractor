package org.apache.commons.math3.util;
import java.io.Serializable;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NullArgumentException;

public class DefaultTransformer implements NumberTransformer, Serializable  {
  final private static long serialVersionUID = 4019938025047800455L;
  @Override() public boolean equals(Object other) {
    if(this == other) {
      return true;
    }
    return other instanceof DefaultTransformer;
  }
  public double transform(Object o) throws NullArgumentException, MathIllegalArgumentException {
    if(o == null) {
      throw new NullArgumentException(LocalizedFormats.OBJECT_TRANSFORMATION);
    }
    if(o instanceof Number) {
      return ((Number)o).doubleValue();
    }
    try {
      String var_4209 = o.toString();
      return Double.parseDouble(var_4209);
    }
    catch (NumberFormatException e) {
      throw new MathIllegalArgumentException(LocalizedFormats.CANNOT_TRANSFORM_TO_DOUBLE, o.toString());
    }
  }
  @Override() public int hashCode() {
    return 401993047;
  }
}