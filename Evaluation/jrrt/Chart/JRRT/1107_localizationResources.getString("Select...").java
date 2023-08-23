package org.jfree.chart.editor;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.ui.LCBLayout;
import org.jfree.chart.ui.PaintSample;
import org.jfree.chart.ui.StrokeChooserPanel;
import org.jfree.chart.ui.StrokeSample;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.ResourceBundleWrapper;

class DefaultPlotEditor extends JPanel implements ActionListener  {
  final private static String[] orientationNames = { "Vertical", "Horizontal" } ;
  final private static int ORIENTATION_VERTICAL = 0;
  final private static int ORIENTATION_HORIZONTAL = 1;
  private PaintSample backgroundPaintSample;
  private StrokeSample outlineStrokeSample;
  private PaintSample outlinePaintSample;
  private DefaultAxisEditor domainAxisPropertyPanel;
  private DefaultAxisEditor rangeAxisPropertyPanel;
  private StrokeSample[] availableStrokeSamples;
  private RectangleInsets plotInsets;
  private PlotOrientation plotOrientation;
  private JComboBox orientationCombo;
  private Boolean drawLines;
  private JCheckBox drawLinesCheckBox;
  private Boolean drawShapes;
  private JCheckBox drawShapesCheckBox;
  protected static ResourceBundle localizationResources = ResourceBundleWrapper.getBundle("org.jfree.chart.editor.LocalizationBundle");
  public DefaultPlotEditor(Plot plot) {
    super();
    this.plotInsets = plot.getInsets();
    this.backgroundPaintSample = new PaintSample(plot.getBackgroundPaint());
    this.outlineStrokeSample = new StrokeSample(plot.getOutlineStroke());
    this.outlinePaintSample = new PaintSample(plot.getOutlinePaint());
    if(plot instanceof CategoryPlot) {
      this.plotOrientation = ((CategoryPlot)plot).getOrientation();
    }
    else 
      if(plot instanceof XYPlot) {
        this.plotOrientation = ((XYPlot)plot).getOrientation();
      }
    if(plot instanceof CategoryPlot) {
      CategoryItemRenderer renderer = ((CategoryPlot)plot).getRenderer();
      if(renderer instanceof LineAndShapeRenderer) {
        LineAndShapeRenderer r = (LineAndShapeRenderer)renderer;
        this.drawLines = Boolean.valueOf(r.getBaseLinesVisible());
        this.drawShapes = Boolean.valueOf(r.getBaseShapesVisible());
      }
    }
    else 
      if(plot instanceof XYPlot) {
        XYItemRenderer renderer = ((XYPlot)plot).getRenderer();
        if(renderer instanceof StandardXYItemRenderer) {
          StandardXYItemRenderer r = (StandardXYItemRenderer)renderer;
          this.drawLines = Boolean.valueOf(r.getPlotLines());
          this.drawShapes = Boolean.valueOf(r.getBaseShapesVisible());
        }
      }
    setLayout(new BorderLayout());
    this.availableStrokeSamples = new StrokeSample[4];
    this.availableStrokeSamples[0] = new StrokeSample(null);
    this.availableStrokeSamples[1] = new StrokeSample(new BasicStroke(1.0F));
    this.availableStrokeSamples[2] = new StrokeSample(new BasicStroke(2.0F));
    this.availableStrokeSamples[3] = new StrokeSample(new BasicStroke(3.0F));
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), plot.getPlotType() + localizationResources.getString(":")));
    JPanel general = new JPanel(new BorderLayout());
    general.setBorder(BorderFactory.createTitledBorder(localizationResources.getString("General")));
    JPanel interior = new JPanel(new LCBLayout(7));
    interior.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    interior.add(new JLabel(localizationResources.getString("Outline_stroke")));
    String var_1107 = localizationResources.getString("Select...");
    JButton button = new JButton(var_1107);
    button.setActionCommand("OutlineStroke");
    button.addActionListener(this);
    interior.add(this.outlineStrokeSample);
    interior.add(button);
    interior.add(new JLabel(localizationResources.getString("Outline_Paint")));
    button = new JButton(localizationResources.getString("Select..."));
    button.setActionCommand("OutlinePaint");
    button.addActionListener(this);
    interior.add(this.outlinePaintSample);
    interior.add(button);
    interior.add(new JLabel(localizationResources.getString("Background_paint")));
    button = new JButton(localizationResources.getString("Select..."));
    button.setActionCommand("BackgroundPaint");
    button.addActionListener(this);
    interior.add(this.backgroundPaintSample);
    interior.add(button);
    if(this.plotOrientation != null) {
      boolean isVertical = this.plotOrientation.equals(PlotOrientation.VERTICAL);
      int index = isVertical ? ORIENTATION_VERTICAL : ORIENTATION_HORIZONTAL;
      interior.add(new JLabel(localizationResources.getString("Orientation")));
      this.orientationCombo = new JComboBox(orientationNames);
      this.orientationCombo.setSelectedIndex(index);
      this.orientationCombo.setActionCommand("Orientation");
      this.orientationCombo.addActionListener(this);
      interior.add(new JPanel());
      interior.add(this.orientationCombo);
    }
    if(this.drawLines != null) {
      interior.add(new JLabel(localizationResources.getString("Draw_lines")));
      this.drawLinesCheckBox = new JCheckBox();
      this.drawLinesCheckBox.setSelected(this.drawLines.booleanValue());
      this.drawLinesCheckBox.setActionCommand("DrawLines");
      this.drawLinesCheckBox.addActionListener(this);
      interior.add(new JPanel());
      interior.add(this.drawLinesCheckBox);
    }
    if(this.drawShapes != null) {
      interior.add(new JLabel(localizationResources.getString("Draw_shapes")));
      this.drawShapesCheckBox = new JCheckBox();
      this.drawShapesCheckBox.setSelected(this.drawShapes.booleanValue());
      this.drawShapesCheckBox.setActionCommand("DrawShapes");
      this.drawShapesCheckBox.addActionListener(this);
      interior.add(new JPanel());
      interior.add(this.drawShapesCheckBox);
    }
    general.add(interior, BorderLayout.NORTH);
    JPanel appearance = new JPanel(new BorderLayout());
    appearance.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    appearance.add(general, BorderLayout.NORTH);
    JTabbedPane tabs = new JTabbedPane();
    tabs.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    Axis domainAxis = null;
    if(plot instanceof CategoryPlot) {
      domainAxis = ((CategoryPlot)plot).getDomainAxis();
    }
    else 
      if(plot instanceof XYPlot) {
        domainAxis = ((XYPlot)plot).getDomainAxis();
      }
    this.domainAxisPropertyPanel = DefaultAxisEditor.getInstance(domainAxis);
    if(this.domainAxisPropertyPanel != null) {
      this.domainAxisPropertyPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      tabs.add(localizationResources.getString("Domain_Axis"), this.domainAxisPropertyPanel);
    }
    Axis rangeAxis = null;
    if(plot instanceof CategoryPlot) {
      rangeAxis = ((CategoryPlot)plot).getRangeAxis();
    }
    else 
      if(plot instanceof XYPlot) {
        rangeAxis = ((XYPlot)plot).getRangeAxis();
      }
    this.rangeAxisPropertyPanel = DefaultAxisEditor.getInstance(rangeAxis);
    if(this.rangeAxisPropertyPanel != null) {
      this.rangeAxisPropertyPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      tabs.add(localizationResources.getString("Range_Axis"), this.rangeAxisPropertyPanel);
    }
    tabs.add(localizationResources.getString("Appearance"), appearance);
    panel.add(tabs);
    add(panel);
  }
  public DefaultAxisEditor getDomainAxisPropertyEditPanel() {
    return this.domainAxisPropertyPanel;
  }
  public DefaultAxisEditor getRangeAxisPropertyEditPanel() {
    return this.rangeAxisPropertyPanel;
  }
  public Paint getBackgroundPaint() {
    return this.backgroundPaintSample.getPaint();
  }
  public Paint getOutlinePaint() {
    return this.outlinePaintSample.getPaint();
  }
  public RectangleInsets getPlotInsets() {
    if(this.plotInsets == null) {
      this.plotInsets = new RectangleInsets(0.0D, 0.0D, 0.0D, 0.0D);
    }
    return this.plotInsets;
  }
  public Stroke getOutlineStroke() {
    return this.outlineStrokeSample.getStroke();
  }
  public void actionPerformed(ActionEvent event) {
    String command = event.getActionCommand();
    if(command.equals("BackgroundPaint")) {
      attemptBackgroundPaintSelection();
    }
    else 
      if(command.equals("OutlineStroke")) {
        attemptOutlineStrokeSelection();
      }
      else 
        if(command.equals("OutlinePaint")) {
          attemptOutlinePaintSelection();
        }
        else 
          if(command.equals("Orientation")) {
            attemptOrientationSelection();
          }
          else 
            if(command.equals("DrawLines")) {
              attemptDrawLinesSelection();
            }
            else 
              if(command.equals("DrawShapes")) {
                attemptDrawShapesSelection();
              }
  }
  private void attemptBackgroundPaintSelection() {
    Color c;
    c = JColorChooser.showDialog(this, localizationResources.getString("Background_Color"), Color.blue);
    if(c != null) {
      this.backgroundPaintSample.setPaint(c);
    }
  }
  private void attemptDrawLinesSelection() {
    this.drawLines = Boolean.valueOf(this.drawLinesCheckBox.isSelected());
  }
  private void attemptDrawShapesSelection() {
    this.drawShapes = Boolean.valueOf(this.drawShapesCheckBox.isSelected());
  }
  private void attemptOrientationSelection() {
    int index = this.orientationCombo.getSelectedIndex();
    if(index == ORIENTATION_VERTICAL) {
      this.plotOrientation = PlotOrientation.VERTICAL;
    }
    else {
      this.plotOrientation = PlotOrientation.HORIZONTAL;
    }
  }
  private void attemptOutlinePaintSelection() {
    Color c;
    c = JColorChooser.showDialog(this, localizationResources.getString("Outline_Color"), Color.blue);
    if(c != null) {
      this.outlinePaintSample.setPaint(c);
    }
  }
  private void attemptOutlineStrokeSelection() {
    StrokeChooserPanel panel = new StrokeChooserPanel(this.outlineStrokeSample, this.availableStrokeSamples);
    int result = JOptionPane.showConfirmDialog(this, panel, localizationResources.getString("Stroke_Selection"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if(result == JOptionPane.OK_OPTION) {
      this.outlineStrokeSample.setStroke(panel.getSelectedStroke());
    }
  }
  public void updatePlotProperties(Plot plot) {
    plot.setOutlinePaint(getOutlinePaint());
    plot.setOutlineStroke(getOutlineStroke());
    plot.setBackgroundPaint(getBackgroundPaint());
    plot.setInsets(getPlotInsets());
    if(this.domainAxisPropertyPanel != null) {
      Axis domainAxis = null;
      if(plot instanceof CategoryPlot) {
        CategoryPlot p = (CategoryPlot)plot;
        domainAxis = p.getDomainAxis();
      }
      else 
        if(plot instanceof XYPlot) {
          XYPlot p = (XYPlot)plot;
          domainAxis = p.getDomainAxis();
        }
      if(domainAxis != null) {
        this.domainAxisPropertyPanel.setAxisProperties(domainAxis);
      }
    }
    if(this.rangeAxisPropertyPanel != null) {
      Axis rangeAxis = null;
      if(plot instanceof CategoryPlot) {
        CategoryPlot p = (CategoryPlot)plot;
        rangeAxis = p.getRangeAxis();
      }
      else 
        if(plot instanceof XYPlot) {
          XYPlot p = (XYPlot)plot;
          rangeAxis = p.getRangeAxis();
        }
      if(rangeAxis != null) {
        this.rangeAxisPropertyPanel.setAxisProperties(rangeAxis);
      }
    }
    if(this.plotOrientation != null) {
      if(plot instanceof CategoryPlot) {
        CategoryPlot p = (CategoryPlot)plot;
        p.setOrientation(this.plotOrientation);
      }
      else 
        if(plot instanceof XYPlot) {
          XYPlot p = (XYPlot)plot;
          p.setOrientation(this.plotOrientation);
        }
    }
    if(this.drawLines != null) {
      if(plot instanceof CategoryPlot) {
        CategoryPlot p = (CategoryPlot)plot;
        CategoryItemRenderer r = p.getRenderer();
        if(r instanceof LineAndShapeRenderer) {
          ((LineAndShapeRenderer)r).setBaseLinesVisible(this.drawLines.booleanValue());
        }
      }
      else 
        if(plot instanceof XYPlot) {
          XYPlot p = (XYPlot)plot;
          XYItemRenderer r = p.getRenderer();
          if(r instanceof StandardXYItemRenderer) {
            ((StandardXYItemRenderer)r).setPlotLines(this.drawLines.booleanValue());
          }
        }
    }
    if(this.drawShapes != null) {
      if(plot instanceof CategoryPlot) {
        CategoryPlot p = (CategoryPlot)plot;
        CategoryItemRenderer r = p.getRenderer();
        if(r instanceof LineAndShapeRenderer) {
          ((LineAndShapeRenderer)r).setBaseShapesVisible(this.drawShapes.booleanValue());
        }
      }
      else 
        if(plot instanceof XYPlot) {
          XYPlot p = (XYPlot)plot;
          XYItemRenderer r = p.getRenderer();
          if(r instanceof StandardXYItemRenderer) {
            ((StandardXYItemRenderer)r).setBaseShapesVisible(this.drawShapes.booleanValue());
          }
        }
    }
  }
}