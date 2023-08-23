package org.jfree.chart.renderer.category;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.data.DataUtilities;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;

public class StackedAreaRenderer extends AreaRenderer implements Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = -3595635038460823663L;
  private boolean renderAsPercentages;
  public StackedAreaRenderer() {
    this(false);
  }
  public StackedAreaRenderer(boolean renderAsPercentages) {
    super();
    this.renderAsPercentages = renderAsPercentages;
  }
  public Range findRangeBounds(CategoryDataset dataset) {
    if(this.renderAsPercentages) {
      return new Range(0.0D, 1.0D);
    }
    else {
      return DatasetUtilities.findStackedRangeBounds(dataset);
    }
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof StackedAreaRenderer)) {
      return false;
    }
    StackedAreaRenderer that = (StackedAreaRenderer)obj;
    if(this.renderAsPercentages != that.renderAsPercentages) {
      return false;
    }
    return super.equals(obj);
  }
  public boolean getRenderAsPercentages() {
    return this.renderAsPercentages;
  }
  protected double getPreviousHeight(CategoryDataset dataset, int series, int category) {
    double result = 0.0D;
    Number n;
    double total = 0.0D;
    if(this.renderAsPercentages) {
      total = DataUtilities.calculateColumnTotal(dataset, category);
    }
    for(int i = 0; i < series; i++) {
      n = dataset.getValue(i, category);
      if(n != null) {
        double v = n.doubleValue();
        if(this.renderAsPercentages) {
          v = v / total;
        }
        result += v;
      }
    }
    return result;
  }
  private double[] adjustedStackValues(double[] stack1, double[] stack2) {
    double[] result = new double[2];
    double var_2719 = stack2[0];
    if(stack1[0] == 0.0D || var_2719 == 0.0D) {
      result[0] = 0.0D;
    }
    else {
      result[0] = (stack1[0] + stack2[0]) / 2.0D;
    }
    if(stack1[1] == 0.0D || stack2[1] == 0.0D) {
      result[1] = 0.0D;
    }
    else {
      result[1] = (stack1[1] + stack2[1]) / 2.0D;
    }
    return result;
  }
  private double[] averageStackValues(double[] stack1, double[] stack2) {
    double[] result = new double[2];
    result[0] = (stack1[0] + stack2[0]) / 2.0D;
    result[1] = (stack1[1] + stack2[1]) / 2.0D;
    return result;
  }
  protected double[] getStackValues(CategoryDataset dataset, int series, int index) {
    double[] result = new double[2];
    for(int i = 0; i < series; i++) {
      if(isSeriesVisible(i)) {
        double v = 0.0D;
        Number n = dataset.getValue(i, index);
        if(n != null) {
          v = n.doubleValue();
        }
        if(!Double.isNaN(v)) {
          if(v >= 0.0D) {
            result[1] += v;
          }
          else {
            result[0] += v;
          }
        }
      }
    }
    return result;
  }
  public int getPassCount() {
    return 2;
  }
  public void drawItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, boolean selected, int pass) {
    Shape entityArea = null;
    EntityCollection entities = state.getEntityCollection();
    double y1 = 0.0D;
    Number n = dataset.getValue(row, column);
    if(n != null) {
      y1 = n.doubleValue();
    }
    double[] stack1 = getStackValues(dataset, row, column);
    double xx1 = domainAxis.getCategoryMiddle(column, getColumnCount(), dataArea, plot.getDomainAxisEdge());
    double y0 = 0.0D;
    n = dataset.getValue(row, Math.max(column - 1, 0));
    if(n != null) {
      y0 = n.doubleValue();
    }
    double[] stack0 = getStackValues(dataset, row, Math.max(column - 1, 0));
    double xx0 = domainAxis.getCategoryStart(column, getColumnCount(), dataArea, plot.getDomainAxisEdge());
    int itemCount = dataset.getColumnCount();
    double y2 = 0.0D;
    n = dataset.getValue(row, Math.min(column + 1, itemCount - 1));
    if(n != null) {
      y2 = n.doubleValue();
    }
    double[] stack2 = getStackValues(dataset, row, Math.min(column + 1, itemCount - 1));
    double xx2 = domainAxis.getCategoryEnd(column, getColumnCount(), dataArea, plot.getDomainAxisEdge());
    double xxLeft = xx0;
    double xxRight = xx2;
    double[] stackLeft = averageStackValues(stack0, stack1);
    double[] stackRight = averageStackValues(stack1, stack2);
    double[] adjStackLeft = adjustedStackValues(stack0, stack1);
    double[] adjStackRight = adjustedStackValues(stack1, stack2);
    float transY1;
    RectangleEdge edge1 = plot.getRangeAxisEdge();
    GeneralPath left = new GeneralPath();
    GeneralPath right = new GeneralPath();
    if(y1 >= 0.0D) {
      transY1 = (float)rangeAxis.valueToJava2D(y1 + stack1[1], dataArea, edge1);
      float transStack1 = (float)rangeAxis.valueToJava2D(stack1[1], dataArea, edge1);
      float transStackLeft = (float)rangeAxis.valueToJava2D(adjStackLeft[1], dataArea, edge1);
      if(y0 >= 0.0D) {
        double yleft = (y0 + y1) / 2.0D + stackLeft[1];
        float transYLeft = (float)rangeAxis.valueToJava2D(yleft, dataArea, edge1);
        left.moveTo((float)xx1, transY1);
        left.lineTo((float)xx1, transStack1);
        left.lineTo((float)xxLeft, transStackLeft);
        left.lineTo((float)xxLeft, transYLeft);
        left.closePath();
      }
      else {
        left.moveTo((float)xx1, transStack1);
        left.lineTo((float)xx1, transY1);
        left.lineTo((float)xxLeft, transStackLeft);
        left.closePath();
      }
      float transStackRight = (float)rangeAxis.valueToJava2D(adjStackRight[1], dataArea, edge1);
      if(y2 >= 0.0D) {
        double yright = (y1 + y2) / 2.0D + stackRight[1];
        float transYRight = (float)rangeAxis.valueToJava2D(yright, dataArea, edge1);
        right.moveTo((float)xx1, transStack1);
        right.lineTo((float)xx1, transY1);
        right.lineTo((float)xxRight, transYRight);
        right.lineTo((float)xxRight, transStackRight);
        right.closePath();
      }
      else {
        right.moveTo((float)xx1, transStack1);
        right.lineTo((float)xx1, transY1);
        right.lineTo((float)xxRight, transStackRight);
        right.closePath();
      }
    }
    else {
      transY1 = (float)rangeAxis.valueToJava2D(y1 + stack1[0], dataArea, edge1);
      float transStack1 = (float)rangeAxis.valueToJava2D(stack1[0], dataArea, edge1);
      float transStackLeft = (float)rangeAxis.valueToJava2D(adjStackLeft[0], dataArea, edge1);
      if(y0 >= 0.0D) {
        left.moveTo((float)xx1, transStack1);
        left.lineTo((float)xx1, transY1);
        left.lineTo((float)xxLeft, transStackLeft);
        left.clone();
      }
      else {
        double yleft = (y0 + y1) / 2.0D + stackLeft[0];
        float transYLeft = (float)rangeAxis.valueToJava2D(yleft, dataArea, edge1);
        left.moveTo((float)xx1, transY1);
        left.lineTo((float)xx1, transStack1);
        left.lineTo((float)xxLeft, transStackLeft);
        left.lineTo((float)xxLeft, transYLeft);
        left.closePath();
      }
      float transStackRight = (float)rangeAxis.valueToJava2D(adjStackRight[0], dataArea, edge1);
      if(y2 >= 0.0D) {
        right.moveTo((float)xx1, transStack1);
        right.lineTo((float)xx1, transY1);
        right.lineTo((float)xxRight, transStackRight);
        right.closePath();
      }
      else {
        double yright = (y1 + y2) / 2.0D + stackRight[0];
        float transYRight = (float)rangeAxis.valueToJava2D(yright, dataArea, edge1);
        right.moveTo((float)xx1, transStack1);
        right.lineTo((float)xx1, transY1);
        right.lineTo((float)xxRight, transYRight);
        right.lineTo((float)xxRight, transStackRight);
        right.closePath();
      }
    }
    g2.setPaint(getItemPaint(row, column, selected));
    g2.setStroke(getItemStroke(row, column, selected));
    Paint itemPaint = getItemPaint(row, column, selected);
    if(pass == 0) {
      g2.setPaint(itemPaint);
      g2.fill(left);
      g2.fill(right);
    }
    if(entities != null) {
      GeneralPath gp = new GeneralPath(left);
      gp.append(right, false);
      entityArea = gp;
      addEntity(entities, entityArea, dataset, row, column, selected);
    }
  }
  public void setRenderAsPercentages(boolean asPercentages) {
    this.renderAsPercentages = asPercentages;
    fireChangeEvent();
  }
}