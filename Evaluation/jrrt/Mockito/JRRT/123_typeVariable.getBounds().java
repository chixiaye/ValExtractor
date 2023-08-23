package org.mockito.internal.util.reflection;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.util.Checks;
import java.lang.reflect.*;
import java.util.*;

abstract public class GenericMetadataSupport  {
  protected Map<TypeVariable, Type> contextualActualTypeParameters = new HashMap<TypeVariable, Type>();
  private BoundedType boundsOf(TypeVariable typeParameter) {
    if(typeParameter.getBounds()[0] instanceof TypeVariable) {
      return boundsOf((TypeVariable)typeParameter.getBounds()[0]);
    }
    return new TypeVarBoundedType(typeParameter);
  }
  private BoundedType boundsOf(WildcardType wildCard) {
    WildCardBoundedType wildCardBoundedType = new WildCardBoundedType(wildCard);
    if(wildCardBoundedType.firstBound() instanceof TypeVariable) {
      return boundsOf((TypeVariable)wildCardBoundedType.firstBound());
    }
    return wildCardBoundedType;
  }
  abstract public Class<?> rawType();
  public Class<?>[] rawExtraInterfaces() {
    return new Class[0];
  }
  public static GenericMetadataSupport inferFrom(Type type) {
    Checks.checkNotNull(type, "type");
    if(type instanceof Class) {
      return new FromClassGenericMetadataSupport((Class<?>)type);
    }
    if(type instanceof ParameterizedType) {
      return new FromParameterizedTypeGenericMetadataSupport((ParameterizedType)type);
    }
    throw new MockitoException("Type meta-data for this Type (" + type.getClass().getCanonicalName() + ") is not supported : " + type);
  }
  public GenericMetadataSupport resolveGenericReturnType(Method method) {
    Type genericReturnType = method.getGenericReturnType();
    if(genericReturnType instanceof Class) {
      return new NotGenericReturnTypeSupport(genericReturnType);
    }
    if(genericReturnType instanceof ParameterizedType) {
      return new ParameterizedReturnType(this, method.getTypeParameters(), (ParameterizedType)method.getGenericReturnType());
    }
    if(genericReturnType instanceof TypeVariable) {
      return new TypeVariableReturnType(this, method.getTypeParameters(), (TypeVariable)genericReturnType);
    }
    throw new MockitoException("Ouch, it shouldn\'t happen, type \'" + genericReturnType.getClass().getCanonicalName() + "\' on method : \'" + method.toGenericString() + "\' is not supported : " + genericReturnType);
  }
  public List<Type> extraInterfaces() {
    return Collections.emptyList();
  }
  public Map<TypeVariable, Type> actualTypeArguments() {
    TypeVariable[] typeParameters = rawType().getTypeParameters();
    LinkedHashMap<TypeVariable, Type> actualTypeArguments = new LinkedHashMap<TypeVariable, Type>();
    for (TypeVariable typeParameter : typeParameters) {
      Type actualType = getActualTypeArgumentFor(typeParameter);
      actualTypeArguments.put(typeParameter, actualType);
    }
    return actualTypeArguments;
  }
  protected Type getActualTypeArgumentFor(TypeVariable typeParameter) {
    Type type = this.contextualActualTypeParameters.get(typeParameter);
    if(type instanceof TypeVariable) {
      TypeVariable typeVariable = (TypeVariable)type;
      return getActualTypeArgumentFor(typeVariable);
    }
    return type;
  }
  public boolean hasRawExtraInterfaces() {
    return rawExtraInterfaces().length > 0;
  }
  protected void registerTypeParametersOn(TypeVariable[] typeParameters) {
    for (TypeVariable type : typeParameters) {
      registerTypeVariableIfNotPresent(type);
    }
  }
  private void registerTypeVariableIfNotPresent(TypeVariable typeVariable) {
    if(!contextualActualTypeParameters.containsKey(typeVariable)) {
      contextualActualTypeParameters.put(typeVariable, boundsOf(typeVariable));
    }
  }
  protected void registerTypeVariablesOn(Type classType) {
    if(!(classType instanceof ParameterizedType)) {
      return ;
    }
    ParameterizedType parameterizedType = (ParameterizedType)classType;
    TypeVariable[] typeParameters = ((Class<?>)parameterizedType.getRawType()).getTypeParameters();
    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
    for(int i = 0; i < actualTypeArguments.length; i++) {
      TypeVariable typeParameter = typeParameters[i];
      Type actualTypeArgument = actualTypeArguments[i];
      if(actualTypeArgument instanceof WildcardType) {
        contextualActualTypeParameters.put(typeParameter, boundsOf((WildcardType)actualTypeArgument));
      }
      else 
        if(typeParameter != actualTypeArgument) {
          contextualActualTypeParameters.put(typeParameter, actualTypeArgument);
        }
    }
  }
  
  public interface BoundedType extends Type  {
    Type firstBound();
    Type[] interfaceBounds();
  }
  
  private static class FromClassGenericMetadataSupport extends GenericMetadataSupport  {
    final private Class<?> clazz;
    public FromClassGenericMetadataSupport(Class<?> clazz) {
      super();
      this.clazz = clazz;
      for(java.lang.Class currentExploredClass = clazz; currentExploredClass != null && currentExploredClass != Object.class; currentExploredClass = superClassOf(currentExploredClass)) {
        readActualTypeParametersOnDeclaringClass(currentExploredClass);
      }
    }
    private Class superClassOf(Class currentExploredClass) {
      Type genericSuperclass = currentExploredClass.getGenericSuperclass();
      if(genericSuperclass instanceof ParameterizedType) {
        Type rawType = ((ParameterizedType)genericSuperclass).getRawType();
        return (Class)rawType;
      }
      return (Class)genericSuperclass;
    }
    @Override() public Class<?> rawType() {
      return clazz;
    }
    private void readActualTypeParametersOnDeclaringClass(Class<?> clazz) {
      registerTypeParametersOn(clazz.getTypeParameters());
      registerTypeVariablesOn(clazz.getGenericSuperclass());
      for (Type genericInterface : clazz.getGenericInterfaces()) {
        registerTypeVariablesOn(genericInterface);
      }
    }
  }
  
  private static class FromParameterizedTypeGenericMetadataSupport extends GenericMetadataSupport  {
    final private ParameterizedType parameterizedType;
    public FromParameterizedTypeGenericMetadataSupport(ParameterizedType parameterizedType) {
      super();
      this.parameterizedType = parameterizedType;
      readActualTypeParameters();
    }
    @Override() public Class<?> rawType() {
      return (Class<?>)parameterizedType.getRawType();
    }
    private void readActualTypeParameters() {
      registerTypeVariablesOn(parameterizedType.getRawType());
      registerTypeVariablesOn(parameterizedType);
    }
  }
  
  private static class NotGenericReturnTypeSupport extends GenericMetadataSupport  {
    final private Class<?> returnType;
    public NotGenericReturnTypeSupport(Type genericReturnType) {
      super();
      returnType = (Class<?>)genericReturnType;
    }
    @Override() public Class<?> rawType() {
      return returnType;
    }
  }
  
  private static class ParameterizedReturnType extends GenericMetadataSupport  {
    final private ParameterizedType parameterizedType;
    final private TypeVariable[] typeParameters;
    public ParameterizedReturnType(GenericMetadataSupport source, TypeVariable[] typeParameters, ParameterizedType parameterizedType) {
      super();
      this.parameterizedType = parameterizedType;
      this.typeParameters = typeParameters;
      this.contextualActualTypeParameters = source.contextualActualTypeParameters;
      readTypeParameters();
      readTypeVariables();
    }
    @Override() public Class<?> rawType() {
      return (Class<?>)parameterizedType.getRawType();
    }
    private void readTypeParameters() {
      registerTypeParametersOn(typeParameters);
    }
    private void readTypeVariables() {
      registerTypeVariablesOn(parameterizedType);
    }
  }
  
  public static class TypeVarBoundedType implements BoundedType  {
    final private TypeVariable typeVariable;
    public TypeVarBoundedType(TypeVariable typeVariable) {
      super();
      this.typeVariable = typeVariable;
    }
    @Override() public String toString() {
      return "{firstBound=" + firstBound() + ", interfaceBounds=" + Arrays.deepToString(interfaceBounds()) + '}';
    }
    public Type firstBound() {
      return typeVariable.getBounds()[0];
    }
    public TypeVariable typeVariable() {
      return typeVariable;
    }
    public Type[] interfaceBounds() {
      Type[] interfaceBounds = new Type[typeVariable.getBounds().length - 1];
      Type[] var_123 = typeVariable.getBounds();
      System.arraycopy(var_123, 1, interfaceBounds, 0, typeVariable.getBounds().length - 1);
      return interfaceBounds;
    }
    @Override() public boolean equals(Object o) {
      if(this == o) 
        return true;
      if(o == null || getClass() != o.getClass()) 
        return false;
      return typeVariable.equals(((TypeVarBoundedType)o).typeVariable);
    }
    @Override() public int hashCode() {
      return typeVariable.hashCode();
    }
  }
  
  private static class TypeVariableReturnType extends GenericMetadataSupport  {
    final private TypeVariable typeVariable;
    final private TypeVariable[] typeParameters;
    private Class<?> rawType;
    public TypeVariableReturnType(GenericMetadataSupport source, TypeVariable[] typeParameters, TypeVariable typeVariable) {
      super();
      this.typeParameters = typeParameters;
      this.typeVariable = typeVariable;
      this.contextualActualTypeParameters = source.contextualActualTypeParameters;
      readTypeParameters();
      readTypeVariables();
    }
    private Class<?> extractRawTypeOf(Type type) {
      if(type instanceof Class) {
        return (Class<?>)type;
      }
      if(type instanceof ParameterizedType) {
        return (Class<?>)((ParameterizedType)type).getRawType();
      }
      if(type instanceof BoundedType) {
        return extractRawTypeOf(((BoundedType)type).firstBound());
      }
      if(type instanceof TypeVariable) {
        return extractRawTypeOf(contextualActualTypeParameters.get(type));
      }
      throw new MockitoException("Raw extraction not supported for : \'" + type + "\'");
    }
    @Override() public Class<?> rawType() {
      if(rawType == null) {
        rawType = extractRawTypeOf(typeVariable);
      }
      return rawType;
    }
    public Class<?>[] rawExtraInterfaces() {
      List<Type> extraInterfaces = extraInterfaces();
      List<Class<?>> rawExtraInterfaces = new ArrayList<Class<?>>();
      for (Type extraInterface : extraInterfaces) {
        Class<?> rawInterface = extractRawTypeOf(extraInterface);
        if(!rawType().equals(rawInterface)) {
          rawExtraInterfaces.add(rawInterface);
        }
      }
      return rawExtraInterfaces.toArray(new Class[rawExtraInterfaces.size()]);
    }
    @Override() public List<Type> extraInterfaces() {
      Type type = extractActualBoundedTypeOf(typeVariable);
      if(type instanceof BoundedType) {
        return Arrays.asList(((BoundedType)type).interfaceBounds());
      }
      if(type instanceof ParameterizedType) {
        return Collections.singletonList(type);
      }
      if(type instanceof Class) {
        return Collections.emptyList();
      }
      throw new MockitoException("Cannot extract extra-interfaces from \'" + typeVariable + "\' : \'" + type + "\'");
    }
    private Type extractActualBoundedTypeOf(Type type) {
      if(type instanceof TypeVariable) {
        return extractActualBoundedTypeOf(contextualActualTypeParameters.get(type));
      }
      if(type instanceof BoundedType) {
        Type actualFirstBound = extractActualBoundedTypeOf(((BoundedType)type).firstBound());
        if(!(actualFirstBound instanceof BoundedType)) {
          return type;
        }
        return actualFirstBound;
      }
      return type;
    }
    private void readTypeParameters() {
      registerTypeParametersOn(typeParameters);
    }
    private void readTypeVariables() {
      for (Type type : typeVariable.getBounds()) {
        registerTypeVariablesOn(type);
      }
      registerTypeParametersOn(new TypeVariable[]{ typeVariable } );
      registerTypeVariablesOn(getActualTypeArgumentFor(typeVariable));
    }
  }
  
  public static class WildCardBoundedType implements BoundedType  {
    final private WildcardType wildcard;
    public WildCardBoundedType(WildcardType wildcard) {
      super();
      this.wildcard = wildcard;
    }
    @Override() public String toString() {
      return "{firstBound=" + firstBound() + ", interfaceBounds=[]}";
    }
    public Type firstBound() {
      Type[] lowerBounds = wildcard.getLowerBounds();
      Type[] upperBounds = wildcard.getUpperBounds();
      return lowerBounds.length != 0 ? lowerBounds[0] : upperBounds[0];
    }
    public Type[] interfaceBounds() {
      return new Type[0];
    }
    public WildcardType wildCard() {
      return wildcard;
    }
    @Override() public boolean equals(Object o) {
      if(this == o) 
        return true;
      if(o == null || getClass() != o.getClass()) 
        return false;
      return wildcard.equals(((TypeVarBoundedType)o).typeVariable);
    }
    @Override() public int hashCode() {
      return wildcard.hashCode();
    }
  }
}