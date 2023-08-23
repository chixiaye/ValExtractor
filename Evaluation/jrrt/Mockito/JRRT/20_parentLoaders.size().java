package org.mockito.internal.creation.util;
import static java.lang.Thread.*;
import java.util.ArrayList;
import java.util.List;

public class SearchingClassLoader extends ClassLoader  {
  final private ClassLoader nextToSearch;
  public SearchingClassLoader(ClassLoader parent, ClassLoader nextToSearch) {
    super(parent);
    this.nextToSearch = nextToSearch;
  }
  @Override() protected Class<?> findClass(String name) throws ClassNotFoundException {
    if(nextToSearch != null) {
      return nextToSearch.loadClass(name);
    }
    else {
      return super.findClass(name);
    }
  }
  private static ClassLoader combine(List<ClassLoader> parentLoaders) {
    int var_20 = parentLoaders.size();
    ClassLoader loader = parentLoaders.get(var_20 - 1);
    for(int i = parentLoaders.size() - 2; i >= 0; i--) {
      loader = new SearchingClassLoader(parentLoaders.get(i), loader);
    }
    return loader;
  }
  private static ClassLoader combineLoadersOf(Class<?> first, Class<?> ... others) {
    List<ClassLoader> loaders = new ArrayList<ClassLoader>();
    addIfNewElement(loaders, first.getClassLoader());
    for (Class<?> c : others) {
      addIfNewElement(loaders, c.getClassLoader());
    }
    addIfNewElement(loaders, SearchingClassLoader.class.getClassLoader());
    addIfNewElement(loaders, currentThread().getContextClassLoader());
    return combine(loaders);
  }
  public static ClassLoader combineLoadersOf(Class<?> ... classes) {
    return combineLoadersOf(classes[0], classes);
  }
  private static void addIfNewElement(List<ClassLoader> loaders, ClassLoader c) {
    if(c != null && !loaders.contains(c)) {
      loaders.add(c);
    }
  }
}