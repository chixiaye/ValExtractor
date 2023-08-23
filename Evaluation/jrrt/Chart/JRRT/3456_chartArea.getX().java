package org.jfree.chart.title;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.util.HorizontalAlignment;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.Size2D;
import org.jfree.chart.util.VerticalAlignment;

public class ImageTitle extends Title  {
  private Image image;
  public ImageTitle(Image image) {
    this(image, image.getHeight(null), image.getWidth(null), Title.DEFAULT_POSITION, Title.DEFAULT_HORIZONTAL_ALIGNMENT, Title.DEFAULT_VERTICAL_ALIGNMENT, Title.DEFAULT_PADDING);
  }
  public ImageTitle(Image image, RectangleEdge position, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
    this(image, image.getHeight(null), image.getWidth(null), position, horizontalAlignment, verticalAlignment, Title.DEFAULT_PADDING);
  }
  public ImageTitle(Image image, int height, int width, RectangleEdge position, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment, RectangleInsets padding) {
    super(position, horizontalAlignment, verticalAlignment, padding);
    if(image == null) {
      throw new NullPointerException("Null \'image\' argument.");
    }
    this.image = image;
    setHeight(height);
    setWidth(width);
  }
  public Image getImage() {
    return this.image;
  }
  public Object draw(Graphics2D g2, Rectangle2D area, Object params) {
    draw(g2, area);
    return null;
  }
  public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
    Size2D s = new Size2D(this.image.getWidth(null), this.image.getHeight(null));
    return new Size2D(calculateTotalWidth(s.getWidth()), calculateTotalHeight(s.getHeight()));
  }
  protected Size2D drawHorizontal(Graphics2D g2, Rectangle2D chartArea) {
    double startY = 0.0D;
    double topSpace = 0.0D;
    double bottomSpace = 0.0D;
    double leftSpace = 0.0D;
    double rightSpace = 0.0D;
    double w = getWidth();
    double h = getHeight();
    RectangleInsets padding = getPadding();
    topSpace = padding.calculateTopOutset(h);
    bottomSpace = padding.calculateBottomOutset(h);
    leftSpace = padding.calculateLeftOutset(w);
    rightSpace = padding.calculateRightOutset(w);
    if(getPosition() == RectangleEdge.TOP) {
      startY = chartArea.getY() + topSpace;
    }
    else {
      startY = chartArea.getY() + chartArea.getHeight() - bottomSpace - h;
    }
    HorizontalAlignment horizontalAlignment = getHorizontalAlignment();
    double startX = 0.0D;
    if(horizontalAlignment == HorizontalAlignment.CENTER) {
      double var_3456 = chartArea.getX();
      startX = var_3456 + leftSpace + chartArea.getWidth() / 2.0D - w / 2.0D;
    }
    else 
      if(horizontalAlignment == HorizontalAlignment.LEFT) {
        startX = chartArea.getX() + leftSpace;
      }
      else 
        if(horizontalAlignment == HorizontalAlignment.RIGHT) {
          startX = chartArea.getX() + chartArea.getWidth() - rightSpace - w;
        }
    g2.drawImage(this.image, (int)startX, (int)startY, (int)w, (int)h, null);
    return new Size2D(chartArea.getWidth() + leftSpace + rightSpace, h + topSpace + bottomSpace);
  }
  protected Size2D drawVertical(Graphics2D g2, Rectangle2D chartArea) {
    double startX = 0.0D;
    double topSpace = 0.0D;
    double bottomSpace = 0.0D;
    double leftSpace = 0.0D;
    double rightSpace = 0.0D;
    double w = getWidth();
    double h = getHeight();
    RectangleInsets padding = getPadding();
    if(padding != null) {
      topSpace = padding.calculateTopOutset(h);
      bottomSpace = padding.calculateBottomOutset(h);
      leftSpace = padding.calculateLeftOutset(w);
      rightSpace = padding.calculateRightOutset(w);
    }
    if(getPosition() == RectangleEdge.LEFT) {
      startX = chartArea.getX() + leftSpace;
    }
    else {
      startX = chartArea.getMaxX() - rightSpace - w;
    }
    VerticalAlignment alignment = getVerticalAlignment();
    double startY = 0.0D;
    if(alignment == VerticalAlignment.CENTER) {
      startY = chartArea.getMinY() + topSpace + chartArea.getHeight() / 2.0D - h / 2.0D;
    }
    else 
      if(alignment == VerticalAlignment.TOP) {
        startY = chartArea.getMinY() + topSpace;
      }
      else 
        if(alignment == VerticalAlignment.BOTTOM) {
          startY = chartArea.getMaxY() - bottomSpace - h;
        }
    g2.drawImage(this.image, (int)startX, (int)startY, (int)w, (int)h, null);
    return new Size2D(chartArea.getWidth() + leftSpace + rightSpace, h + topSpace + bottomSpace);
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof ImageTitle)) {
      return false;
    }
    ImageTitle that = (ImageTitle)obj;
    if(!ObjectUtilities.equal(this.image, that.image)) {
      return false;
    }
    return super.equals(obj);
  }
  public void draw(Graphics2D g2, Rectangle2D area) {
    RectangleEdge position = getPosition();
    if(position == RectangleEdge.TOP || position == RectangleEdge.BOTTOM) {
      drawHorizontal(g2, area);
    }
    else 
      if(position == RectangleEdge.LEFT || position == RectangleEdge.RIGHT) {
        drawVertical(g2, area);
      }
      else {
        throw new RuntimeException("Invalid title position.");
      }
  }
  public void setImage(Image image) {
    if(image == null) {
      throw new NullPointerException("Null \'image\' argument.");
    }
    this.image = image;
    notifyListeners(new TitleChangeEvent(this));
  }
}