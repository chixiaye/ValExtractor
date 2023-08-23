package org.jfree.data.xml;
import java.util.Stack;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RootHandler extends DefaultHandler implements DatasetTags  {
  private Stack subHandlers;
  public RootHandler() {
    super();
    this.subHandlers = new Stack();
  }
  public DefaultHandler getCurrentHandler() {
    DefaultHandler result = this;
    if(this.subHandlers != null) {
      Stack var_4373 = this.subHandlers;
      if(var_4373.size() > 0) {
        Object top = this.subHandlers.peek();
        if(top != null) {
          result = (DefaultHandler)top;
        }
      }
    }
    return result;
  }
  public DefaultHandler popSubHandler() {
    return (DefaultHandler)this.subHandlers.pop();
  }
  public Stack getSubHandlers() {
    return this.subHandlers;
  }
  public void characters(char[] ch, int start, int length) throws SAXException {
    DefaultHandler handler = getCurrentHandler();
    if(handler != this) {
      handler.characters(ch, start, length);
    }
  }
  public void pushSubHandler(DefaultHandler subhandler) {
    this.subHandlers.push(subhandler);
  }
}