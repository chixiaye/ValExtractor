package org.jfree.chart.renderer;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.WaferMapPlot;
import org.jfree.data.general.WaferMapDataset;

public class WaferMapRenderer extends AbstractRenderer  {
  private Map paintIndex;
  private WaferMapPlot plot;
  private int paintLimit;
  final private static int DEFAULT_PAINT_LIMIT = 35;
  final public static int POSITION_INDEX = 0;
  final public static int VALUE_INDEX = 1;
  private int paintIndexMethod;
  public WaferMapRenderer() {
    this(null, null);
  }
  public WaferMapRenderer(Integer paintLimit, Integer paintIndexMethod) {
    super();
    this.paintIndex = new HashMap();
    if(paintLimit == null) {
      this.paintLimit = DEFAULT_PAINT_LIMIT;
    }
    else {
      this.paintLimit = paintLimit.intValue();
    }
    this.paintIndexMethod = VALUE_INDEX;
    if(paintIndexMethod != null) {
      if(isMethodValid(paintIndexMethod.intValue())) {
        this.paintIndexMethod = paintIndexMethod.intValue();
      }
    }
  }
  public WaferMapRenderer(int paintLimit, int paintIndexMethod) {
    this(new Integer(paintLimit), new Integer(paintIndexMethod));
  }
  public DrawingSupplier getDrawingSupplier() {
    DrawingSupplier result = null;
    WaferMapPlot p = getPlot();
    if(p != null) {
      result = p.getDrawingSupplier();
    }
    return result;
  }
  public LegendItemCollection getLegendCollection() {
    LegendItemCollection result = new LegendItemCollection();
    if(this.paintIndex != null && this.paintIndex.size() > 0) {
      if(this.paintIndex.size() <= this.paintLimit) {
        for(java.util.Iterator i = this.paintIndex.entrySet().iterator(); i.hasNext(); ) {
          Map.Entry entry = (Map.Entry)i.next();
          String label = entry.getKey().toString();
          String description = label;
          Shape shape = new Rectangle2D.Double(1D, 1D, 1D, 1D);
          int var_2369 = ((Integer)entry.getValue()).intValue();
          Paint paint = lookupSeriesPaint(var_2369);
          Paint outlinePaint = Color.black;
          Stroke outlineStroke = DEFAULT_STROKE;
          result.add(new LegendItem(label, description, null, null, shape, paint, outlineStroke, outlinePaint));
        }
      }
      else {
        Set unique = new HashSet();
        for(java.util.Iterator i = this.paintIndex.entrySet().iterator(); i.hasNext(); ) {
          Map.Entry entry = (Map.Entry)i.next();
          if(unique.add(entry.getValue())) {
            String label = getMinPaintValue((Integer)entry.getValue()).toString() + " - " + getMaxPaintValue((Integer)entry.getValue()).toString();
            String description = label;
            Shape shape = new Rectangle2D.Double(1D, 1D, 1D, 1D);
            Paint paint = getSeriesPaint(((Integer)entry.getValue()).intValue());
            Paint outlinePaint = Color.black;
            Stroke outlineStroke = DEFAULT_STROKE;
            result.add(new LegendItem(label, description, null, null, shape, paint, outlineStroke, outlinePaint));
          }
        }
      }
    }
    return result;
  }
  private Number getMaxPaintValue(Integer index) {
    double maxValue = Double.NEGATIVE_INFINITY;
    for(java.util.Iterator i = this.paintIndex.entrySet().iterator(); i.hasNext(); ) {
      Map.Entry entry = (Map.Entry)i.next();
      if(((Integer)entry.getValue()).equals(index)) {
        if(((Number)entry.getKey()).doubleValue() > maxValue) {
          maxValue = ((Number)entry.getKey()).doubleValue();
        }
      }
    }
    return new Double(maxValue);
  }
  private Number getMinPaintValue(Integer index) {
    double minValue = Double.POSITIVE_INFINITY;
    for(java.util.Iterator i = this.paintIndex.entrySet().iterator(); i.hasNext(); ) {
      Map.Entry entry = (Map.Entry)i.next();
      if(((Integer)entry.getValue()).equals(index)) {
        if(((Number)entry.getKey()).doubleValue() < minValue) {
          minValue = ((Number)entry.getKey()).doubleValue();
        }
      }
    }
    return new Double(minValue);
  }
  public Paint getChipColor(Number value) {
    return getSeriesPaint(getPaintIndex(value));
  }
  public WaferMapPlot getPlot() {
    return this.plot;
  }
  private boolean isMethodValid(int method) {
    switch (method){
      case POSITION_INDEX:
      return true;
      case VALUE_INDEX:
      return true;
      default:
      return false;
    }
  }
  private int getPaintIndex(Number value) {
    return ((Integer)this.paintIndex.get(value)).intValue();
  }
  private void makePaintIndex() {
    if(this.plot == null) {
      return ;
    }
    WaferMapDataset data = this.plot.getDataset();
    Number dataMin = data.getMinValue();
    Number dataMax = data.getMaxValue();
    Set uniqueValues = data.getUniqueValues();
    if(uniqueValues.size() <= this.paintLimit) {
      int count = 0;
      for(java.util.Iterator i = uniqueValues.iterator(); i.hasNext(); ) {
        this.paintIndex.put(i.next(), new Integer(count++));
      }
    }
    else {
      switch (this.paintIndexMethod){
        case POSITION_INDEX:
        makePositionIndex(uniqueValues);
        break ;
        case VALUE_INDEX:
        makeValueIndex(dataMax, dataMin, uniqueValues);
        break ;
        default:
        break ;
      }
    }
  }
  private void makePositionIndex(Set uniqueValues) {
    int valuesPerColor = (int)Math.ceil((double)uniqueValues.size() / this.paintLimit);
    int count = 0;
    int paint = 0;
    for(java.util.Iterator i = uniqueValues.iterator(); i.hasNext(); ) {
      this.paintIndex.put(i.next(), new Integer(paint));
      if(++count % valuesPerColor == 0) {
        paint++;
      }
      if(paint > this.paintLimit) {
        paint = this.paintLimit;
      }
    }
  }
  private void makeValueIndex(Number max, Number min, Set uniqueValues) {
    double valueRange = max.doubleValue() - min.doubleValue();
    double valueStep = valueRange / this.paintLimit;
    int paint = 0;
    double cutPoint = min.doubleValue() + valueStep;
    for(java.util.Iterator i = uniqueValues.iterator(); i.hasNext(); ) {
      Number value = (Number)i.next();
      while(value.doubleValue() > cutPoint){
        cutPoint += valueStep;
        paint++;
        if(paint > this.paintLimit) {
          paint = this.paintLimit;
        }
      }
      this.paintIndex.put(value, new Integer(paint));
    }
  }
  public void setPlot(WaferMapPlot plot) {
    this.plot = plot;
    makePaintIndex();
  }
}