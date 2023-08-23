package org.jfree.chart.renderer.category;
import org.jfree.chart.plot.CategoryCrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.renderer.RendererState;
import org.jfree.data.category.CategoryDatasetSelectionState;

public class CategoryItemRendererState extends RendererState  {
  private double barWidth;
  private double seriesRunningTotal;
  private int[] visibleSeries;
  private CategoryCrosshairState crosshairState;
  private CategoryDatasetSelectionState selectionState;
  public CategoryItemRendererState(PlotRenderingInfo info) {
    super(info);
    this.barWidth = 0.0D;
    this.seriesRunningTotal = 0.0D;
  }
  public CategoryCrosshairState getCrosshairState() {
    return this.crosshairState;
  }
  public CategoryDatasetSelectionState getSelectionState() {
    return this.selectionState;
  }
  public double getBarWidth() {
    return this.barWidth;
  }
  public double getSeriesRunningTotal() {
    return this.seriesRunningTotal;
  }
  public int getVisibleSeriesCount() {
    if(this.visibleSeries == null) {
      return -1;
    }
    return this.visibleSeries.length;
  }
  public int getVisibleSeriesIndex(int rowIndex) {
    if(this.visibleSeries == null) {
      return rowIndex;
    }
    int index = -1;
    for(int vRow = 0; vRow < this.visibleSeries.length; vRow++) {
      if(this.visibleSeries[vRow] == rowIndex) {
        index = vRow;
        break ;
      }
    }
    return index;
  }
  public int[] getVisibleSeriesArray() {
    if(this.visibleSeries == null) {
      return null;
    }
    int var_2625 = this.visibleSeries.length;
    int[] result = new int[var_2625];
    System.arraycopy(this.visibleSeries, 0, result, 0, this.visibleSeries.length);
    return result;
  }
  public void setBarWidth(double width) {
    this.barWidth = width;
  }
  public void setCrosshairState(CategoryCrosshairState state) {
    this.crosshairState = state;
  }
  public void setSelectionState(CategoryDatasetSelectionState state) {
    this.selectionState = state;
  }
  void setSeriesRunningTotal(double total) {
    this.seriesRunningTotal = total;
  }
  public void setVisibleSeriesArray(int[] visibleSeries) {
    this.visibleSeries = visibleSeries;
  }
}