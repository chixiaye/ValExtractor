package org.jfree.data.xml;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ValueHandler extends DefaultHandler implements DatasetTags  {
  private RootHandler rootHandler;
  private ItemHandler itemHandler;
  private StringBuffer currentText;
  public ValueHandler(RootHandler rootHandler, ItemHandler itemHandler) {
    super();
    this.rootHandler = rootHandler;
    this.itemHandler = itemHandler;
    this.currentText = new StringBuffer();
  }
  protected String getCurrentText() {
    return this.currentText.toString();
  }
  public void characters(char[] ch, int start, int length) {
    if(this.currentText != null) {
      this.currentText.append(String.copyValueOf(ch, start, length));
    }
  }
  protected void clearCurrentText() {
    StringBuffer var_4365 = this.currentText;
    var_4365.delete(0, this.currentText.length());
  }
  public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    if(qName.equals(VALUE_TAG)) {
      Number value;
      try {
        value = Double.valueOf(this.currentText.toString());
        if(((Double)value).isNaN()) {
          value = null;
        }
      }
      catch (NumberFormatException e1) {
        value = null;
      }
      this.itemHandler.setValue(value);
      this.rootHandler.popSubHandler();
    }
    else {
      throw new SAXException("Expecting </Value> but found " + qName);
    }
  }
  public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
    if(qName.equals(VALUE_TAG)) {
      clearCurrentText();
    }
    else {
      throw new SAXException("Expecting <Value> but found " + qName);
    }
  }
}