package org.jfree.chart;
import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.EventListenerList;
import org.jfree.chart.editor.ChartEditor;
import org.jfree.chart.editor.ChartEditorManager;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.panel.Overlay;
import org.jfree.chart.event.OverlayChangeEvent;
import org.jfree.chart.event.OverlayChangeListener;
import org.jfree.chart.panel.AbstractMouseHandler;
import org.jfree.chart.panel.PanHandler;
import org.jfree.chart.panel.ZoomHandler;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.Zoomable;
import org.jfree.chart.ui.ExtensionFileFilter;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.chart.util.SerialUtilities;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetAndSelection;
import org.jfree.data.general.DatasetSelectionState;

public class ChartPanel extends JPanel implements ChartChangeListener, ChartProgressListener, ActionListener, MouseListener, MouseMotionListener, OverlayChangeListener, RenderingSource, Printable, Serializable  {
  final private static long serialVersionUID = 6046366297214274674L;
  final public static boolean DEFAULT_BUFFER_USED = true;
  final public static int DEFAULT_WIDTH = 680;
  final public static int DEFAULT_HEIGHT = 420;
  final public static int DEFAULT_MINIMUM_DRAW_WIDTH = 300;
  final public static int DEFAULT_MINIMUM_DRAW_HEIGHT = 200;
  final public static int DEFAULT_MAXIMUM_DRAW_WIDTH = 1024;
  final public static int DEFAULT_MAXIMUM_DRAW_HEIGHT = 768;
  final public static int DEFAULT_ZOOM_TRIGGER_DISTANCE = 10;
  final public static String PROPERTIES_COMMAND = "PROPERTIES";
  final public static String COPY_COMMAND = "COPY";
  final public static String SAVE_COMMAND = "SAVE";
  final public static String PRINT_COMMAND = "PRINT";
  final public static String ZOOM_IN_BOTH_COMMAND = "ZOOM_IN_BOTH";
  final public static String ZOOM_IN_DOMAIN_COMMAND = "ZOOM_IN_DOMAIN";
  final public static String ZOOM_IN_RANGE_COMMAND = "ZOOM_IN_RANGE";
  final public static String ZOOM_OUT_BOTH_COMMAND = "ZOOM_OUT_BOTH";
  final public static String ZOOM_OUT_DOMAIN_COMMAND = "ZOOM_DOMAIN_BOTH";
  final public static String ZOOM_OUT_RANGE_COMMAND = "ZOOM_RANGE_BOTH";
  final public static String ZOOM_RESET_BOTH_COMMAND = "ZOOM_RESET_BOTH";
  final public static String ZOOM_RESET_DOMAIN_COMMAND = "ZOOM_RESET_DOMAIN";
  final public static String ZOOM_RESET_RANGE_COMMAND = "ZOOM_RESET_RANGE";
  private JFreeChart chart;
  private transient EventListenerList chartMouseListeners;
  private boolean useBuffer;
  private boolean refreshBuffer;
  private transient Image chartBuffer;
  private int chartBufferHeight;
  private int chartBufferWidth;
  private int minimumDrawWidth;
  private int minimumDrawHeight;
  private int maximumDrawWidth;
  private int maximumDrawHeight;
  private JPopupMenu popup;
  private ChartRenderingInfo info;
  private Point2D anchor;
  private double scaleX;
  private double scaleY;
  private PlotOrientation orientation = PlotOrientation.VERTICAL;
  private boolean domainZoomable = false;
  private boolean rangeZoomable = false;
  private Point2D zoomPoint = null;
  private transient Rectangle2D zoomRectangle = null;
  private boolean fillZoomRectangle = true;
  private int zoomTriggerDistance;
  private JMenuItem zoomInBothMenuItem;
  private JMenuItem zoomInDomainMenuItem;
  private JMenuItem zoomInRangeMenuItem;
  private JMenuItem zoomOutBothMenuItem;
  private JMenuItem zoomOutDomainMenuItem;
  private JMenuItem zoomOutRangeMenuItem;
  private JMenuItem zoomResetBothMenuItem;
  private JMenuItem zoomResetDomainMenuItem;
  private JMenuItem zoomResetRangeMenuItem;
  private File defaultDirectoryForSaveAs;
  private boolean enforceFileExtensions;
  private boolean ownToolTipDelaysActive;
  private int originalToolTipInitialDelay;
  private int originalToolTipReshowDelay;
  private int originalToolTipDismissDelay;
  private int ownToolTipInitialDelay;
  private int ownToolTipReshowDelay;
  private int ownToolTipDismissDelay;
  private double zoomInFactor = 0.5D;
  private double zoomOutFactor = 2.0D;
  private boolean zoomAroundAnchor;
  private transient Paint zoomOutlinePaint;
  private transient Paint zoomFillPaint;
  protected static ResourceBundle localizationResources = ResourceBundleWrapper.getBundle("org.jfree.chart.LocalizationBundle");
  private List overlays;
  private List availableMouseHandlers;
  private AbstractMouseHandler liveMouseHandler;
  private List auxiliaryMouseHandlers;
  private ZoomHandler zoomHandler;
  private List selectionStates = new java.util.ArrayList();
  private Shape selectionShape;
  private Paint selectionFillPaint;
  private Paint selectionOutlinePaint = Color.darkGray;
  private Stroke selectionOutlineStroke = new BasicStroke(1.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 4.0F, new float[]{ 3.0F, 3.0F } , 0.0F);
  private MouseWheelHandler mouseWheelHandler;
  public ChartPanel(JFreeChart chart) {
    this(chart, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_MINIMUM_DRAW_WIDTH, DEFAULT_MINIMUM_DRAW_HEIGHT, DEFAULT_MAXIMUM_DRAW_WIDTH, DEFAULT_MAXIMUM_DRAW_HEIGHT, DEFAULT_BUFFER_USED, true, true, true, true, true);
  }
  public ChartPanel(JFreeChart chart, boolean properties, boolean save, boolean print, boolean zoom, boolean tooltips) {
    this(chart, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_MINIMUM_DRAW_WIDTH, DEFAULT_MINIMUM_DRAW_HEIGHT, DEFAULT_MAXIMUM_DRAW_WIDTH, DEFAULT_MAXIMUM_DRAW_HEIGHT, DEFAULT_BUFFER_USED, properties, save, print, zoom, tooltips);
  }
  public ChartPanel(JFreeChart chart, boolean useBuffer) {
    this(chart, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_MINIMUM_DRAW_WIDTH, DEFAULT_MINIMUM_DRAW_HEIGHT, DEFAULT_MAXIMUM_DRAW_WIDTH, DEFAULT_MAXIMUM_DRAW_HEIGHT, useBuffer, true, true, true, true, true);
  }
  public ChartPanel(JFreeChart chart, int width, int height, int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth, int maximumDrawHeight, boolean useBuffer, boolean properties, boolean copy, boolean save, boolean print, boolean zoom, boolean tooltips) {
    super();
    setChart(chart);
    this.chartMouseListeners = new EventListenerList();
    this.info = new ChartRenderingInfo();
    this.info.setRenderingSource(this);
    setPreferredSize(new Dimension(width, height));
    this.useBuffer = useBuffer;
    this.refreshBuffer = false;
    this.minimumDrawWidth = minimumDrawWidth;
    this.minimumDrawHeight = minimumDrawHeight;
    this.maximumDrawWidth = maximumDrawWidth;
    this.maximumDrawHeight = maximumDrawHeight;
    this.zoomTriggerDistance = DEFAULT_ZOOM_TRIGGER_DISTANCE;
    this.popup = null;
    if(properties || copy || save || print || zoom) {
      this.popup = createPopupMenu(properties, copy, save, print, zoom);
    }
    enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
    setDisplayToolTips(tooltips);
    addMouseListener(this);
    addMouseMotionListener(this);
    this.defaultDirectoryForSaveAs = null;
    this.enforceFileExtensions = true;
    ToolTipManager ttm = ToolTipManager.sharedInstance();
    this.ownToolTipInitialDelay = ttm.getInitialDelay();
    this.ownToolTipDismissDelay = ttm.getDismissDelay();
    this.ownToolTipReshowDelay = ttm.getReshowDelay();
    this.zoomAroundAnchor = false;
    this.zoomOutlinePaint = Color.blue;
    this.zoomFillPaint = new Color(0, 0, 255, 63);
    this.overlays = new java.util.ArrayList();
    this.availableMouseHandlers = new java.util.ArrayList();
    this.zoomHandler = new ZoomHandler();
    this.availableMouseHandlers.add(zoomHandler);
    PanHandler panHandler = new PanHandler();
    int panMask = InputEvent.CTRL_MASK;
    String osName = System.getProperty("os.name").toLowerCase();
    if(osName.startsWith("mac os x")) {
      panMask = InputEvent.ALT_MASK;
    }
    panHandler.setModifier(panMask);
    this.availableMouseHandlers.add(panHandler);
    this.auxiliaryMouseHandlers = new java.util.ArrayList();
  }
  public ChartPanel(JFreeChart chart, int width, int height, int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth, int maximumDrawHeight, boolean useBuffer, boolean properties, boolean save, boolean print, boolean zoom, boolean tooltips) {
    this(chart, width, height, minimumDrawWidth, minimumDrawHeight, maximumDrawWidth, maximumDrawHeight, useBuffer, properties, true, save, print, zoom, tooltips);
  }
  public ChartEntity getEntityForPoint(int viewX, int viewY) {
    ChartEntity result = null;
    if(this.info != null) {
      Insets insets = getInsets();
      double x = (viewX - insets.left) / this.scaleX;
      double y = (viewY - insets.top) / this.scaleY;
      EntityCollection entities = this.info.getEntityCollection();
      result = entities != null ? entities.getEntity(x, y) : null;
    }
    return result;
  }
  public ChartRenderingInfo getChartRenderingInfo() {
    return this.info;
  }
  public DatasetSelectionState getSelectionState(Dataset dataset) {
    Iterator iterator = this.selectionStates.iterator();
    while(iterator.hasNext()){
      DatasetAndSelection das = (DatasetAndSelection)iterator.next();
      if(das.getDataset() == dataset) {
        return das.getSelection();
      }
    }
    return null;
  }
  public EventListener[] getListeners(Class listenerType) {
    if(listenerType == ChartMouseListener.class) {
      return this.chartMouseListeners.getListeners(listenerType);
    }
    else {
      return super.getListeners(listenerType);
    }
  }
  public File getDefaultDirectoryForSaveAs() {
    return this.defaultDirectoryForSaveAs;
  }
  public Graphics2D createGraphics2D() {
    return (Graphics2D)getGraphics().create();
  }
  public JFreeChart getChart() {
    return this.chart;
  }
  protected JPopupMenu createPopupMenu(boolean properties, boolean save, boolean print, boolean zoom) {
    return createPopupMenu(properties, false, save, print, zoom);
  }
  protected JPopupMenu createPopupMenu(boolean properties, boolean copy, boolean save, boolean print, boolean zoom) {
    JPopupMenu result = new JPopupMenu("Chart:");
    boolean separator = false;
    if(properties) {
      JMenuItem propertiesItem = new JMenuItem(localizationResources.getString("Properties..."));
      propertiesItem.setActionCommand(PROPERTIES_COMMAND);
      propertiesItem.addActionListener(this);
      result.add(propertiesItem);
      separator = true;
    }
    if(copy) {
      if(separator) {
        result.addSeparator();
        separator = false;
      }
      JMenuItem copyItem = new JMenuItem(localizationResources.getString("Copy"));
      copyItem.setActionCommand(COPY_COMMAND);
      copyItem.addActionListener(this);
      result.add(copyItem);
      separator = !save;
    }
    if(save) {
      if(separator) {
        result.addSeparator();
        separator = false;
      }
      JMenuItem saveItem = new JMenuItem(localizationResources.getString("Save_as..."));
      saveItem.setActionCommand(SAVE_COMMAND);
      saveItem.addActionListener(this);
      result.add(saveItem);
      separator = true;
    }
    if(print) {
      if(separator) {
        result.addSeparator();
        separator = false;
      }
      JMenuItem printItem = new JMenuItem(localizationResources.getString("Print..."));
      printItem.setActionCommand(PRINT_COMMAND);
      printItem.addActionListener(this);
      result.add(printItem);
      separator = true;
    }
    if(zoom) {
      if(separator) {
        result.addSeparator();
        separator = false;
      }
      JMenu zoomInMenu = new JMenu(localizationResources.getString("Zoom_In"));
      this.zoomInBothMenuItem = new JMenuItem(localizationResources.getString("All_Axes"));
      this.zoomInBothMenuItem.setActionCommand(ZOOM_IN_BOTH_COMMAND);
      this.zoomInBothMenuItem.addActionListener(this);
      zoomInMenu.add(this.zoomInBothMenuItem);
      zoomInMenu.addSeparator();
      this.zoomInDomainMenuItem = new JMenuItem(localizationResources.getString("Domain_Axis"));
      this.zoomInDomainMenuItem.setActionCommand(ZOOM_IN_DOMAIN_COMMAND);
      this.zoomInDomainMenuItem.addActionListener(this);
      zoomInMenu.add(this.zoomInDomainMenuItem);
      this.zoomInRangeMenuItem = new JMenuItem(localizationResources.getString("Range_Axis"));
      this.zoomInRangeMenuItem.setActionCommand(ZOOM_IN_RANGE_COMMAND);
      this.zoomInRangeMenuItem.addActionListener(this);
      zoomInMenu.add(this.zoomInRangeMenuItem);
      result.add(zoomInMenu);
      JMenu zoomOutMenu = new JMenu(localizationResources.getString("Zoom_Out"));
      this.zoomOutBothMenuItem = new JMenuItem(localizationResources.getString("All_Axes"));
      this.zoomOutBothMenuItem.setActionCommand(ZOOM_OUT_BOTH_COMMAND);
      this.zoomOutBothMenuItem.addActionListener(this);
      zoomOutMenu.add(this.zoomOutBothMenuItem);
      zoomOutMenu.addSeparator();
      this.zoomOutDomainMenuItem = new JMenuItem(localizationResources.getString("Domain_Axis"));
      this.zoomOutDomainMenuItem.setActionCommand(ZOOM_OUT_DOMAIN_COMMAND);
      this.zoomOutDomainMenuItem.addActionListener(this);
      zoomOutMenu.add(this.zoomOutDomainMenuItem);
      this.zoomOutRangeMenuItem = new JMenuItem(localizationResources.getString("Range_Axis"));
      this.zoomOutRangeMenuItem.setActionCommand(ZOOM_OUT_RANGE_COMMAND);
      this.zoomOutRangeMenuItem.addActionListener(this);
      zoomOutMenu.add(this.zoomOutRangeMenuItem);
      result.add(zoomOutMenu);
      JMenu autoRangeMenu = new JMenu(localizationResources.getString("Auto_Range"));
      this.zoomResetBothMenuItem = new JMenuItem(localizationResources.getString("All_Axes"));
      this.zoomResetBothMenuItem.setActionCommand(ZOOM_RESET_BOTH_COMMAND);
      this.zoomResetBothMenuItem.addActionListener(this);
      autoRangeMenu.add(this.zoomResetBothMenuItem);
      autoRangeMenu.addSeparator();
      this.zoomResetDomainMenuItem = new JMenuItem(localizationResources.getString("Domain_Axis"));
      this.zoomResetDomainMenuItem.setActionCommand(ZOOM_RESET_DOMAIN_COMMAND);
      this.zoomResetDomainMenuItem.addActionListener(this);
      autoRangeMenu.add(this.zoomResetDomainMenuItem);
      this.zoomResetRangeMenuItem = new JMenuItem(localizationResources.getString("Range_Axis"));
      this.zoomResetRangeMenuItem.setActionCommand(ZOOM_RESET_RANGE_COMMAND);
      this.zoomResetRangeMenuItem.addActionListener(this);
      autoRangeMenu.add(this.zoomResetRangeMenuItem);
      result.addSeparator();
      result.add(autoRangeMenu);
    }
    return result;
  }
  public JPopupMenu getPopupMenu() {
    return this.popup;
  }
  public Paint getSelectionFillPaint() {
    return this.selectionFillPaint;
  }
  public Paint getSelectionOutlinePaint() {
    return this.selectionOutlinePaint;
  }
  public Paint getZoomFillPaint() {
    return this.zoomFillPaint;
  }
  public Paint getZoomOutlinePaint() {
    return this.zoomOutlinePaint;
  }
  public PlotOrientation getOrientation() {
    return this.orientation;
  }
  public Point translateJava2DToScreen(Point2D java2DPoint) {
    Insets insets = getInsets();
    int x = (int)(java2DPoint.getX() * this.scaleX + insets.left);
    int y = (int)(java2DPoint.getY() * this.scaleY + insets.top);
    return new Point(x, y);
  }
  public Point2D getAnchor() {
    return this.anchor;
  }
  public Point2D translateScreenToJava2D(Point screenPoint) {
    Insets insets = getInsets();
    double x = (screenPoint.getX() - insets.left) / this.scaleX;
    double y = (screenPoint.getY() - insets.top) / this.scaleY;
    return new Point2D.Double(x, y);
  }
  public Rectangle2D getScreenDataArea() {
    Rectangle2D dataArea = this.info.getPlotInfo().getDataArea();
    Insets insets = getInsets();
    double x = dataArea.getX() * this.scaleX + insets.left;
    double y = dataArea.getY() * this.scaleY + insets.top;
    double w = dataArea.getWidth() * this.scaleX;
    double h = dataArea.getHeight() * this.scaleY;
    return new Rectangle2D.Double(x, y, w, h);
  }
  public Rectangle2D getScreenDataArea(int x, int y) {
    PlotRenderingInfo plotInfo = this.info.getPlotInfo();
    Rectangle2D result;
    if(plotInfo.getSubplotCount() == 0) {
      result = getScreenDataArea();
    }
    else {
      Point2D selectOrigin = translateScreenToJava2D(new Point(x, y));
      int subplotIndex = plotInfo.getSubplotIndex(selectOrigin);
      if(subplotIndex == -1) {
        return null;
      }
      result = scale(plotInfo.getSubplotInfo(subplotIndex).getDataArea());
    }
    return result;
  }
  public Rectangle2D getZoomRectangle() {
    return this.zoomRectangle;
  }
  public Rectangle2D scale(Rectangle2D rect) {
    Insets insets = getInsets();
    double x = rect.getX() * getScaleX() + insets.left;
    double y = rect.getY() * getScaleY() + insets.top;
    double w = rect.getWidth() * getScaleX();
    double h = rect.getHeight() * getScaleY();
    return new Rectangle2D.Double(x, y, w, h);
  }
  public Shape getSelectionShape() {
    return this.selectionShape;
  }
  public String getToolTipText(MouseEvent e) {
    String result = null;
    if(this.info != null) {
      EntityCollection entities = this.info.getEntityCollection();
      if(entities != null) {
        Insets insets = getInsets();
        ChartEntity entity = entities.getEntity((int)((e.getX() - insets.left) / this.scaleX), (int)((e.getY() - insets.top) / this.scaleY));
        if(entity != null) {
          result = entity.getToolTipText();
        }
      }
    }
    return result;
  }
  public Stroke getSelectionOutlineStroke() {
    return this.selectionOutlineStroke;
  }
  public ZoomHandler getZoomHandler() {
    return this.zoomHandler;
  }
  public boolean getFillZoomRectangle() {
    return this.fillZoomRectangle;
  }
  public boolean getRefreshBuffer() {
    return this.refreshBuffer;
  }
  public boolean getUseBuffer() {
    return this.useBuffer;
  }
  public boolean getZoomAroundAnchor() {
    return this.zoomAroundAnchor;
  }
  public boolean isDomainZoomable() {
    return this.domainZoomable;
  }
  public boolean isEnforceFileExtensions() {
    return this.enforceFileExtensions;
  }
  public boolean isMouseWheelEnabled() {
    return this.mouseWheelHandler != null;
  }
  public boolean isRangeZoomable() {
    return this.rangeZoomable;
  }
  public boolean removeMouseHandler(AbstractMouseHandler handler) {
    if(handler == null) {
      throw new IllegalArgumentException("Null \'handler\' argument.");
    }
    return this.availableMouseHandlers.remove(handler);
  }
  public double getScaleX() {
    return this.scaleX;
  }
  public double getScaleY() {
    return this.scaleY;
  }
  public double getZoomInFactor() {
    return this.zoomInFactor;
  }
  public double getZoomOutFactor() {
    return this.zoomOutFactor;
  }
  public int getDismissDelay() {
    return this.ownToolTipDismissDelay;
  }
  public int getInitialDelay() {
    return this.ownToolTipInitialDelay;
  }
  public int getMaximumDrawHeight() {
    return this.maximumDrawHeight;
  }
  public int getMaximumDrawWidth() {
    return this.maximumDrawWidth;
  }
  public int getMinimumDrawHeight() {
    return this.minimumDrawHeight;
  }
  public int getMinimumDrawWidth() {
    return this.minimumDrawWidth;
  }
  public int getReshowDelay() {
    return this.ownToolTipReshowDelay;
  }
  public int getZoomTriggerDistance() {
    return this.zoomTriggerDistance;
  }
  public int print(Graphics g, PageFormat pf, int pageIndex) {
    if(pageIndex != 0) {
      return NO_SUCH_PAGE;
    }
    Graphics2D g2 = (Graphics2D)g;
    double x = pf.getImageableX();
    double y = pf.getImageableY();
    double w = pf.getImageableWidth();
    double h = pf.getImageableHeight();
    this.chart.draw(g2, new Rectangle2D.Double(x, y, w, h), this.anchor, null);
    return PAGE_EXISTS;
  }
  public void actionPerformed(ActionEvent event) {
    String command = event.getActionCommand();
    double screenX = -1.0D;
    double screenY = -1.0D;
    if(this.zoomPoint != null) {
      screenX = this.zoomPoint.getX();
      screenY = this.zoomPoint.getY();
    }
    if(command.equals(PROPERTIES_COMMAND)) {
      doEditChartProperties();
    }
    else 
      if(command.equals(COPY_COMMAND)) {
        doCopy();
      }
      else 
        if(command.equals(SAVE_COMMAND)) {
          try {
            doSaveAs();
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
        else 
          if(command.equals(PRINT_COMMAND)) {
            createChartPrintJob();
          }
          else 
            if(command.equals(ZOOM_IN_BOTH_COMMAND)) {
              zoomInBoth(screenX, screenY);
            }
            else 
              if(command.equals(ZOOM_IN_DOMAIN_COMMAND)) {
                zoomInDomain(screenX, screenY);
              }
              else 
                if(command.equals(ZOOM_IN_RANGE_COMMAND)) {
                  zoomInRange(screenX, screenY);
                }
                else 
                  if(command.equals(ZOOM_OUT_BOTH_COMMAND)) {
                    zoomOutBoth(screenX, screenY);
                  }
                  else 
                    if(command.equals(ZOOM_OUT_DOMAIN_COMMAND)) {
                      zoomOutDomain(screenX, screenY);
                    }
                    else 
                      if(command.equals(ZOOM_OUT_RANGE_COMMAND)) {
                        zoomOutRange(screenX, screenY);
                      }
                      else 
                        if(command.equals(ZOOM_RESET_BOTH_COMMAND)) {
                          restoreAutoBounds();
                        }
                        else 
                          if(command.equals(ZOOM_RESET_DOMAIN_COMMAND)) {
                            restoreAutoDomainBounds();
                          }
                          else 
                            if(command.equals(ZOOM_RESET_RANGE_COMMAND)) {
                              restoreAutoRangeBounds();
                            }
  }
  public void addChartMouseListener(ChartMouseListener listener) {
    if(listener == null) {
      throw new IllegalArgumentException("Null \'listener\' argument.");
    }
    this.chartMouseListeners.add(ChartMouseListener.class, listener);
  }
  public void addMouseHandler(AbstractMouseHandler handler) {
    if(handler == null) {
      throw new IllegalArgumentException("Null \'handler\' argument.");
    }
    this.availableMouseHandlers.add(handler);
  }
  public void addOverlay(Overlay overlay) {
    if(overlay == null) {
      throw new IllegalArgumentException("Null \'overlay\' argument.");
    }
    this.overlays.add(overlay);
    overlay.addChangeListener(this);
    repaint();
  }
  public void chartChanged(ChartChangeEvent event) {
    this.refreshBuffer = true;
    Plot plot = this.chart.getPlot();
    if(plot instanceof Zoomable) {
      Zoomable z = (Zoomable)plot;
      this.orientation = z.getOrientation();
    }
    repaint();
  }
  public void chartProgress(ChartProgressEvent event) {
  }
  public void clearLiveMouseHandler() {
    this.liveMouseHandler = null;
  }
  public void createChartPrintJob() {
    PrinterJob job = PrinterJob.getPrinterJob();
    PageFormat pf = job.defaultPage();
    PageFormat pf2 = job.pageDialog(pf);
    if(pf2 != pf) {
      job.setPrintable(this, pf2);
      if(job.printDialog()) {
        try {
          job.print();
        }
        catch (PrinterException e) {
          JOptionPane.showMessageDialog(this, e);
        }
      }
    }
  }
  protected void displayPopupMenu(int x, int y) {
    if(this.popup != null) {
      Plot plot = this.chart.getPlot();
      boolean isDomainZoomable = false;
      boolean isRangeZoomable = false;
      if(plot instanceof Zoomable) {
        Zoomable z = (Zoomable)plot;
        isDomainZoomable = z.isDomainZoomable();
        isRangeZoomable = z.isRangeZoomable();
      }
      if(this.zoomInDomainMenuItem != null) {
        this.zoomInDomainMenuItem.setEnabled(isDomainZoomable);
      }
      if(this.zoomOutDomainMenuItem != null) {
        this.zoomOutDomainMenuItem.setEnabled(isDomainZoomable);
      }
      if(this.zoomResetDomainMenuItem != null) {
        this.zoomResetDomainMenuItem.setEnabled(isDomainZoomable);
      }
      if(this.zoomInRangeMenuItem != null) {
        this.zoomInRangeMenuItem.setEnabled(isRangeZoomable);
      }
      if(this.zoomOutRangeMenuItem != null) {
        this.zoomOutRangeMenuItem.setEnabled(isRangeZoomable);
      }
      if(this.zoomResetRangeMenuItem != null) {
        this.zoomResetRangeMenuItem.setEnabled(isRangeZoomable);
      }
      if(this.zoomInBothMenuItem != null) {
        this.zoomInBothMenuItem.setEnabled(isDomainZoomable && isRangeZoomable);
      }
      if(this.zoomOutBothMenuItem != null) {
        this.zoomOutBothMenuItem.setEnabled(isDomainZoomable && isRangeZoomable);
      }
      if(this.zoomResetBothMenuItem != null) {
        this.zoomResetBothMenuItem.setEnabled(isDomainZoomable && isRangeZoomable);
      }
      this.popup.show(this, x, y);
    }
  }
  public void doCopy() {
    Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Insets insets = getInsets();
    int w = getWidth() - insets.left - insets.right;
    int h = getHeight() - insets.top - insets.bottom;
    ChartTransferable selection = new ChartTransferable(this.chart, w, h, getMinimumDrawWidth(), getMinimumDrawHeight(), getMaximumDrawWidth(), getMaximumDrawHeight(), true);
    systemClipboard.setContents(selection, null);
  }
  public void doEditChartProperties() {
    ChartEditor editor = ChartEditorManager.getChartEditor(this.chart);
    int result = JOptionPane.showConfirmDialog(this, editor, localizationResources.getString("Chart_Properties"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if(result == JOptionPane.OK_OPTION) {
      editor.updateChart(this.chart);
    }
  }
  public void doSaveAs() throws IOException {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setCurrentDirectory(this.defaultDirectoryForSaveAs);
    ExtensionFileFilter filter = new ExtensionFileFilter(localizationResources.getString("PNG_Image_Files"), ".png");
    fileChooser.addChoosableFileFilter(filter);
    int option = fileChooser.showSaveDialog(this);
    if(option == JFileChooser.APPROVE_OPTION) {
      String filename = fileChooser.getSelectedFile().getPath();
      if(isEnforceFileExtensions()) {
        if(!filename.endsWith(".png")) {
          filename = filename + ".png";
        }
      }
      ChartUtilities.saveChartAsPNG(new File(filename), this.chart, getWidth(), getHeight());
    }
  }
  private void drawSelectionShape(Graphics2D g2, boolean xor) {
    if(this.selectionShape != null) {
      if(xor) {
        g2.setXORMode(Color.gray);
      }
      if(this.selectionFillPaint != null) {
        g2.setPaint(this.selectionFillPaint);
        g2.fill(this.selectionShape);
      }
      g2.setPaint(this.selectionOutlinePaint);
      g2.setStroke(this.selectionOutlineStroke);
      GeneralPath pp = new GeneralPath(this.selectionShape);
      pp.closePath();
      g2.draw(pp);
      if(xor) {
        g2.setPaintMode();
      }
    }
  }
  private void drawZoomRectangle(Graphics2D g2, boolean xor) {
    if(this.zoomRectangle != null) {
      if(xor) {
        g2.setXORMode(Color.gray);
      }
      if(this.fillZoomRectangle) {
        g2.setPaint(this.zoomFillPaint);
        g2.fill(this.zoomRectangle);
      }
      else {
        g2.setPaint(this.zoomOutlinePaint);
        g2.draw(this.zoomRectangle);
      }
      if(xor) {
        g2.setPaintMode();
      }
    }
  }
  public void mouseClicked(MouseEvent event) {
    if(this.liveMouseHandler != null) {
      this.liveMouseHandler.mouseClicked(event);
    }
    Iterator iterator = this.auxiliaryMouseHandlers.iterator();
    while(iterator.hasNext()){
      AbstractMouseHandler mh = (AbstractMouseHandler)iterator.next();
      mh.mouseClicked(event);
    }
  }
  public void mouseDragged(MouseEvent e) {
    if(this.popup != null && this.popup.isShowing()) {
      return ;
    }
    if(this.liveMouseHandler != null) {
      this.liveMouseHandler.mouseDragged(e);
    }
    Iterator iterator = this.auxiliaryMouseHandlers.iterator();
    while(iterator.hasNext()){
      AbstractMouseHandler handler = (AbstractMouseHandler)iterator.next();
      handler.mouseDragged(e);
    }
  }
  public void mouseEntered(MouseEvent e) {
    if(!this.ownToolTipDelaysActive) {
      ToolTipManager ttm = ToolTipManager.sharedInstance();
      this.originalToolTipInitialDelay = ttm.getInitialDelay();
      ttm.setInitialDelay(this.ownToolTipInitialDelay);
      this.originalToolTipReshowDelay = ttm.getReshowDelay();
      ttm.setReshowDelay(this.ownToolTipReshowDelay);
      this.originalToolTipDismissDelay = ttm.getDismissDelay();
      ttm.setDismissDelay(this.ownToolTipDismissDelay);
      this.ownToolTipDelaysActive = true;
    }
    if(this.liveMouseHandler != null) {
      this.liveMouseHandler.mouseEntered(e);
    }
    Iterator iterator = this.auxiliaryMouseHandlers.iterator();
    while(iterator.hasNext()){
      AbstractMouseHandler h = (AbstractMouseHandler)iterator.next();
      h.mouseEntered(e);
    }
  }
  public void mouseExited(MouseEvent e) {
    if(this.ownToolTipDelaysActive) {
      ToolTipManager ttm = ToolTipManager.sharedInstance();
      ttm.setInitialDelay(this.originalToolTipInitialDelay);
      ttm.setReshowDelay(this.originalToolTipReshowDelay);
      ttm.setDismissDelay(this.originalToolTipDismissDelay);
      this.ownToolTipDelaysActive = false;
    }
    if(this.liveMouseHandler != null) {
      this.liveMouseHandler.mouseExited(e);
    }
    Iterator iterator = this.auxiliaryMouseHandlers.iterator();
    while(iterator.hasNext()){
      AbstractMouseHandler h = (AbstractMouseHandler)iterator.next();
      h.mouseExited(e);
    }
  }
  public void mouseMoved(MouseEvent e) {
    Object[] listeners = this.chartMouseListeners.getListeners(ChartMouseListener.class);
    if(listeners.length == 0) {
      return ;
    }
    Insets insets = getInsets();
    int x = (int)((e.getX() - insets.left) / this.scaleX);
    int y = (int)((e.getY() - insets.top) / this.scaleY);
    ChartEntity entity = null;
    if(this.info != null) {
      EntityCollection entities = this.info.getEntityCollection();
      if(entities != null) {
        entity = entities.getEntity(x, y);
      }
    }
    if(this.chart != null) {
      ChartMouseEvent event = new ChartMouseEvent(getChart(), e, entity);
      for(int i = listeners.length - 1; i >= 0; i -= 1) {
        ((ChartMouseListener)listeners[i]).chartMouseMoved(event);
      }
    }
  }
  public void mousePressed(MouseEvent e) {
    if(this.chart == null) {
      return ;
    }
    int mods = e.getModifiers();
    if(e.isPopupTrigger()) {
      if(this.popup != null) {
        displayPopupMenu(e.getX(), e.getY());
      }
      return ;
    }
    if(this.liveMouseHandler != null) {
      this.liveMouseHandler.mousePressed(e);
    }
    else {
      AbstractMouseHandler h = null;
      boolean found = false;
      Iterator iterator = this.availableMouseHandlers.iterator();
      AbstractMouseHandler nomod = null;
      while(iterator.hasNext() && !found){
        h = (AbstractMouseHandler)iterator.next();
        if(h.getModifier() == 0 && nomod == null) {
          nomod = h;
        }
        else {
          found = (mods & h.getModifier()) == h.getModifier();
        }
      }
      if(!found && nomod != null) {
        h = nomod;
        found = true;
      }
      if(found) {
        this.liveMouseHandler = h;
        this.liveMouseHandler.mousePressed(e);
      }
    }
    Iterator iterator = this.auxiliaryMouseHandlers.iterator();
    while(iterator.hasNext()){
      AbstractMouseHandler handler = (AbstractMouseHandler)iterator.next();
      handler.mousePressed(e);
    }
  }
  public void mouseReleased(MouseEvent e) {
    if(e.isPopupTrigger()) {
      if(this.popup != null) {
        displayPopupMenu(e.getX(), e.getY());
      }
      return ;
    }
    if(this.liveMouseHandler != null) {
      this.liveMouseHandler.mouseReleased(e);
    }
    Iterator iterator = this.auxiliaryMouseHandlers.iterator();
    while(iterator.hasNext()){
      AbstractMouseHandler mh = (AbstractMouseHandler)iterator.next();
      mh.mouseReleased(e);
    }
  }
  public void overlayChanged(OverlayChangeEvent event) {
    repaint();
  }
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    if(this.chart == null) {
      return ;
    }
    Graphics2D g2 = (Graphics2D)g.create();
    Dimension size = getSize();
    Insets insets = getInsets();
    Rectangle2D available = new Rectangle2D.Double(insets.left, insets.top, size.getWidth() - insets.left - insets.right, size.getHeight() - insets.top - insets.bottom);
    boolean scale = false;
    double drawWidth = available.getWidth();
    double drawHeight = available.getHeight();
    this.scaleX = 1.0D;
    this.scaleY = 1.0D;
    if(drawWidth < this.minimumDrawWidth) {
      this.scaleX = drawWidth / this.minimumDrawWidth;
      drawWidth = this.minimumDrawWidth;
      scale = true;
    }
    else 
      if(drawWidth > this.maximumDrawWidth) {
        this.scaleX = drawWidth / this.maximumDrawWidth;
        drawWidth = this.maximumDrawWidth;
        scale = true;
      }
    if(drawHeight < this.minimumDrawHeight) {
      this.scaleY = drawHeight / this.minimumDrawHeight;
      drawHeight = this.minimumDrawHeight;
      scale = true;
    }
    else 
      if(drawHeight > this.maximumDrawHeight) {
        this.scaleY = drawHeight / this.maximumDrawHeight;
        drawHeight = this.maximumDrawHeight;
        scale = true;
      }
    Rectangle2D chartArea = new Rectangle2D.Double(0.0D, 0.0D, drawWidth, drawHeight);
    if(this.useBuffer) {
      if((this.chartBuffer == null) || (this.chartBufferWidth != available.getWidth()) || (this.chartBufferHeight != available.getHeight())) {
        this.chartBufferWidth = (int)available.getWidth();
        this.chartBufferHeight = (int)available.getHeight();
        GraphicsConfiguration gc = g2.getDeviceConfiguration();
        this.chartBuffer = gc.createCompatibleImage(this.chartBufferWidth, this.chartBufferHeight, Transparency.TRANSLUCENT);
        this.refreshBuffer = true;
      }
      if(this.refreshBuffer) {
        this.refreshBuffer = false;
        Rectangle2D bufferArea = new Rectangle2D.Double(0, 0, this.chartBufferWidth, this.chartBufferHeight);
        Graphics2D bufferG2 = (Graphics2D)this.chartBuffer.getGraphics();
        Composite savedComposite = bufferG2.getComposite();
        bufferG2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0F));
        Rectangle r = new Rectangle(0, 0, this.chartBufferWidth, this.chartBufferHeight);
        bufferG2.fill(r);
        bufferG2.setComposite(savedComposite);
        if(scale) {
          AffineTransform saved = bufferG2.getTransform();
          AffineTransform st = AffineTransform.getScaleInstance(this.scaleX, this.scaleY);
          bufferG2.transform(st);
          this.chart.draw(bufferG2, chartArea, this.anchor, this.info);
          bufferG2.setTransform(saved);
        }
        else {
          this.chart.draw(bufferG2, bufferArea, this.anchor, this.info);
        }
      }
      g2.drawImage(this.chartBuffer, insets.left, insets.top, this);
    }
    else {
      AffineTransform saved = g2.getTransform();
      g2.translate(insets.left, insets.top);
      if(scale) {
        AffineTransform st = AffineTransform.getScaleInstance(this.scaleX, this.scaleY);
        g2.transform(st);
      }
      this.chart.draw(g2, chartArea, this.anchor, this.info);
      g2.setTransform(saved);
    }
    Iterator iterator = this.overlays.iterator();
    while(iterator.hasNext()){
      Overlay overlay = (Overlay)iterator.next();
      overlay.paintOverlay(g2, this);
    }
    drawZoomRectangle(g2, !this.useBuffer);
    drawSelectionShape(g2, !this.useBuffer);
    g2.dispose();
    this.anchor = null;
  }
  public void putSelectionState(Dataset dataset, DatasetSelectionState state) {
    this.selectionStates.add(new DatasetAndSelection(dataset, state));
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.zoomFillPaint = SerialUtilities.readPaint(stream);
    this.zoomOutlinePaint = SerialUtilities.readPaint(stream);
    this.chartMouseListeners = new EventListenerList();
    if(this.chart != null) {
      this.chart.addChangeListener(this);
    }
  }
  public void removeChartMouseListener(ChartMouseListener listener) {
    this.chartMouseListeners.remove(ChartMouseListener.class, listener);
  }
  public void removeOverlay(Overlay overlay) {
    if(overlay == null) {
      throw new IllegalArgumentException("Null \'overlay\' argument.");
    }
    boolean removed = this.overlays.remove(overlay);
    if(removed) {
      overlay.removeChangeListener(this);
      repaint();
    }
  }
  public void restoreAutoBounds() {
    Plot plot = this.chart.getPlot();
    if(plot == null) {
      return ;
    }
    boolean savedNotify = plot.isNotify();
    plot.setNotify(false);
    restoreAutoDomainBounds();
    restoreAutoRangeBounds();
    plot.setNotify(savedNotify);
  }
  public void restoreAutoDomainBounds() {
    Plot plot = this.chart.getPlot();
    if(plot instanceof Zoomable) {
      Zoomable z = (Zoomable)plot;
      boolean savedNotify = plot.isNotify();
      plot.setNotify(false);
      Point2D zp = (this.zoomPoint != null ? this.zoomPoint : new Point());
      z.zoomDomainAxes(0.0D, this.info.getPlotInfo(), zp);
      plot.setNotify(savedNotify);
    }
  }
  public void restoreAutoRangeBounds() {
    Plot plot = this.chart.getPlot();
    if(plot instanceof Zoomable) {
      Zoomable z = (Zoomable)plot;
      boolean savedNotify = plot.isNotify();
      plot.setNotify(false);
      Point2D zp = (this.zoomPoint != null ? this.zoomPoint : new Point());
      z.zoomRangeAxes(0.0D, this.info.getPlotInfo(), zp);
      plot.setNotify(savedNotify);
    }
  }
  protected void setAnchor(Point2D anchor) {
    this.anchor = anchor;
  }
  public void setChart(JFreeChart chart) {
    if(this.chart != null) {
      this.chart.removeChangeListener(this);
      this.chart.removeProgressListener(this);
    }
    this.chart = chart;
    if(chart != null) {
      this.chart.addChangeListener(this);
      this.chart.addProgressListener(this);
      Plot plot = chart.getPlot();
      this.domainZoomable = false;
      this.rangeZoomable = false;
      if(plot instanceof Zoomable) {
        Zoomable z = (Zoomable)plot;
        this.domainZoomable = z.isDomainZoomable();
        this.rangeZoomable = z.isRangeZoomable();
        this.orientation = z.getOrientation();
      }
    }
    else {
      this.domainZoomable = false;
      this.rangeZoomable = false;
    }
    if(this.useBuffer) {
      this.refreshBuffer = true;
    }
    repaint();
  }
  public void setDefaultDirectoryForSaveAs(File directory) {
    if(directory != null) {
      if(!directory.isDirectory()) {
        throw new IllegalArgumentException("The \'directory\' argument is not a directory.");
      }
    }
    this.defaultDirectoryForSaveAs = directory;
  }
  public void setDismissDelay(int delay) {
    this.ownToolTipDismissDelay = delay;
  }
  public void setDisplayToolTips(boolean flag) {
    if(flag) {
      ToolTipManager.sharedInstance().registerComponent(this);
    }
    else {
      ToolTipManager.sharedInstance().unregisterComponent(this);
    }
  }
  public void setDomainZoomable(boolean flag) {
    if(flag) {
      Plot plot = this.chart.getPlot();
      if(plot instanceof Zoomable) {
        Zoomable z = (Zoomable)plot;
        this.domainZoomable = flag && (z.isDomainZoomable());
      }
    }
    else {
      this.domainZoomable = false;
    }
  }
  public void setEnforceFileExtensions(boolean enforce) {
    this.enforceFileExtensions = enforce;
  }
  public void setFillZoomRectangle(boolean flag) {
    this.fillZoomRectangle = flag;
  }
  public void setInitialDelay(int delay) {
    this.ownToolTipInitialDelay = delay;
  }
  public void setMaximumDrawHeight(int height) {
    this.maximumDrawHeight = height;
  }
  public void setMaximumDrawWidth(int width) {
    this.maximumDrawWidth = width;
  }
  public void setMinimumDrawHeight(int height) {
    this.minimumDrawHeight = height;
  }
  public void setMinimumDrawWidth(int width) {
    this.minimumDrawWidth = width;
  }
  public void setMouseWheelEnabled(boolean flag) {
    if(flag && this.mouseWheelHandler == null) {
      this.mouseWheelHandler = new MouseWheelHandler(this);
    }
    else 
      if(!flag && this.mouseWheelHandler != null) {
        removeMouseWheelListener(this.mouseWheelHandler);
        this.mouseWheelHandler = null;
      }
  }
  public void setMouseZoomable(boolean flag) {
    setMouseZoomable(flag, true);
  }
  public void setMouseZoomable(boolean flag, boolean fillRectangle) {
    setDomainZoomable(flag);
    setRangeZoomable(flag);
    setFillZoomRectangle(fillRectangle);
  }
  public void setPopupMenu(JPopupMenu popup) {
    this.popup = popup;
  }
  public void setRangeZoomable(boolean flag) {
    if(flag) {
      Plot plot = this.chart.getPlot();
      if(plot instanceof Zoomable) {
        Zoomable z = (Zoomable)plot;
        this.rangeZoomable = flag && (z.isRangeZoomable());
      }
    }
    else {
      this.rangeZoomable = false;
    }
  }
  public void setRefreshBuffer(boolean flag) {
    this.refreshBuffer = flag;
  }
  public void setReshowDelay(int delay) {
    this.ownToolTipReshowDelay = delay;
  }
  public void setSelectionFillPaint(Paint paint) {
    this.selectionFillPaint = paint;
  }
  public void setSelectionOutlinePaint(Paint paint) {
    this.selectionOutlinePaint = paint;
  }
  public void setSelectionOutlineStroke(Stroke stroke) {
    this.selectionOutlineStroke = stroke;
  }
  public void setSelectionShape(Shape shape) {
    this.selectionShape = shape;
  }
  public void setZoomAroundAnchor(boolean zoomAroundAnchor) {
    this.zoomAroundAnchor = zoomAroundAnchor;
  }
  public void setZoomFillPaint(Paint paint) {
    if(paint == null) {
      throw new IllegalArgumentException("Null \'paint\' argument.");
    }
    this.zoomFillPaint = paint;
  }
  public void setZoomInFactor(double factor) {
    this.zoomInFactor = factor;
  }
  public void setZoomOutFactor(double factor) {
    this.zoomOutFactor = factor;
  }
  public void setZoomOutlinePaint(Paint paint) {
    this.zoomOutlinePaint = paint;
  }
  public void setZoomRectangle(Rectangle2D rect) {
    this.zoomRectangle = rect;
  }
  public void setZoomTriggerDistance(int distance) {
    this.zoomTriggerDistance = distance;
  }
  public void updateUI() {
    if(this.popup != null) {
      SwingUtilities.updateComponentTreeUI(this.popup);
    }
    super.updateUI();
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writePaint(this.zoomFillPaint, stream);
    SerialUtilities.writePaint(this.zoomOutlinePaint, stream);
  }
  public void zoom(Rectangle2D selection) {
    Point2D selectOrigin = translateScreenToJava2D(new Point((int)Math.ceil(selection.getX()), (int)Math.ceil(selection.getY())));
    PlotRenderingInfo plotInfo = this.info.getPlotInfo();
    Rectangle2D scaledDataArea = getScreenDataArea((int)selection.getCenterX(), (int)selection.getCenterY());
    if((selection.getHeight() > 0) && (selection.getWidth() > 0)) {
      double hLower = (selection.getMinX() - scaledDataArea.getMinX()) / scaledDataArea.getWidth();
      double hUpper = (selection.getMaxX() - scaledDataArea.getMinX()) / scaledDataArea.getWidth();
      double var_97 = scaledDataArea.getMaxY();
      double vLower = (var_97 - selection.getMaxY()) / scaledDataArea.getHeight();
      double vUpper = (scaledDataArea.getMaxY() - selection.getMinY()) / scaledDataArea.getHeight();
      Plot p = this.chart.getPlot();
      if(p instanceof Zoomable) {
        boolean savedNotify = p.isNotify();
        p.setNotify(false);
        Zoomable z = (Zoomable)p;
        if(z.getOrientation() == PlotOrientation.HORIZONTAL) {
          z.zoomDomainAxes(vLower, vUpper, plotInfo, selectOrigin);
          z.zoomRangeAxes(hLower, hUpper, plotInfo, selectOrigin);
        }
        else {
          z.zoomDomainAxes(hLower, hUpper, plotInfo, selectOrigin);
          z.zoomRangeAxes(vLower, vUpper, plotInfo, selectOrigin);
        }
        p.setNotify(savedNotify);
      }
    }
  }
  public void zoomInBoth(double x, double y) {
    Plot plot = this.chart.getPlot();
    if(plot == null) {
      return ;
    }
    boolean savedNotify = plot.isNotify();
    plot.setNotify(false);
    zoomInDomain(x, y);
    zoomInRange(x, y);
    plot.setNotify(savedNotify);
  }
  public void zoomInDomain(double x, double y) {
    Plot plot = this.chart.getPlot();
    if(plot instanceof Zoomable) {
      boolean savedNotify = plot.isNotify();
      plot.setNotify(false);
      Zoomable z = (Zoomable)plot;
      z.zoomDomainAxes(this.zoomInFactor, this.info.getPlotInfo(), translateScreenToJava2D(new Point((int)x, (int)y)), this.zoomAroundAnchor);
      plot.setNotify(savedNotify);
    }
  }
  public void zoomInRange(double x, double y) {
    Plot plot = this.chart.getPlot();
    if(plot instanceof Zoomable) {
      boolean savedNotify = plot.isNotify();
      plot.setNotify(false);
      Zoomable z = (Zoomable)plot;
      z.zoomRangeAxes(this.zoomInFactor, this.info.getPlotInfo(), translateScreenToJava2D(new Point((int)x, (int)y)), this.zoomAroundAnchor);
      plot.setNotify(savedNotify);
    }
  }
  public void zoomOutBoth(double x, double y) {
    Plot plot = this.chart.getPlot();
    if(plot == null) {
      return ;
    }
    boolean savedNotify = plot.isNotify();
    plot.setNotify(false);
    zoomOutDomain(x, y);
    zoomOutRange(x, y);
    plot.setNotify(savedNotify);
  }
  public void zoomOutDomain(double x, double y) {
    Plot plot = this.chart.getPlot();
    if(plot instanceof Zoomable) {
      boolean savedNotify = plot.isNotify();
      plot.setNotify(false);
      Zoomable z = (Zoomable)plot;
      z.zoomDomainAxes(this.zoomOutFactor, this.info.getPlotInfo(), translateScreenToJava2D(new Point((int)x, (int)y)), this.zoomAroundAnchor);
      plot.setNotify(savedNotify);
    }
  }
  public void zoomOutRange(double x, double y) {
    Plot plot = this.chart.getPlot();
    if(plot instanceof Zoomable) {
      boolean savedNotify = plot.isNotify();
      plot.setNotify(false);
      Zoomable z = (Zoomable)plot;
      z.zoomRangeAxes(this.zoomOutFactor, this.info.getPlotInfo(), translateScreenToJava2D(new Point((int)x, (int)y)), this.zoomAroundAnchor);
      plot.setNotify(savedNotify);
    }
  }
}