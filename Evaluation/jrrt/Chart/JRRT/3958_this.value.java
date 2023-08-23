package org.jfree.data.general;
import java.io.Serializable;
import org.jfree.chart.event.DatasetChangeInfo;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PublicCloneable;

public class DefaultValueDataset extends AbstractDataset implements ValueDataset, Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = 8137521217249294891L;
  private Number value;
  public DefaultValueDataset() {
    this(null);
  }
  public DefaultValueDataset(Number value) {
    super();
    this.value = value;
  }
  public DefaultValueDataset(double value) {
    this(new Double(value));
  }
  public Number getValue() {
    return this.value;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(obj instanceof ValueDataset) {
      ValueDataset vd = (ValueDataset)obj;
      return ObjectUtilities.equal(this.value, vd.getValue());
    }
    return false;
  }
  public int hashCode() {
    Number var_3958 = this.value;
    return (var_3958 != null ? this.value.hashCode() : 0);
  }
  public void setValue(Number value) {
    this.value = value;
    fireDatasetChanged(new DatasetChangeInfo());
  }
}