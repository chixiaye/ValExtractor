package org.jfree.chart.labels;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jfree.chart.util.HashUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.xy.XYDataset;

public class MultipleXYSeriesLabelGenerator implements XYSeriesLabelGenerator, Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = 138976236941898560L;
  final public static String DEFAULT_LABEL_FORMAT = "{0}";
  private String formatPattern;
  private String additionalFormatPattern;
  private Map seriesLabelLists;
  public MultipleXYSeriesLabelGenerator() {
    this(DEFAULT_LABEL_FORMAT);
  }
  public MultipleXYSeriesLabelGenerator(String format) {
    super();
    if(format == null) {
      throw new IllegalArgumentException("Null \'format\' argument.");
    }
    this.formatPattern = format;
    this.additionalFormatPattern = "\n{0}";
    this.seriesLabelLists = new HashMap();
  }
  public Object clone() throws CloneNotSupportedException {
    MultipleXYSeriesLabelGenerator clone = (MultipleXYSeriesLabelGenerator)super.clone();
    clone.seriesLabelLists = new HashMap();
    Set keys = this.seriesLabelLists.keySet();
    Iterator iterator = keys.iterator();
    while(iterator.hasNext()){
      Object key = iterator.next();
      Object entry = this.seriesLabelLists.get(key);
      Object toAdd = entry;
      if(entry instanceof PublicCloneable) {
        PublicCloneable pc = (PublicCloneable)entry;
        toAdd = pc.clone();
      }
      clone.seriesLabelLists.put(key, toAdd);
    }
    return clone;
  }
  protected Object[] createItemArray(XYDataset dataset, int series) {
    Object[] result = new Object[1];
    result[0] = dataset.getSeriesKey(series).toString();
    return result;
  }
  public String generateLabel(XYDataset dataset, int series) {
    if(dataset == null) {
      throw new IllegalArgumentException("Null \'dataset\' argument.");
    }
    StringBuffer label = new StringBuffer();
    label.append(MessageFormat.format(this.formatPattern, createItemArray(dataset, series)));
    Integer key = new Integer(series);
    List extraLabels = (List)this.seriesLabelLists.get(key);
    if(extraLabels != null) {
      Object[] temp = new Object[1];
      for(int i = 0; i < extraLabels.size(); i++) {
        temp[0] = extraLabels.get(i);
        String labelAddition = MessageFormat.format(this.additionalFormatPattern, temp);
        label.append(labelAddition);
      }
    }
    return label.toString();
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof MultipleXYSeriesLabelGenerator)) {
      return false;
    }
    MultipleXYSeriesLabelGenerator that = (MultipleXYSeriesLabelGenerator)obj;
    if(!this.formatPattern.equals(that.formatPattern)) {
      return false;
    }
    if(!this.additionalFormatPattern.equals(that.additionalFormatPattern)) {
      return false;
    }
    if(!this.seriesLabelLists.equals(that.seriesLabelLists)) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    int result = 127;
    result = HashUtilities.hashCode(result, this.formatPattern);
    result = HashUtilities.hashCode(result, this.additionalFormatPattern);
    result = HashUtilities.hashCode(result, this.seriesLabelLists);
    return result;
  }
  public void addSeriesLabel(int series, String label) {
    Integer key = new Integer(series);
    Map var_1173 = this.seriesLabelLists;
    List labelList = (List)var_1173.get(key);
    if(labelList == null) {
      labelList = new java.util.ArrayList();
      this.seriesLabelLists.put(key, labelList);
    }
    labelList.add(label);
  }
  public void clearSeriesLabels(int series) {
    Integer key = new Integer(series);
    this.seriesLabelLists.put(key, null);
  }
}