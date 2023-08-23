package org.jfree.data.statistics;
import java.io.Serializable;
import org.jfree.chart.util.ObjectUtilities;

public class MeanAndStandardDeviation implements Serializable  {
  final private static long serialVersionUID = 7413468697315721515L;
  private Number mean;
  private Number standardDeviation;
  public MeanAndStandardDeviation(Number mean, Number standardDeviation) {
    super();
    this.mean = mean;
    this.standardDeviation = standardDeviation;
  }
  public MeanAndStandardDeviation(double mean, double standardDeviation) {
    this(new Double(mean), new Double(standardDeviation));
  }
  public Number getMean() {
    return this.mean;
  }
  public Number getStandardDeviation() {
    return this.standardDeviation;
  }
  public String toString() {
    return "[" + this.mean + ", " + this.standardDeviation + "]";
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof MeanAndStandardDeviation)) {
      return false;
    }
    MeanAndStandardDeviation that = (MeanAndStandardDeviation)obj;
    if(!ObjectUtilities.equal(this.mean, that.mean)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.standardDeviation, that.standardDeviation)) {
      return false;
    }
    return true;
  }
  public double getMeanValue() {
    double result = Double.NaN;
    Number var_4099 = this.mean;
    if(var_4099 != null) {
      result = this.mean.doubleValue();
    }
    return result;
  }
  public double getStandardDeviationValue() {
    double result = Double.NaN;
    if(this.standardDeviation != null) {
      result = this.standardDeviation.doubleValue();
    }
    return result;
  }
}