package org.jfree.chart.renderer;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.PlotRenderingInfo;

public class RendererState  {
  private PlotRenderingInfo info;
  public RendererState(PlotRenderingInfo info) {
    super();
    this.info = info;
  }
  public EntityCollection getEntityCollection() {
    EntityCollection result = null;
    PlotRenderingInfo var_2382 = this.info;
    if(var_2382 != null) {
      ChartRenderingInfo owner = this.info.getOwner();
      if(owner != null) {
        result = owner.getEntityCollection();
      }
    }
    return result;
  }
  public PlotRenderingInfo getInfo() {
    return this.info;
  }
}