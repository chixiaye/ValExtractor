package org.mockito.internal.stubbing.defaultanswers;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.mockito.internal.util.MockUtil;
import org.mockito.internal.util.ObjectMethodsGuru;
import org.mockito.internal.util.Primitives;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.mock.MockName;
import org.mockito.stubbing.Answer;

public class ReturnsEmptyValues implements Answer<Object>, Serializable  {
  final private static long serialVersionUID = 1998191268711234347L;
  ObjectMethodsGuru methodsGuru = new ObjectMethodsGuru();
  MockUtil mockUtil = new MockUtil();
  public Object answer(InvocationOnMock invocation) {
    if(methodsGuru.isToString(invocation.getMethod())) {
      Object var_94 = invocation.getMock();
      Object mock = var_94;
      MockName name = mockUtil.getMockName(mock);
      if(name.isDefault()) {
        return "Mock for " + mockUtil.getMockSettings(mock).getTypeToMock().getSimpleName() + ", hashCode: " + mock.hashCode();
      }
      else {
        return name.toString();
      }
    }
    else 
      if(methodsGuru.isCompareToMethod(invocation.getMethod())) {
        return invocation.getMock() == invocation.getArguments()[0] ? 0 : 1;
      }
    Class<?> returnType = invocation.getMethod().getReturnType();
    return returnValueFor(returnType);
  }
  Object returnValueFor(Class<?> type) {
    if(Primitives.isPrimitiveOrWrapper(type)) {
      return Primitives.defaultValueForPrimitiveOrWrapper(type);
    }
    else 
      if(type == Iterable.class) {
        return new ArrayList<Object>(0);
      }
      else 
        if(type == Collection.class) {
          return new LinkedList<Object>();
        }
        else 
          if(type == Set.class) {
            return new HashSet<Object>();
          }
          else 
            if(type == HashSet.class) {
              return new HashSet<Object>();
            }
            else 
              if(type == SortedSet.class) {
                return new TreeSet<Object>();
              }
              else 
                if(type == TreeSet.class) {
                  return new TreeSet<Object>();
                }
                else 
                  if(type == LinkedHashSet.class) {
                    return new LinkedHashSet<Object>();
                  }
                  else 
                    if(type == List.class) {
                      return new LinkedList<Object>();
                    }
                    else 
                      if(type == LinkedList.class) {
                        return new LinkedList<Object>();
                      }
                      else 
                        if(type == ArrayList.class) {
                          return new ArrayList<Object>();
                        }
                        else 
                          if(type == Map.class) {
                            return new HashMap<Object, Object>();
                          }
                          else 
                            if(type == HashMap.class) {
                              return new HashMap<Object, Object>();
                            }
                            else 
                              if(type == SortedMap.class) {
                                return new TreeMap<Object, Object>();
                              }
                              else 
                                if(type == TreeMap.class) {
                                  return new TreeMap<Object, Object>();
                                }
                                else 
                                  if(type == LinkedHashMap.class) {
                                    return new LinkedHashMap<Object, Object>();
                                  }
    return null;
  }
}