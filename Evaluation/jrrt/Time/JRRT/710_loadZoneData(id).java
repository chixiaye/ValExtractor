package org.joda.time.tz;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.joda.time.DateTimeZone;

public class ZoneInfoProvider implements Provider  {
  final private File iFileDir;
  final private String iResourcePath;
  final private ClassLoader iLoader;
  final private Map<String, Object> iZoneInfoMap;
  public ZoneInfoProvider(File fileDir) throws IOException {
    super();
    if(fileDir == null) {
      throw new IllegalArgumentException("No file directory provided");
    }
    if(!fileDir.exists()) {
      throw new IOException("File directory doesn\'t exist: " + fileDir);
    }
    if(!fileDir.isDirectory()) {
      throw new IOException("File doesn\'t refer to a directory: " + fileDir);
    }
    iFileDir = fileDir;
    iResourcePath = null;
    iLoader = null;
    iZoneInfoMap = loadZoneInfoMap(openResource("ZoneInfoMap"));
  }
  public ZoneInfoProvider(String resourcePath) throws IOException {
    this(resourcePath, null, false);
  }
  public ZoneInfoProvider(String resourcePath, ClassLoader loader) throws IOException {
    this(resourcePath, loader, true);
  }
  private ZoneInfoProvider(String resourcePath, ClassLoader loader, boolean favorSystemLoader) throws IOException {
    super();
    if(resourcePath == null) {
      throw new IllegalArgumentException("No resource path provided");
    }
    if(!resourcePath.endsWith("/")) {
      resourcePath += '/';
    }
    iFileDir = null;
    iResourcePath = resourcePath;
    if(loader == null && !favorSystemLoader) {
      loader = getClass().getClassLoader();
    }
    iLoader = loader;
    iZoneInfoMap = loadZoneInfoMap(openResource("ZoneInfoMap"));
  }
  public DateTimeZone getZone(String id) {
    if(id == null) {
      return null;
    }
    Object obj = iZoneInfoMap.get(id);
    if(obj == null) {
      return null;
    }
    if(id.equals(obj)) {
      DateTimeZone var_710 = loadZoneData(id);
      return var_710;
    }
    if(obj instanceof SoftReference<?>) {
      @SuppressWarnings(value = {"unchecked", }) SoftReference<DateTimeZone> ref = (SoftReference<DateTimeZone>)obj;
      DateTimeZone tz = ref.get();
      if(tz != null) {
        return tz;
      }
      return loadZoneData(id);
    }
    return getZone((String)obj);
  }
  private DateTimeZone loadZoneData(String id) {
    InputStream in = null;
    try {
      in = openResource(id);
      DateTimeZone tz = DateTimeZoneBuilder.readFrom(in, id);
      iZoneInfoMap.put(id, new SoftReference<DateTimeZone>(tz));
      return tz;
    }
    catch (IOException ex) {
      uncaughtException(ex);
      iZoneInfoMap.remove(id);
      return null;
    }
    finally {
      try {
        if(in != null) {
          in.close();
        }
      }
      catch (IOException ex) {
      }
    }
  }
  @SuppressWarnings(value = {"resource", }) private InputStream openResource(String name) throws IOException {
    InputStream in;
    if(iFileDir != null) {
      in = new FileInputStream(new File(iFileDir, name));
    }
    else {
      String path = iResourcePath.concat(name);
      if(iLoader != null) {
        in = iLoader.getResourceAsStream(path);
      }
      else {
        in = ClassLoader.getSystemResourceAsStream(path);
      }
      if(in == null) {
        StringBuilder buf = new StringBuilder(40).append("Resource not found: \"").append(path).append("\" ClassLoader: ").append(iLoader != null ? iLoader.toString() : "system");
        throw new IOException(buf.toString());
      }
    }
    return in;
  }
  private static Map<String, Object> loadZoneInfoMap(InputStream in) throws IOException {
    Map<String, Object> map = new ConcurrentHashMap<String, Object>();
    DataInputStream din = new DataInputStream(in);
    try {
      readZoneInfoMap(din, map);
    }
    finally {
      try {
        din.close();
      }
      catch (IOException ex) {
      }
    }
    map.put("UTC", new SoftReference<DateTimeZone>(DateTimeZone.UTC));
    return map;
  }
  public Set<String> getAvailableIDs() {
    return new TreeSet<String>(iZoneInfoMap.keySet());
  }
  private static void readZoneInfoMap(DataInputStream din, Map<String, Object> zimap) throws IOException {
    int size = din.readUnsignedShort();
    String[] pool = new String[size];
    for(int i = 0; i < size; i++) {
      pool[i] = din.readUTF().intern();
    }
    size = din.readUnsignedShort();
    for(int i = 0; i < size; i++) {
      try {
        zimap.put(pool[din.readUnsignedShort()], pool[din.readUnsignedShort()]);
      }
      catch (ArrayIndexOutOfBoundsException ex) {
        throw new IOException("Corrupt zone info map");
      }
    }
  }
  protected void uncaughtException(Exception ex) {
    ex.printStackTrace();
  }
}