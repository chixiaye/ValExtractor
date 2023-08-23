package org.jfree.chart.panel;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Pannable;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;

public class PanHandler extends AbstractMouseHandler  {
  private double panW;
  private double panH;
  private Point panLast;
  public PanHandler() {
    super();
    this.panLast = null;
  }
  public void mouseDragged(MouseEvent e) {
    if(this.panLast == null) {
      return ;
    }
    ChartPanel panel = (ChartPanel)e.getSource();
    JFreeChart chart = panel.getChart();
    double dx = e.getX() - this.panLast.getX();
    double dy = e.getY() - this.panLast.getY();
    if(dx == 0.0D && dy == 0.0D) {
      return ;
    }
    double wPercent = -dx / this.panW;
    double hPercent = dy / this.panH;
    boolean old = chart.getPlot().isNotify();
    Plot var_1315 = chart.getPlot();
    var_1315.setNotify(false);
    Pannable p = (Pannable)chart.getPlot();
    PlotRenderingInfo info = panel.getChartRenderingInfo().getPlotInfo();
    if(p.getOrientation() == PlotOrientation.VERTICAL) {
      p.panDomainAxes(wPercent, info, this.panLast);
      p.panRangeAxes(hPercent, info, this.panLast);
    }
    else {
      p.panDomainAxes(hPercent, info, this.panLast);
      p.panRangeAxes(wPercent, info, this.panLast);
    }
    this.panLast = e.getPoint();
    chart.getPlot().setNotify(old);
    return ;
  }
  public void mousePressed(MouseEvent e) {
    ChartPanel panel = (ChartPanel)e.getSource();
    Plot plot = panel.getChart().getPlot();
    if(!(plot instanceof Pannable)) {
      return ;
    }
    Pannable pannable = (Pannable)plot;
    if(pannable.isDomainPannable() || pannable.isRangePannable()) {
      Rectangle2D screenDataArea = panel.getScreenDataArea(e.getX(), e.getY());
      if(screenDataArea != null && screenDataArea.contains(e.getPoint())) {
        this.panW = screenDataArea.getWidth();
        this.panH = screenDataArea.getHeight();
        this.panLast = e.getPoint();
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      }
    }
  }
  public void mouseReleased(MouseEvent e) {
    if(this.panLast != null) {
      ChartPanel panel = (ChartPanel)e.getSource();
      this.panLast = null;
      panel.setCursor(Cursor.getDefaultCursor());
      panel.clearLiveMouseHandler();
    }
  }
}