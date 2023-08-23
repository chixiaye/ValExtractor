package org.jfree.chart.title;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.swing.event.EventListenerList;
import org.jfree.chart.block.AbstractBlock;
import org.jfree.chart.block.Block;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.event.TitleChangeListener;
import org.jfree.chart.util.HorizontalAlignment;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.VerticalAlignment;

abstract public class Title extends AbstractBlock implements Block, Cloneable, Serializable  {
  final private static long serialVersionUID = -6675162505277817221L;
  final public static RectangleEdge DEFAULT_POSITION = RectangleEdge.TOP;
  final public static HorizontalAlignment DEFAULT_HORIZONTAL_ALIGNMENT = HorizontalAlignment.CENTER;
  final public static VerticalAlignment DEFAULT_VERTICAL_ALIGNMENT = VerticalAlignment.CENTER;
  final public static RectangleInsets DEFAULT_PADDING = new RectangleInsets(1, 1, 1, 1);
  public boolean visible;
  private RectangleEdge position;
  private HorizontalAlignment horizontalAlignment;
  private VerticalAlignment verticalAlignment;
  private transient EventListenerList listenerList;
  private boolean notify;
  protected Title() {
    this(Title.DEFAULT_POSITION, Title.DEFAULT_HORIZONTAL_ALIGNMENT, Title.DEFAULT_VERTICAL_ALIGNMENT, Title.DEFAULT_PADDING);
  }
  protected Title(RectangleEdge position, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
    this(position, horizontalAlignment, verticalAlignment, Title.DEFAULT_PADDING);
  }
  protected Title(RectangleEdge position, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment, RectangleInsets padding) {
    super();
    if(position == null) {
      throw new IllegalArgumentException("Null \'position\' argument.");
    }
    if(horizontalAlignment == null) {
      throw new IllegalArgumentException("Null \'horizontalAlignment\' argument.");
    }
    if(verticalAlignment == null) {
      throw new IllegalArgumentException("Null \'verticalAlignment\' argument.");
    }
    if(padding == null) {
      throw new IllegalArgumentException("Null \'spacer\' argument.");
    }
    this.visible = true;
    this.position = position;
    this.horizontalAlignment = horizontalAlignment;
    this.verticalAlignment = verticalAlignment;
    setPadding(padding);
    this.listenerList = new EventListenerList();
    this.notify = true;
  }
  public HorizontalAlignment getHorizontalAlignment() {
    return this.horizontalAlignment;
  }
  public Object clone() throws CloneNotSupportedException {
    Title duplicate = (Title)super.clone();
    duplicate.listenerList = new EventListenerList();
    return duplicate;
  }
  public RectangleEdge getPosition() {
    return this.position;
  }
  public VerticalAlignment getVerticalAlignment() {
    return this.verticalAlignment;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof Title)) {
      return false;
    }
    Title that = (Title)obj;
    if(this.visible != that.visible) {
      return false;
    }
    if(this.position != that.position) {
      return false;
    }
    if(this.horizontalAlignment != that.horizontalAlignment) {
      return false;
    }
    if(this.verticalAlignment != that.verticalAlignment) {
      return false;
    }
    if(this.notify != that.notify) {
      return false;
    }
    return super.equals(obj);
  }
  public boolean getNotify() {
    return this.notify;
  }
  public boolean isVisible() {
    return this.visible;
  }
  public int hashCode() {
    int result = 193;
    result = 37 * result + ObjectUtilities.hashCode(this.position);
    result = 37 * result + ObjectUtilities.hashCode(this.horizontalAlignment);
    result = 37 * result + ObjectUtilities.hashCode(this.verticalAlignment);
    return result;
  }
  public void addChangeListener(TitleChangeListener listener) {
    this.listenerList.add(TitleChangeListener.class, listener);
  }
  abstract public void draw(Graphics2D g2, Rectangle2D area);
  protected void notifyListeners(TitleChangeEvent event) {
    if(this.notify) {
      Object[] listeners = this.listenerList.getListenerList();
      for(int i = listeners.length - 2; i >= 0; i -= 2) {
        if(listeners[i] == TitleChangeListener.class) {
          ((TitleChangeListener)listeners[i + 1]).titleChanged(event);
        }
      }
    }
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.listenerList = new EventListenerList();
  }
  public void removeChangeListener(TitleChangeListener listener) {
    this.listenerList.remove(TitleChangeListener.class, listener);
  }
  public void setHorizontalAlignment(HorizontalAlignment alignment) {
    if(alignment == null) {
      throw new IllegalArgumentException("Null \'alignment\' argument.");
    }
    if(this.horizontalAlignment != alignment) {
      this.horizontalAlignment = alignment;
      notifyListeners(new TitleChangeEvent(this));
    }
  }
  public void setNotify(boolean flag) {
    this.notify = flag;
    if(flag) {
      notifyListeners(new TitleChangeEvent(this));
    }
  }
  public void setPosition(RectangleEdge position) {
    if(position == null) {
      throw new IllegalArgumentException("Null \'position\' argument.");
    }
    RectangleEdge var_3468 = this.position;
    if(var_3468 != position) {
      this.position = position;
      notifyListeners(new TitleChangeEvent(this));
    }
  }
  public void setVerticalAlignment(VerticalAlignment alignment) {
    if(alignment == null) {
      throw new IllegalArgumentException("Null \'alignment\' argument.");
    }
    if(this.verticalAlignment != alignment) {
      this.verticalAlignment = alignment;
      notifyListeners(new TitleChangeEvent(this));
    }
  }
  public void setVisible(boolean visible) {
    this.visible = visible;
    notifyListeners(new TitleChangeEvent(this));
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
  }
}