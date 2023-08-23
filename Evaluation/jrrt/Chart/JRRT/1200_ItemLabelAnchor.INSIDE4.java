package org.jfree.chart.labels;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class ItemLabelAnchor implements Serializable  {
  final private static long serialVersionUID = -1233101616128695658L;
  final public static ItemLabelAnchor CENTER = new ItemLabelAnchor("ItemLabelAnchor.CENTER");
  final public static ItemLabelAnchor INSIDE1 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE1");
  final public static ItemLabelAnchor INSIDE2 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE2");
  final public static ItemLabelAnchor INSIDE3 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE3");
  final public static ItemLabelAnchor INSIDE4 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE4");
  final public static ItemLabelAnchor INSIDE5 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE5");
  final public static ItemLabelAnchor INSIDE6 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE6");
  final public static ItemLabelAnchor INSIDE7 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE7");
  final public static ItemLabelAnchor INSIDE8 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE8");
  final public static ItemLabelAnchor INSIDE9 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE9");
  final public static ItemLabelAnchor INSIDE10 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE10");
  final public static ItemLabelAnchor INSIDE11 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE11");
  final public static ItemLabelAnchor INSIDE12 = new ItemLabelAnchor("ItemLabelAnchor.INSIDE12");
  final public static ItemLabelAnchor OUTSIDE1 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE1");
  final public static ItemLabelAnchor OUTSIDE2 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE2");
  final public static ItemLabelAnchor OUTSIDE3 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE3");
  final public static ItemLabelAnchor OUTSIDE4 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE4");
  final public static ItemLabelAnchor OUTSIDE5 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE5");
  final public static ItemLabelAnchor OUTSIDE6 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE6");
  final public static ItemLabelAnchor OUTSIDE7 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE7");
  final public static ItemLabelAnchor OUTSIDE8 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE8");
  final public static ItemLabelAnchor OUTSIDE9 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE9");
  final public static ItemLabelAnchor OUTSIDE10 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE10");
  final public static ItemLabelAnchor OUTSIDE11 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE11");
  final public static ItemLabelAnchor OUTSIDE12 = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE12");
  private String name;
  private ItemLabelAnchor(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    ItemLabelAnchor result = null;
    if(this.equals(ItemLabelAnchor.CENTER)) {
      result = ItemLabelAnchor.CENTER;
    }
    else 
      if(this.equals(ItemLabelAnchor.INSIDE1)) {
        result = ItemLabelAnchor.INSIDE1;
      }
      else 
        if(this.equals(ItemLabelAnchor.INSIDE2)) {
          result = ItemLabelAnchor.INSIDE2;
        }
        else 
          if(this.equals(ItemLabelAnchor.INSIDE3)) {
            result = ItemLabelAnchor.INSIDE3;
          }
          else 
            if(this.equals(ItemLabelAnchor.INSIDE4)) {
              result = ItemLabelAnchor.INSIDE4;
            }
            else 
              if(this.equals(ItemLabelAnchor.INSIDE5)) {
                result = ItemLabelAnchor.INSIDE5;
              }
              else {
                ItemLabelAnchor var_1200 = ItemLabelAnchor.INSIDE6;
                if(this.equals(var_1200)) {
                  result = ItemLabelAnchor.INSIDE6;
                }
                else 
                  if(this.equals(ItemLabelAnchor.INSIDE7)) {
                    result = ItemLabelAnchor.INSIDE7;
                  }
                  else 
                    if(this.equals(ItemLabelAnchor.INSIDE8)) {
                      result = ItemLabelAnchor.INSIDE8;
                    }
                    else 
                      if(this.equals(ItemLabelAnchor.INSIDE9)) {
                        result = ItemLabelAnchor.INSIDE9;
                      }
                      else 
                        if(this.equals(ItemLabelAnchor.INSIDE10)) {
                          result = ItemLabelAnchor.INSIDE10;
                        }
                        else 
                          if(this.equals(ItemLabelAnchor.INSIDE11)) {
                            result = ItemLabelAnchor.INSIDE11;
                          }
                          else 
                            if(this.equals(ItemLabelAnchor.INSIDE12)) {
                              result = ItemLabelAnchor.INSIDE12;
                            }
                            else 
                              if(this.equals(ItemLabelAnchor.OUTSIDE1)) {
                                result = ItemLabelAnchor.OUTSIDE1;
                              }
                              else 
                                if(this.equals(ItemLabelAnchor.OUTSIDE2)) {
                                  result = ItemLabelAnchor.OUTSIDE2;
                                }
                                else 
                                  if(this.equals(ItemLabelAnchor.OUTSIDE3)) {
                                    result = ItemLabelAnchor.OUTSIDE3;
                                  }
                                  else 
                                    if(this.equals(ItemLabelAnchor.OUTSIDE4)) {
                                      result = ItemLabelAnchor.OUTSIDE4;
                                    }
                                    else 
                                      if(this.equals(ItemLabelAnchor.OUTSIDE5)) {
                                        result = ItemLabelAnchor.OUTSIDE5;
                                      }
                                      else 
                                        if(this.equals(ItemLabelAnchor.OUTSIDE6)) {
                                          result = ItemLabelAnchor.OUTSIDE6;
                                        }
                                        else 
                                          if(this.equals(ItemLabelAnchor.OUTSIDE7)) {
                                            result = ItemLabelAnchor.OUTSIDE7;
                                          }
                                          else 
                                            if(this.equals(ItemLabelAnchor.OUTSIDE8)) {
                                              result = ItemLabelAnchor.OUTSIDE8;
                                            }
                                            else 
                                              if(this.equals(ItemLabelAnchor.OUTSIDE9)) {
                                                result = ItemLabelAnchor.OUTSIDE9;
                                              }
                                              else 
                                                if(this.equals(ItemLabelAnchor.OUTSIDE10)) {
                                                  result = ItemLabelAnchor.OUTSIDE10;
                                                }
                                                else 
                                                  if(this.equals(ItemLabelAnchor.OUTSIDE11)) {
                                                    result = ItemLabelAnchor.OUTSIDE11;
                                                  }
                                                  else 
                                                    if(this.equals(ItemLabelAnchor.OUTSIDE12)) {
                                                      result = ItemLabelAnchor.OUTSIDE12;
                                                    }
              }
    return result;
  }
  public String toString() {
    return this.name;
  }
  public boolean equals(Object o) {
    if(this == o) {
      return true;
    }
    if(!(o instanceof ItemLabelAnchor)) {
      return false;
    }
    ItemLabelAnchor order = (ItemLabelAnchor)o;
    if(!this.name.equals(order.toString())) {
      return false;
    }
    return true;
  }
}