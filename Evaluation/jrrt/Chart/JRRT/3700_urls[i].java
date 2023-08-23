package org.jfree.chart.util;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceBundleWrapper  {
  private static URLClassLoader noCodeBaseClassLoader;
  private ResourceBundleWrapper() {
    super();
  }
  final public static ResourceBundle getBundle(String baseName) {
    if(noCodeBaseClassLoader != null) {
      return ResourceBundle.getBundle(baseName, Locale.getDefault(), noCodeBaseClassLoader);
    }
    else {
      return ResourceBundle.getBundle(baseName);
    }
  }
  final public static ResourceBundle getBundle(String baseName, Locale locale) {
    if(noCodeBaseClassLoader != null) {
      return ResourceBundle.getBundle(baseName, locale, noCodeBaseClassLoader);
    }
    else {
      return ResourceBundle.getBundle(baseName, locale);
    }
  }
  public static ResourceBundle getBundle(String baseName, Locale locale, ClassLoader loader) {
    return ResourceBundle.getBundle(baseName, locale, loader);
  }
  public static void removeCodeBase(URL codeBase, URLClassLoader urlClassLoader) {
    List urlsNoBase = new ArrayList();
    URL[] urls = urlClassLoader.getURLs();
    for(int i = 0; i < urls.length; i++) {
      URL var_3700 = urls[i];
      if(!var_3700.sameFile(codeBase)) {
        urlsNoBase.add(urls[i]);
      }
    }
    URL[] urlsNoBaseArray = (URL[])urlsNoBase.toArray(new URL[0]);
    noCodeBaseClassLoader = URLClassLoader.newInstance(urlsNoBaseArray);
  }
}