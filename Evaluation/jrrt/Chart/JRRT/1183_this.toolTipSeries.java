package org.jfree.chart.labels;
import java.io.Serializable;
import java.util.List;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.xy.XYDataset;

public class CustomXYToolTipGenerator implements XYToolTipGenerator, Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = 8636030004670141362L;
  private List toolTipSeries = new java.util.ArrayList();
  public CustomXYToolTipGenerator() {
    super();
  }
  public Object clone() throws CloneNotSupportedException {
    CustomXYToolTipGenerator clone = (CustomXYToolTipGenerator)super.clone();
    List var_1183 = this.toolTipSeries;
    if(var_1183 != null) {
      clone.toolTipSeries = new java.util.ArrayList(this.toolTipSeries);
    }
    return clone;
  }
  public String generateToolTip(XYDataset data, int series, int item) {
    return getToolTipText(series, item);
  }
  public String getToolTipText(int series, int item) {
    String result = null;
    if(series < getListCount()) {
      List tooltips = (List)this.toolTipSeries.get(series);
      if(tooltips != null) {
        if(item < tooltips.size()) {
          result = (String)tooltips.get(item);
        }
      }
    }
    return result;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(obj instanceof CustomXYToolTipGenerator) {
      CustomXYToolTipGenerator generator = (CustomXYToolTipGenerator)obj;
      boolean result = true;
      for(int series = 0; series < getListCount(); series++) {
        for(int item = 0; item < getToolTipCount(series); item++) {
          String t1 = getToolTipText(series, item);
          String t2 = generator.getToolTipText(series, item);
          if(t1 != null) {
            result = result && t1.equals(t2);
          }
          else {
            result = result && (t2 == null);
          }
        }
      }
      return result;
    }
    return false;
  }
  public int getListCount() {
    return this.toolTipSeries.size();
  }
  public int getToolTipCount(int list) {
    int result = 0;
    List tooltips = (List)this.toolTipSeries.get(list);
    if(tooltips != null) {
      result = tooltips.size();
    }
    return result;
  }
  public void addToolTipSeries(List toolTips) {
    this.toolTipSeries.add(toolTips);
  }
}