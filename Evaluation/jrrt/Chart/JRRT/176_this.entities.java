package org.jfree.chart;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtilities;

public class ChartRenderingInfo implements Cloneable, Serializable  {
  final private static long serialVersionUID = 2751952018173406822L;
  private transient Rectangle2D chartArea;
  private PlotRenderingInfo plotInfo;
  private EntityCollection entities;
  private RenderingSource renderingSource;
  public ChartRenderingInfo() {
    this(new StandardEntityCollection());
  }
  public ChartRenderingInfo(EntityCollection entities) {
    super();
    this.chartArea = new Rectangle2D.Double();
    this.plotInfo = new PlotRenderingInfo(this);
    this.entities = entities;
  }
  public EntityCollection getEntityCollection() {
    return this.entities;
  }
  public Object clone() throws CloneNotSupportedException {
    ChartRenderingInfo clone = (ChartRenderingInfo)super.clone();
    if(this.chartArea != null) {
      clone.chartArea = (Rectangle2D)this.chartArea.clone();
    }
    if(this.entities instanceof PublicCloneable) {
      PublicCloneable pc = (PublicCloneable)this.entities;
      clone.entities = (EntityCollection)pc.clone();
    }
    return clone;
  }
  public PlotRenderingInfo getPlotInfo() {
    return this.plotInfo;
  }
  public Rectangle2D getChartArea() {
    return this.chartArea;
  }
  public RenderingSource getRenderingSource() {
    return this.renderingSource;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof ChartRenderingInfo)) {
      return false;
    }
    ChartRenderingInfo that = (ChartRenderingInfo)obj;
    if(!ObjectUtilities.equal(this.chartArea, that.chartArea)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.plotInfo, that.plotInfo)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.entities, that.entities)) {
      return false;
    }
    return true;
  }
  public void clear() {
    this.chartArea.setRect(0.0D, 0.0D, 0.0D, 0.0D);
    this.plotInfo = new PlotRenderingInfo(this);
    EntityCollection var_176 = this.entities;
    if(var_176 != null) {
      this.entities.clear();
    }
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.chartArea = (Rectangle2D)SerialUtilities.readShape(stream);
  }
  public void setChartArea(Rectangle2D area) {
    this.chartArea.setRect(area);
  }
  public void setEntityCollection(EntityCollection entities) {
    this.entities = entities;
  }
  public void setRenderingSource(RenderingSource source) {
    this.renderingSource = source;
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writeShape(this.chartArea, stream);
  }
}