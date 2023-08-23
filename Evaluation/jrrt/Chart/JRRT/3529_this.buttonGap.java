package org.jfree.chart.ui;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.io.Serializable;

public class LCBLayout implements LayoutManager, Serializable  {
  final private static long serialVersionUID = -2531780832406163833L;
  final private static int COLUMNS = 3;
  private int[] colWidth;
  private int[] rowHeight;
  private int labelGap;
  private int buttonGap;
  private int vGap;
  public LCBLayout(int maxrows) {
    super();
    this.labelGap = 10;
    this.buttonGap = 6;
    this.vGap = 2;
    this.colWidth = new int[COLUMNS];
    this.rowHeight = new int[maxrows];
  }
  public Dimension minimumLayoutSize(Container parent) {
    synchronized(parent.getTreeLock()) {
      Insets insets = parent.getInsets();
      int ncomponents = parent.getComponentCount();
      int nrows = ncomponents / COLUMNS;
      for(int c = 0; c < COLUMNS; c++) {
        for(int r = 0; r < nrows; r++) {
          Component component = parent.getComponent(r * COLUMNS + c);
          Dimension d = component.getMinimumSize();
          if(this.colWidth[c] < d.width) {
            this.colWidth[c] = d.width;
          }
          if(this.rowHeight[r] < d.height) {
            this.rowHeight[r] = d.height;
          }
        }
      }
      int totalHeight = this.vGap * (nrows - 1);
      for(int r = 0; r < nrows; r++) {
        totalHeight = totalHeight + this.rowHeight[r];
      }
      int totalWidth = this.colWidth[0] + this.labelGap + this.colWidth[1] + this.buttonGap + this.colWidth[2];
      return new Dimension(insets.left + insets.right + totalWidth + this.labelGap + this.buttonGap, insets.top + insets.bottom + totalHeight + this.vGap);
    }
  }
  public Dimension preferredLayoutSize(Container parent) {
    synchronized(parent.getTreeLock()) {
      Insets insets = parent.getInsets();
      int ncomponents = parent.getComponentCount();
      int nrows = ncomponents / COLUMNS;
      for(int c = 0; c < COLUMNS; c++) {
        for(int r = 0; r < nrows; r++) {
          Component component = parent.getComponent(r * COLUMNS + c);
          Dimension d = component.getPreferredSize();
          if(this.colWidth[c] < d.width) {
            this.colWidth[c] = d.width;
          }
          if(this.rowHeight[r] < d.height) {
            this.rowHeight[r] = d.height;
          }
        }
      }
      int totalHeight = this.vGap * (nrows - 1);
      for(int r = 0; r < nrows; r++) {
        totalHeight = totalHeight + this.rowHeight[r];
      }
      int var_3529 = this.buttonGap;
      int totalWidth = this.colWidth[0] + this.labelGap + this.colWidth[1] + var_3529 + this.colWidth[2];
      return new Dimension(insets.left + insets.right + totalWidth + this.labelGap + this.buttonGap, insets.top + insets.bottom + totalHeight + this.vGap);
    }
  }
  public void addLayoutComponent(Component comp) {
  }
  public void addLayoutComponent(String name, Component comp) {
  }
  public void layoutContainer(Container parent) {
    synchronized(parent.getTreeLock()) {
      Insets insets = parent.getInsets();
      int ncomponents = parent.getComponentCount();
      int nrows = ncomponents / COLUMNS;
      for(int c = 0; c < COLUMNS; c++) {
        for(int r = 0; r < nrows; r++) {
          Component component = parent.getComponent(r * COLUMNS + c);
          Dimension d = component.getPreferredSize();
          if(this.colWidth[c] < d.width) {
            this.colWidth[c] = d.width;
          }
          if(this.rowHeight[r] < d.height) {
            this.rowHeight[r] = d.height;
          }
        }
      }
      int totalHeight = this.vGap * (nrows - 1);
      for(int r = 0; r < nrows; r++) {
        totalHeight = totalHeight + this.rowHeight[r];
      }
      int totalWidth = this.colWidth[0] + this.colWidth[1] + this.colWidth[2];
      int available = parent.getWidth() - insets.left - insets.right - this.labelGap - this.buttonGap;
      this.colWidth[1] = this.colWidth[1] + (available - totalWidth);
      int x = insets.left;
      for(int c = 0; c < COLUMNS; c++) {
        int y = insets.top;
        for(int r = 0; r < nrows; r++) {
          int i = r * COLUMNS + c;
          if(i < ncomponents) {
            Component component = parent.getComponent(i);
            Dimension d = component.getPreferredSize();
            int h = d.height;
            int adjust = (this.rowHeight[r] - h) / 2;
            parent.getComponent(i).setBounds(x, y + adjust, this.colWidth[c], h);
          }
          y = y + this.rowHeight[r] + this.vGap;
        }
        x = x + this.colWidth[c];
        if(c == 0) {
          x = x + this.labelGap;
        }
        if(c == 1) {
          x = x + this.buttonGap;
        }
      }
    }
  }
  public void removeLayoutComponent(Component comp) {
  }
  public void removeLayoutComponent(String name, Component comp) {
  }
}