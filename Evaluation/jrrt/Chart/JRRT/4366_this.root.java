package org.jfree.data.xml;
import java.util.Iterator;
import org.jfree.data.DefaultKeyedValues;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CategorySeriesHandler extends DefaultHandler implements DatasetTags  {
  private RootHandler root;
  private Comparable seriesKey;
  private DefaultKeyedValues values;
  public CategorySeriesHandler(RootHandler root) {
    super();
    this.root = root;
    this.values = new DefaultKeyedValues();
  }
  public void addItem(Comparable key, final Number value) {
    this.values.addValue(key, value);
  }
  public void endElement(String namespaceURI, String localName, String qName) {
    if(this.root instanceof CategoryDatasetHandler) {
      CategoryDatasetHandler handler = (CategoryDatasetHandler)this.root;
      Iterator iterator = this.values.getKeys().iterator();
      while(iterator.hasNext()){
        Comparable key = (Comparable)iterator.next();
        Number value = this.values.getValue(key);
        handler.addItem(this.seriesKey, key, value);
      }
      this.root.popSubHandler();
    }
  }
  public void setSeriesKey(Comparable key) {
    this.seriesKey = key;
  }
  public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
    if(qName.equals(SERIES_TAG)) {
      setSeriesKey(atts.getValue("name"));
      RootHandler var_4366 = this.root;
      ItemHandler subhandler = new ItemHandler(var_4366, this);
      this.root.pushSubHandler(subhandler);
    }
    else 
      if(qName.equals(ITEM_TAG)) {
        ItemHandler subhandler = new ItemHandler(this.root, this);
        this.root.pushSubHandler(subhandler);
        subhandler.startElement(namespaceURI, localName, qName, atts);
      }
      else {
        throw new SAXException("Expecting <Series> or <Item> tag...found " + qName);
      }
  }
}