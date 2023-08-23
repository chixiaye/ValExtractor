package org.jfree.chart.text;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class TextBlockAnchor implements Serializable  {
  final private static long serialVersionUID = -3045058380983401544L;
  final public static TextBlockAnchor TOP_LEFT = new TextBlockAnchor("TextBlockAnchor.TOP_LEFT");
  final public static TextBlockAnchor TOP_CENTER = new TextBlockAnchor("TextBlockAnchor.TOP_CENTER");
  final public static TextBlockAnchor TOP_RIGHT = new TextBlockAnchor("TextBlockAnchor.TOP_RIGHT");
  final public static TextBlockAnchor CENTER_LEFT = new TextBlockAnchor("TextBlockAnchor.CENTER_LEFT");
  final public static TextBlockAnchor CENTER = new TextBlockAnchor("TextBlockAnchor.CENTER");
  final public static TextBlockAnchor CENTER_RIGHT = new TextBlockAnchor("TextBlockAnchor.CENTER_RIGHT");
  final public static TextBlockAnchor BOTTOM_LEFT = new TextBlockAnchor("TextBlockAnchor.BOTTOM_LEFT");
  final public static TextBlockAnchor BOTTOM_CENTER = new TextBlockAnchor("TextBlockAnchor.BOTTOM_CENTER");
  final public static TextBlockAnchor BOTTOM_RIGHT = new TextBlockAnchor("TextBlockAnchor.BOTTOM_RIGHT");
  private String name;
  private TextBlockAnchor(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    if(this.equals(TextBlockAnchor.TOP_CENTER)) {
      return TextBlockAnchor.TOP_CENTER;
    }
    else 
      if(this.equals(TextBlockAnchor.TOP_LEFT)) {
        return TextBlockAnchor.TOP_LEFT;
      }
      else {
        TextBlockAnchor var_3281 = TextBlockAnchor.TOP_RIGHT;
        if(this.equals(var_3281)) {
          return TextBlockAnchor.TOP_RIGHT;
        }
        else 
          if(this.equals(TextBlockAnchor.CENTER)) {
            return TextBlockAnchor.CENTER;
          }
          else 
            if(this.equals(TextBlockAnchor.CENTER_LEFT)) {
              return TextBlockAnchor.CENTER_LEFT;
            }
            else 
              if(this.equals(TextBlockAnchor.CENTER_RIGHT)) {
                return TextBlockAnchor.CENTER_RIGHT;
              }
              else 
                if(this.equals(TextBlockAnchor.BOTTOM_CENTER)) {
                  return TextBlockAnchor.BOTTOM_CENTER;
                }
                else 
                  if(this.equals(TextBlockAnchor.BOTTOM_LEFT)) {
                    return TextBlockAnchor.BOTTOM_LEFT;
                  }
                  else 
                    if(this.equals(TextBlockAnchor.BOTTOM_RIGHT)) {
                      return TextBlockAnchor.BOTTOM_RIGHT;
                    }
      }
    return null;
  }
  public String toString() {
    return this.name;
  }
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(!(obj instanceof TextBlockAnchor)) {
      return false;
    }
    TextBlockAnchor other = (TextBlockAnchor)obj;
    if(!this.name.equals(other.name)) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}