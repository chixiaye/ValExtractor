package com.google.javascript.rhino;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JSDocInfo implements Serializable  {
  final private static long serialVersionUID = 1L;
  private LazilyInitializedInfo info = null;
  private LazilyInitializedDocumentation documentation = null;
  private Node associatedNode = null;
  private Visibility visibility = null;
  private int bitset = 0x00;
  private JSTypeExpression type = null;
  private JSTypeExpression thisType = null;
  private boolean includeDocumentation = false;
  final private static int MASK_FLAGS = 0x3FFFFFFF;
  final private static int MASK_CONSTANT = 0x00000001;
  final private static int MASK_CONSTRUCTOR = 0x00000002;
  final private static int MASK_DEFINE = 0x00000004;
  final private static int MASK_HIDDEN = 0x00000008;
  final private static int MASK_PRESERVETRY = 0x00000010;
  final private static int MASK_NOCHECK = 0x00000020;
  final private static int MASK_OVERRIDE = 0x00000040;
  final private static int MASK_NOALIAS = 0x00000080;
  final private static int MASK_DEPRECATED = 0x00000100;
  final private static int MASK_INTERFACE = 0x00000200;
  final private static int MASK_EXPORT = 0x00000400;
  final private static int MASK_NOSHADOW = 0x00000800;
  final private static int MASK_FILEOVERVIEW = 0x00001000;
  final private static int MASK_IMPLICITCAST = 0x00002000;
  final private static int MASK_NOSIDEEFFECTS = 0x00004000;
  final private static int MASK_EXTERNS = 0x00008000;
  final private static int MASK_JAVADISPATCH = 0x00010000;
  final private static int MASK_NOCOMPILE = 0x00020000;
  final private static int MASK_CONSISTIDGEN = 0x00040000;
  final private static int MASK_IDGEN = 0x00080000;
  final private static int MASK_EXPOSE = 0x00100000;
  final private static int MASK_STRUCT = 0x00200000;
  final private static int MASK_DICT = 0x00400000;
  final private static int MASK_STALBEIDGEN = 0x00800000;
  final private static int MASK_TYPEFIELD = 0xE0000000;
  final private static int TYPEFIELD_TYPE = 0x20000000;
  final private static int TYPEFIELD_RETURN = 0x40000000;
  final private static int TYPEFIELD_ENUM = 0x60000000;
  final private static int TYPEFIELD_TYPEDEF = 0x80000000;
  public JSDocInfo() {
    super();
  }
  JSDocInfo(boolean includeDocumentation) {
    super();
    this.includeDocumentation = includeDocumentation;
  }
  public Collection<Marker> getMarkers() {
    return (documentation == null || documentation.markers == null) ? ImmutableList.<Marker>of() : documentation.markers;
  }
  public Collection<Node> getTypeNodes() {
    List<Node> nodes = Lists.newArrayList();
    if(type != null) {
      nodes.add(type.getRoot());
    }
    if(thisType != null) {
      nodes.add(thisType.getRoot());
    }
    if(info != null) {
      if(info.baseType != null) {
        nodes.add(info.baseType.getRoot());
      }
      if(info.extendedInterfaces != null) {
        for (JSTypeExpression interfaceType : info.extendedInterfaces) {
          nodes.add(interfaceType.getRoot());
        }
      }
      if(info.implementedInterfaces != null) {
        for (JSTypeExpression interfaceType : info.implementedInterfaces) {
          nodes.add(interfaceType.getRoot());
        }
      }
      if(info.parameters != null) {
        for (JSTypeExpression parameterType : info.parameters.values()) {
          if(parameterType != null) {
            nodes.add(parameterType.getRoot());
          }
        }
      }
      List<JSTypeExpression> var_2526 = info.thrownTypes;
      if(var_2526 != null) {
        for (JSTypeExpression thrownType : info.thrownTypes) {
          if(thrownType != null) {
            nodes.add(thrownType.getRoot());
          }
        }
      }
    }
    return nodes;
  }
  public Collection<String> getAuthors() {
    return documentation == null ? null : documentation.authors;
  }
  public Collection<String> getReferences() {
    return documentation == null ? null : documentation.sees;
  }
  public ImmutableList<String> getTemplateTypeNames() {
    if(info == null || info.templateTypeNames == null) {
      return ImmutableList.of();
    }
    return info.templateTypeNames;
  }
  public JSTypeExpression getBaseType() {
    return (info == null) ? null : info.baseType;
  }
  public JSTypeExpression getEnumParameterType() {
    return getType(TYPEFIELD_ENUM);
  }
  public JSTypeExpression getParameterType(String parameter) {
    if(info == null || info.parameters == null) {
      return null;
    }
    return info.parameters.get(parameter);
  }
  public JSTypeExpression getReturnType() {
    return getType(TYPEFIELD_RETURN);
  }
  public JSTypeExpression getThisType() {
    return thisType;
  }
  public JSTypeExpression getType() {
    return getType(TYPEFIELD_TYPE);
  }
  private JSTypeExpression getType(int typefield) {
    if((MASK_TYPEFIELD & bitset) == typefield) {
      return type;
    }
    else {
      return null;
    }
  }
  public JSTypeExpression getTypedefType() {
    return getType(TYPEFIELD_TYPEDEF);
  }
  public List<JSTypeExpression> getExtendedInterfaces() {
    if(info == null || info.extendedInterfaces == null) {
      return ImmutableList.of();
    }
    return Collections.unmodifiableList(info.extendedInterfaces);
  }
  public List<JSTypeExpression> getImplementedInterfaces() {
    if(info == null || info.implementedInterfaces == null) {
      return ImmutableList.of();
    }
    return Collections.unmodifiableList(info.implementedInterfaces);
  }
  public List<JSTypeExpression> getThrownTypes() {
    if(info == null || info.thrownTypes == null) {
      return ImmutableList.of();
    }
    return Collections.unmodifiableList(info.thrownTypes);
  }
  Marker addMarker() {
    if(!lazyInitDocumentation()) {
      return null;
    }
    if(documentation.markers == null) {
      documentation.markers = Lists.newArrayList();
    }
    Marker marker = new Marker();
    documentation.markers.add(marker);
    return marker;
  }
  public Node getAssociatedNode() {
    return this.associatedNode;
  }
  public Set<String> getModifies() {
    Set<String> modifies = info == null ? null : info.modifies;
    return modifies == null ? Collections.<String>emptySet() : modifies;
  }
  public Set<String> getParameterNames() {
    if(info == null || info.parameters == null) {
      return ImmutableSet.of();
    }
    return ImmutableSet.copyOf(info.parameters.keySet());
  }
  public Set<String> getSuppressions() {
    Set<String> suppressions = info == null ? null : info.suppressions;
    return suppressions == null ? Collections.<String>emptySet() : suppressions;
  }
  public String getBlockDescription() {
    return documentation == null ? null : documentation.blockDescription;
  }
  public String getDeprecationReason() {
    return info == null ? null : info.deprecated;
  }
  public String getDescription() {
    return (info == null) ? null : info.description;
  }
  public String getDescriptionForParameter(String name) {
    if(documentation == null || documentation.parameters == null) {
      return null;
    }
    return documentation.parameters.get(name);
  }
  public String getFileOverview() {
    return documentation == null ? null : documentation.fileOverview;
  }
  public String getLendsName() {
    return (info == null) ? null : info.lendsName;
  }
  public String getLicense() {
    return (info == null) ? null : info.license;
  }
  public String getMeaning() {
    return (info == null) ? null : info.meaning;
  }
  public String getOriginalCommentString() {
    return documentation == null ? null : documentation.sourceComment;
  }
  public String getReturnDescription() {
    return documentation == null ? null : documentation.returnDescription;
  }
  public String getSourceName() {
    return this.associatedNode != null ? this.associatedNode.getSourceFileName() : null;
  }
  public String getVersion() {
    return documentation == null ? null : documentation.version;
  }
  @Override() public String toString() {
    return "JSDocInfo";
  }
  public Visibility getVisibility() {
    return visibility;
  }
  boolean addExtendedInterface(JSTypeExpression type) {
    lazyInitInfo();
    if(info.extendedInterfaces == null) {
      info.extendedInterfaces = Lists.newArrayListWithCapacity(2);
    }
    if(info.extendedInterfaces.contains(type)) {
      return false;
    }
    info.extendedInterfaces.add(type);
    return true;
  }
  boolean addImplementedInterface(JSTypeExpression interfaceName) {
    lazyInitInfo();
    if(info.implementedInterfaces == null) {
      info.implementedInterfaces = Lists.newArrayListWithCapacity(2);
    }
    if(info.implementedInterfaces.contains(interfaceName)) {
      return false;
    }
    info.implementedInterfaces.add(interfaceName);
    return true;
  }
  public boolean containsDeclaration() {
    return (hasType() || hasReturnType() || hasEnumParameterType() || hasTypedefType() || hasThisType() || getParameterCount() > 0 || getFlag(MASK_CONSTANT | MASK_CONSTRUCTOR | MASK_DEFINE | MASK_OVERRIDE | MASK_NOALIAS | MASK_DEPRECATED | MASK_INTERFACE | MASK_NOSHADOW | MASK_IMPLICITCAST | MASK_NOSIDEEFFECTS));
  }
  boolean declareParam(JSTypeExpression jsType, String parameter) {
    lazyInitInfo();
    if(info.parameters == null) {
      info.parameters = new LinkedHashMap<String, JSTypeExpression>();
    }
    if(!info.parameters.containsKey(parameter)) {
      info.parameters.put(parameter, jsType);
      return true;
    }
    else {
      return false;
    }
  }
  boolean declareTemplateTypeNames(List<String> templateTypeNames) {
    lazyInitInfo();
    if(info.templateTypeNames != null) {
      return false;
    }
    info.templateTypeNames = ImmutableList.copyOf(templateTypeNames);
    return true;
  }
  boolean declareThrows(JSTypeExpression jsType) {
    lazyInitInfo();
    if(info.thrownTypes == null) {
      info.thrownTypes = Lists.newArrayList();
    }
    info.thrownTypes.add(jsType);
    return true;
  }
  boolean documentAuthor(String author) {
    if(!lazyInitDocumentation()) {
      return true;
    }
    if(documentation.authors == null) {
      documentation.authors = Lists.newArrayList();
    }
    documentation.authors.add(author);
    return true;
  }
  boolean documentBlock(String description) {
    if(!lazyInitDocumentation()) {
      return true;
    }
    if(documentation.blockDescription != null) {
      return false;
    }
    documentation.blockDescription = description;
    return true;
  }
  boolean documentFileOverview(String description) {
    setFlag(true, MASK_FILEOVERVIEW);
    if(!lazyInitDocumentation()) {
      return true;
    }
    if(documentation.fileOverview != null) {
      return false;
    }
    documentation.fileOverview = description;
    return true;
  }
  boolean documentParam(String parameter, String description) {
    if(!lazyInitDocumentation()) {
      return true;
    }
    if(documentation.parameters == null) {
      documentation.parameters = new LinkedHashMap<String, String>();
    }
    if(!documentation.parameters.containsKey(parameter)) {
      documentation.parameters.put(parameter, description);
      return true;
    }
    else {
      return false;
    }
  }
  boolean documentReference(String reference) {
    if(!lazyInitDocumentation()) {
      return true;
    }
    if(documentation.sees == null) {
      documentation.sees = Lists.newArrayList();
    }
    documentation.sees.add(reference);
    return true;
  }
  boolean documentReturn(String description) {
    if(!lazyInitDocumentation()) {
      return true;
    }
    if(documentation.returnDescription != null) {
      return false;
    }
    documentation.returnDescription = description;
    return true;
  }
  boolean documentThrows(JSTypeExpression type, String throwsDescription) {
    if(!lazyInitDocumentation()) {
      return true;
    }
    if(documentation.throwsDescriptions == null) {
      documentation.throwsDescriptions = new LinkedHashMap<JSTypeExpression, String>();
    }
    if(!documentation.throwsDescriptions.containsKey(type)) {
      documentation.throwsDescriptions.put(type, throwsDescription);
      return true;
    }
    return false;
  }
  boolean documentVersion(String version) {
    if(!lazyInitDocumentation()) {
      return true;
    }
    if(documentation.version != null) {
      return false;
    }
    documentation.version = version;
    return true;
  }
  private boolean getFlag(int mask) {
    return (bitset & mask) != 0x00;
  }
  public boolean hasBaseType() {
    return getBaseType() != null;
  }
  public boolean hasDescriptionForParameter(String name) {
    if(documentation == null || documentation.parameters == null) {
      return false;
    }
    return documentation.parameters.containsKey(name);
  }
  public boolean hasEnumParameterType() {
    return hasType(TYPEFIELD_ENUM);
  }
  public boolean hasFileOverview() {
    return getFlag(MASK_FILEOVERVIEW);
  }
  public boolean hasModifies() {
    return info != null && info.modifies != null;
  }
  public boolean hasParameter(String parameter) {
    if(info == null || info.parameters == null) {
      return false;
    }
    return info.parameters.containsKey(parameter);
  }
  public boolean hasParameterType(String parameter) {
    return getParameterType(parameter) != null;
  }
  public boolean hasReturnType() {
    return hasType(TYPEFIELD_RETURN);
  }
  public boolean hasThisType() {
    return thisType != null;
  }
  public boolean hasType() {
    return hasType(TYPEFIELD_TYPE);
  }
  private boolean hasType(int mask) {
    return (bitset & MASK_TYPEFIELD) == mask;
  }
  public boolean hasTypedefType() {
    return hasType(TYPEFIELD_TYPEDEF);
  }
  public boolean isConsistentIdGenerator() {
    return getFlag(MASK_CONSISTIDGEN);
  }
  public boolean isConstant() {
    return getFlag(MASK_CONSTANT) || isDefine();
  }
  public boolean isConstructor() {
    return getFlag(MASK_CONSTRUCTOR);
  }
  public boolean isDefine() {
    return getFlag(MASK_DEFINE);
  }
  public boolean isDeprecated() {
    return getFlag(MASK_DEPRECATED);
  }
  public boolean isExport() {
    return getFlag(MASK_EXPORT);
  }
  public boolean isExpose() {
    return getFlag(MASK_EXPOSE);
  }
  public boolean isExterns() {
    return getFlag(MASK_EXTERNS);
  }
  public boolean isHidden() {
    return getFlag(MASK_HIDDEN);
  }
  public boolean isIdGenerator() {
    return getFlag(MASK_IDGEN);
  }
  public boolean isImplicitCast() {
    return getFlag(MASK_IMPLICITCAST);
  }
  public boolean isInterface() {
    return getFlag(MASK_INTERFACE);
  }
  public boolean isJavaDispatch() {
    return getFlag(MASK_JAVADISPATCH);
  }
  public boolean isNoAlias() {
    return getFlag(MASK_NOALIAS);
  }
  public boolean isNoCompile() {
    return getFlag(MASK_NOCOMPILE);
  }
  public boolean isNoShadow() {
    return getFlag(MASK_NOSHADOW);
  }
  public boolean isNoSideEffects() {
    return getFlag(MASK_NOSIDEEFFECTS);
  }
  public boolean isNoTypeCheck() {
    return getFlag(MASK_NOCHECK);
  }
  public boolean isOverride() {
    return getFlag(MASK_OVERRIDE);
  }
  public boolean isStableIdGenerator() {
    return getFlag(MASK_STALBEIDGEN);
  }
  private boolean lazyInitDocumentation() {
    if(!includeDocumentation) {
      return false;
    }
    if(documentation == null) {
      documentation = new LazilyInitializedDocumentation();
    }
    return true;
  }
  public boolean makesDicts() {
    return getFlag(MASK_DICT);
  }
  public boolean makesStructs() {
    return getFlag(MASK_STRUCT);
  }
  boolean setDeprecationReason(String reason) {
    lazyInitInfo();
    if(info.deprecated != null) {
      return false;
    }
    info.deprecated = reason;
    return true;
  }
  boolean setModifies(Set<String> modifies) {
    lazyInitInfo();
    if(info.modifies != null) {
      return false;
    }
    info.modifies = modifies;
    return true;
  }
  boolean setSuppressions(Set<String> suppressions) {
    lazyInitInfo();
    if(info.suppressions != null) {
      return false;
    }
    info.suppressions = suppressions;
    return true;
  }
  public boolean shouldPreserveTry() {
    return getFlag(MASK_PRESERVETRY);
  }
  public int getExtendedInterfacesCount() {
    if(info == null || info.extendedInterfaces == null) {
      return 0;
    }
    return info.extendedInterfaces.size();
  }
  public int getImplementedInterfaceCount() {
    if(info == null || info.implementedInterfaces == null) {
      return 0;
    }
    return info.implementedInterfaces.size();
  }
  public int getParameterCount() {
    if(info == null || info.parameters == null) {
      return 0;
    }
    return info.parameters.size();
  }
  void addModifies(String modifies) {
    lazyInitInfo();
    if(info.modifies == null) {
      info.modifies = Sets.newHashSet();
    }
    info.modifies.add(modifies);
  }
  public void addSuppression(String suppression) {
    lazyInitInfo();
    if(info.suppressions == null) {
      info.suppressions = Sets.newHashSet();
    }
    info.suppressions.add(suppression);
  }
  private void lazyInitInfo() {
    if(info == null) {
      info = new LazilyInitializedInfo();
    }
  }
  public void setAssociatedNode(Node node) {
    this.associatedNode = node;
  }
  void setBaseType(JSTypeExpression type) {
    lazyInitInfo();
    info.baseType = type;
  }
  void setConsistentIdGenerator(boolean value) {
    setFlag(value, MASK_CONSISTIDGEN);
  }
  void setConstant(boolean value) {
    setFlag(value, MASK_CONSTANT);
  }
  void setConstructor(boolean value) {
    setFlag(value, MASK_CONSTRUCTOR);
  }
  void setDefine(boolean value) {
    setFlag(value, MASK_DEFINE);
  }
  public void setDeprecated(boolean value) {
    setFlag(value, MASK_DEPRECATED);
  }
  void setDescription(String desc) {
    lazyInitInfo();
    info.description = desc;
  }
  void setDict() {
    setFlag(true, MASK_DICT);
  }
  void setEnumParameterType(JSTypeExpression type) {
    setType(type, TYPEFIELD_ENUM);
  }
  void setExport(boolean value) {
    setFlag(value, MASK_EXPORT);
  }
  void setExpose(boolean value) {
    setFlag(value, MASK_EXPOSE);
  }
  void setExterns(boolean value) {
    setFlag(value, MASK_EXTERNS);
  }
  private void setFlag(boolean value, int mask) {
    if(value) {
      bitset |= mask;
    }
    else {
      bitset &= ~mask;
    }
  }
  void setHidden(boolean value) {
    setFlag(value, MASK_HIDDEN);
  }
  void setIdGenerator(boolean value) {
    setFlag(value, MASK_IDGEN);
  }
  void setImplicitCast(boolean value) {
    setFlag(value, MASK_IMPLICITCAST);
  }
  void setInterface(boolean value) {
    setFlag(value, MASK_INTERFACE);
  }
  void setJavaDispatch(boolean value) {
    setFlag(value, MASK_JAVADISPATCH);
  }
  void setLendsName(String name) {
    lazyInitInfo();
    info.lendsName = name;
  }
  public void setLicense(String license) {
    lazyInitInfo();
    info.license = license;
  }
  void setMeaning(String meaning) {
    lazyInitInfo();
    info.meaning = meaning;
  }
  void setNoAlias(boolean value) {
    setFlag(value, MASK_NOALIAS);
  }
  void setNoCheck(boolean value) {
    setFlag(value, MASK_NOCHECK);
  }
  void setNoCompile(boolean value) {
    setFlag(value, MASK_NOCOMPILE);
  }
  void setNoShadow(boolean value) {
    setFlag(value, MASK_NOSHADOW);
  }
  void setNoSideEffects(boolean value) {
    setFlag(value, MASK_NOSIDEEFFECTS);
  }
  void setOriginalCommentString(String sourceComment) {
    if(!lazyInitDocumentation()) {
      return ;
    }
    documentation.sourceComment = sourceComment;
  }
  void setOverride(boolean value) {
    setFlag(value, MASK_OVERRIDE);
  }
  void setReturnType(JSTypeExpression type) {
    setType(type, TYPEFIELD_RETURN);
  }
  void setShouldPreserveTry(boolean value) {
    setFlag(value, MASK_PRESERVETRY);
  }
  void setStableIdGenerator(boolean value) {
    setFlag(value, MASK_STALBEIDGEN);
  }
  void setStruct() {
    setFlag(true, MASK_STRUCT);
  }
  void setThisType(JSTypeExpression type) {
    this.thisType = type;
  }
  void setType(JSTypeExpression type) {
    setType(type, TYPEFIELD_TYPE);
  }
  private void setType(JSTypeExpression type, int mask) {
    if((bitset & MASK_TYPEFIELD) != 0) {
      throw new IllegalStateException("API tried to add two incompatible type tags. " + "This should have been blocked and emitted a warning.");
    }
    this.bitset = (bitset & MASK_FLAGS) | mask;
    this.type = type;
  }
  void setTypedefType(JSTypeExpression type) {
    setType(type, TYPEFIELD_TYPEDEF);
  }
  public void setVisibility(Visibility visibility) {
    this.visibility = visibility;
  }
  
  final private static class LazilyInitializedDocumentation  {
    String sourceComment = null;
    List<Marker> markers = null;
    Map<String, String> parameters = null;
    Map<JSTypeExpression, String> throwsDescriptions = null;
    String blockDescription = null;
    String fileOverview = null;
    String returnDescription = null;
    String version = null;
    List<String> authors = null;
    List<String> sees = null;
  }
  
  final private static class LazilyInitializedInfo implements Serializable  {
    final private static long serialVersionUID = 1L;
    JSTypeExpression baseType = null;
    List<JSTypeExpression> extendedInterfaces = null;
    List<JSTypeExpression> implementedInterfaces = null;
    Map<String, JSTypeExpression> parameters = null;
    List<JSTypeExpression> thrownTypes = null;
    ImmutableList<String> templateTypeNames = null;
    String description = null;
    String meaning = null;
    String deprecated = null;
    String license = null;
    Set<String> suppressions = null;
    Set<String> modifies = null;
    String lendsName = null;
  }
  
  final public static class Marker  {
    private TrimmedStringPosition annotation = null;
    private TrimmedStringPosition name = null;
    private SourcePosition<Node> nameNode = null;
    private StringPosition description = null;
    private TypePosition type = null;
    public SourcePosition<Node> getNameNode() {
      return nameNode;
    }
    public StringPosition getAnnotation() {
      return annotation;
    }
    public StringPosition getDescription() {
      return description;
    }
    @Deprecated() public StringPosition getName() {
      return name;
    }
    public TypePosition getType() {
      return type;
    }
    void setAnnotation(TrimmedStringPosition p) {
      annotation = p;
    }
    void setDescription(StringPosition p) {
      description = p;
    }
    void setName(TrimmedStringPosition p) {
      name = p;
    }
    void setNameNode(SourcePosition<Node> p) {
      nameNode = p;
    }
    void setType(TypePosition p) {
      type = p;
    }
  }
  
  public static class NamePosition extends SourcePosition<Node>  {
  }
  
  public static class StringPosition extends SourcePosition<String>  {
  }
  
  static class TrimmedStringPosition extends StringPosition  {
    @Override() public void setItem(String item) {
      Preconditions.checkArgument(item.charAt(0) != ' ' && item.charAt(item.length() - 1) != ' ', "String has leading or trailing whitespace");
      super.setItem(item);
    }
  }
  
  public static class TypePosition extends SourcePosition<Node>  {
    private boolean brackets = false;
    public boolean hasBrackets() {
      return brackets;
    }
    void setHasBrackets(boolean newVal) {
      brackets = newVal;
    }
  }
  public enum Visibility {
    PRIVATE(),

    PROTECTED(),

    PUBLIC(),

    INHERITED(),

  ;
  private Visibility() {
  }
  }
}