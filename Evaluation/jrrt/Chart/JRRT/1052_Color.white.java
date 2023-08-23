package org.jfree.chart.demo;
import java.awt.Color;
import java.text.SimpleDateFormat;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.ApplicationFrame;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.RefineryUtilities;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

public class TimeSeriesChartDemo1 extends ApplicationFrame  {
  public TimeSeriesChartDemo1(String title) {
    super(title);
    ChartPanel chartPanel = (ChartPanel)createDemoPanel();
    chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
    setContentPane(chartPanel);
  }
  private static JFreeChart createChart(XYDataset dataset) {
    JFreeChart chart = ChartFactory.createTimeSeriesChart("Legal & General Unit Trust Prices", "Date", "Price Per Unit", dataset, true);
    Color var_1052 = Color.white;
    chart.setBackgroundPaint(var_1052);
    XYPlot plot = (XYPlot)chart.getPlot();
    plot.setBackgroundPaint(Color.lightGray);
    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.white);
    plot.setAxisOffset(new RectangleInsets(5.0D, 5.0D, 5.0D, 5.0D));
    plot.setDomainCrosshairVisible(true);
    plot.setRangeCrosshairVisible(true);
    XYItemRenderer r = plot.getRenderer();
    if(r instanceof XYLineAndShapeRenderer) {
      XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)r;
      renderer.setBaseShapesVisible(true);
      renderer.setBaseShapesFilled(true);
      renderer.setDrawSeriesLineAsPath(true);
    }
    DateAxis axis = (DateAxis)plot.getDomainAxis();
    axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));
    return chart;
  }
  public static JPanel createDemoPanel() {
    JFreeChart chart = createChart(createDataset());
    ChartPanel panel = new ChartPanel(chart);
    panel.setFillZoomRectangle(true);
    panel.setMouseWheelEnabled(true);
    return panel;
  }
  private static XYDataset createDataset() {
    TimeSeries s1 = new TimeSeries("L&G European Index Trust");
    s1.add(new Month(2, 2001), 181.8D);
    s1.add(new Month(3, 2001), 167.3D);
    s1.add(new Month(4, 2001), 153.8D);
    s1.add(new Month(5, 2001), 167.6D);
    s1.add(new Month(6, 2001), 158.8D);
    s1.add(new Month(7, 2001), 148.3D);
    s1.add(new Month(8, 2001), 153.9D);
    s1.add(new Month(9, 2001), 142.7D);
    s1.add(new Month(10, 2001), 123.2D);
    s1.add(new Month(11, 2001), 131.8D);
    s1.add(new Month(12, 2001), 139.6D);
    s1.add(new Month(1, 2002), 142.9D);
    s1.add(new Month(2, 2002), 138.7D);
    s1.add(new Month(3, 2002), 137.3D);
    s1.add(new Month(4, 2002), 143.9D);
    s1.add(new Month(5, 2002), 139.8D);
    s1.add(new Month(6, 2002), 137.0D);
    s1.add(new Month(7, 2002), 132.8D);
    TimeSeries s2 = new TimeSeries("L&G UK Index Trust");
    s2.add(new Month(2, 2001), 129.6D);
    s2.add(new Month(3, 2001), 123.2D);
    s2.add(new Month(4, 2001), 117.2D);
    s2.add(new Month(5, 2001), 124.1D);
    s2.add(new Month(6, 2001), 122.6D);
    s2.add(new Month(7, 2001), 119.2D);
    s2.add(new Month(8, 2001), 116.5D);
    s2.add(new Month(9, 2001), 112.7D);
    s2.add(new Month(10, 2001), 101.5D);
    s2.add(new Month(11, 2001), 106.1D);
    s2.add(new Month(12, 2001), 110.3D);
    s2.add(new Month(1, 2002), 111.7D);
    s2.add(new Month(2, 2002), 111.0D);
    s2.add(new Month(3, 2002), 109.6D);
    s2.add(new Month(4, 2002), 113.2D);
    s2.add(new Month(5, 2002), 111.6D);
    s2.add(new Month(6, 2002), 108.8D);
    s2.add(new Month(7, 2002), 101.6D);
    TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(s1);
    dataset.addSeries(s2);
    return dataset;
  }
  public static void main(String[] args) {
    TimeSeriesChartDemo1 demo = new TimeSeriesChartDemo1("Time Series Chart Demo 1");
    demo.pack();
    RefineryUtilities.centerFrameOnScreen(demo);
    demo.setVisible(true);
  }
}