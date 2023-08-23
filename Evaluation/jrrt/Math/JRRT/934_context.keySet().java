package org.apache.commons.math3.exception.util;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.text.MessageFormat;
import java.util.Locale;

public class ExceptionContext implements Serializable  {
  final private static long serialVersionUID = -6024911025449780478L;
  private Throwable throwable;
  private List<Localizable> msgPatterns;
  private List<Object[]> msgArguments;
  private Map<String, Object> context;
  public ExceptionContext(final Throwable throwable) {
    super();
    this.throwable = throwable;
    msgPatterns = new ArrayList<Localizable>();
    msgArguments = new ArrayList<Object[]>();
    context = new HashMap<String, Object>();
  }
  public Object getValue(String key) {
    return context.get(key);
  }
  public Set<String> getKeys() {
    return context.keySet();
  }
  private String buildMessage(Locale locale, String separator) {
    final StringBuilder sb = new StringBuilder();
    int count = 0;
    final int len = msgPatterns.size();
    for(int i = 0; i < len; i++) {
      final Localizable pat = msgPatterns.get(i);
      final Object[] args = msgArguments.get(i);
      final MessageFormat fmt = new MessageFormat(pat.getLocalizedString(locale), locale);
      sb.append(fmt.format(args));
      if(++count < len) {
        sb.append(separator);
      }
    }
    return sb.toString();
  }
  public String getLocalizedMessage() {
    return getMessage(Locale.getDefault());
  }
  public String getMessage() {
    return getMessage(Locale.US);
  }
  public String getMessage(final Locale locale) {
    return buildMessage(locale, ": ");
  }
  public String getMessage(final Locale locale, final String separator) {
    return buildMessage(locale, separator);
  }
  private String nonSerializableReplacement(Object obj) {
    return "[Object could not be serialized: " + obj.getClass().getName() + "]";
  }
  public Throwable getThrowable() {
    return throwable;
  }
  public void addMessage(Localizable pattern, Object ... arguments) {
    msgPatterns.add(pattern);
    msgArguments.add(ArgUtils.flatten(arguments));
  }
  private void deSerializeContext(ObjectInputStream in) throws IOException, ClassNotFoundException {
    final int len = in.readInt();
    context = new HashMap<String, Object>();
    for(int i = 0; i < len; i++) {
      final String key = (String)in.readObject();
      final Object value = in.readObject();
      context.put(key, value);
    }
  }
  private void deSerializeMessages(ObjectInputStream in) throws IOException, ClassNotFoundException {
    final int len = in.readInt();
    msgPatterns = new ArrayList<Localizable>(len);
    msgArguments = new ArrayList<Object[]>(len);
    for(int i = 0; i < len; i++) {
      final Localizable pat = (Localizable)in.readObject();
      msgPatterns.add(pat);
      final int aLen = in.readInt();
      final Object[] args = new Object[aLen];
      for(int j = 0; j < aLen; j++) {
        args[j] = in.readObject();
      }
      msgArguments.add(args);
    }
  }
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    throwable = (Throwable)in.readObject();
    deSerializeMessages(in);
    deSerializeContext(in);
  }
  private void serializeContext(ObjectOutputStream out) throws IOException {
    Set<String> var_934 = context.keySet();
    final int len = var_934.size();
    out.writeInt(len);
    for (String key : context.keySet()) {
      out.writeObject(key);
      final Object value = context.get(key);
      if(value instanceof Serializable) {
        out.writeObject(value);
      }
      else {
        out.writeObject(nonSerializableReplacement(value));
      }
    }
  }
  private void serializeMessages(ObjectOutputStream out) throws IOException {
    final int len = msgPatterns.size();
    out.writeInt(len);
    for(int i = 0; i < len; i++) {
      final Localizable pat = msgPatterns.get(i);
      out.writeObject(pat);
      final Object[] args = msgArguments.get(i);
      final int aLen = args.length;
      out.writeInt(aLen);
      for(int j = 0; j < aLen; j++) {
        if(args[j] instanceof Serializable) {
          out.writeObject(args[j]);
        }
        else {
          out.writeObject(nonSerializableReplacement(args[j]));
        }
      }
    }
  }
  public void setValue(String key, Object value) {
    context.put(key, value);
  }
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeObject(throwable);
    serializeMessages(out);
    serializeContext(out);
  }
}