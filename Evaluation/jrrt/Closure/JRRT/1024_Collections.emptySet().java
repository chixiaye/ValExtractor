package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SourcePosition;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompilerOptions implements Serializable, Cloneable  {
  @SuppressWarnings(value = {"unused", }) private boolean manageClosureDependencies = false;
  final private static long serialVersionUID = 7L;
  private LanguageMode languageIn;
  private LanguageMode languageOut;
  boolean acceptConstKeyword;
  private boolean assumeStrictThis;
  public boolean ideMode;
  boolean saveDataStructures = false;
  boolean inferTypes;
  boolean skipAllPasses;
  boolean nameAnonymousFunctionsOnly;
  DevMode devMode;
  DependencyOptions dependencyOptions = new DependencyOptions();
  public transient MessageBundle messageBundle = null;
  public boolean checkSymbols;
  public CheckLevel aggressiveVarCheck;
  public boolean checkSuspiciousCode;
  public boolean checkControlStructures;
  public boolean checkTypes;
  boolean tightenTypes;
  public CheckLevel reportMissingOverride;
  CheckLevel reportUnknownTypes;
  public CheckLevel checkRequires;
  public CheckLevel checkProvides;
  public CheckLevel checkGlobalNamesLevel;
  public CheckLevel brokenClosureRequiresLevel;
  public CheckLevel checkGlobalThisLevel;
  public CheckLevel checkMissingGetCssNameLevel;
  public String checkMissingGetCssNameBlacklist;
  boolean checkCaja;
  Set<String> extraAnnotationNames;
  public boolean foldConstants;
  public boolean deadAssignmentElimination;
  public boolean inlineConstantVars;
  public boolean inlineFunctions;
  public boolean inlineLocalFunctions;
  boolean inlineProperties;
  public boolean crossModuleCodeMotion;
  public boolean coalesceVariableNames;
  public boolean crossModuleMethodMotion;
  public boolean inlineGetters;
  public boolean inlineVariables;
  boolean inlineLocalVariables;
  public boolean flowSensitiveInlineVariables;
  public boolean smartNameRemoval;
  public boolean removeDeadCode;
  public CheckLevel checkUnreachableCode;
  public CheckLevel checkMissingReturn;
  public boolean extractPrototypeMemberDeclarations;
  public boolean removeUnusedPrototypeProperties;
  public boolean removeUnusedPrototypePropertiesInExterns;
  public boolean removeUnusedClassProperties;
  public boolean removeUnusedVars;
  public boolean removeUnusedLocalVars;
  public boolean aliasExternals;
  String aliasableGlobals;
  String unaliasableGlobals;
  public boolean collapseVariableDeclarations;
  boolean groupVariableDeclarations;
  public boolean collapseAnonymousFunctions;
  public Set<String> aliasableStrings;
  public String aliasStringsBlacklist;
  public boolean aliasAllStrings;
  boolean outputJsStringUsage;
  public boolean convertToDottedProperties;
  public boolean rewriteFunctionExpressions;
  public boolean optimizeParameters;
  public boolean optimizeReturns;
  public boolean optimizeCalls;
  public boolean optimizeArgumentsArray;
  boolean chainCalls;
  public VariableRenamingPolicy variableRenaming;
  public PropertyRenamingPolicy propertyRenaming;
  boolean propertyAffinity;
  public boolean labelRenaming;
  public boolean reserveRawExports;
  boolean shadowVariables;
  public boolean generatePseudoNames;
  public String renamePrefix;
  public String renamePrefixNamespace;
  public boolean aliasKeywords;
  public boolean collapseProperties;
  boolean collapseObjectLiterals;
  boolean collapsePropertiesOnExternTypes;
  public boolean devirtualizePrototypeMethods;
  public boolean computeFunctionSideEffects;
  String debugFunctionSideEffectsPath;
  public boolean disambiguateProperties;
  public boolean ambiguateProperties;
  public AnonymousFunctionNamingPolicy anonymousFunctionNaming;
  VariableMap inputAnonymousFunctionNamingMap;
  VariableMap inputVariableMap;
  VariableMap inputPropertyMap;
  public boolean exportTestFunctions;
  boolean specializeInitialModule;
  boolean replaceMessagesWithChromeI18n;
  String tcProjectId;
  boolean runtimeTypeCheck;
  String runtimeTypeCheckLogFunction;
  private CodingConvention codingConvention;
  boolean ignoreCajaProperties;
  public String syntheticBlockStartMarker;
  public String syntheticBlockEndMarker;
  public String locale;
  public boolean markAsCompiled;
  public boolean removeTryCatchFinally;
  public boolean closurePass;
  public boolean jqueryPass;
  boolean removeAbstractMethods;
  boolean removeClosureAsserts;
  public boolean gatherCssNames;
  public Set<String> stripTypes;
  public Set<String> stripNameSuffixes;
  public Set<String> stripNamePrefixes;
  public Set<String> stripTypePrefixes;
  public transient Multimap<CustomPassExecutionTime, CompilerPass> customPasses;
  public boolean markNoSideEffectCalls;
  private Map<String, Object> defineReplacements;
  private TweakProcessing tweakProcessing;
  private Map<String, Object> tweakReplacements;
  public boolean moveFunctionDeclarations;
  public String instrumentationTemplate;
  String appNameStr;
  public boolean recordFunctionInformation;
  public boolean generateExports;
  public CssRenamingMap cssRenamingMap;
  Set<String> cssRenamingWhitelist;
  boolean processObjectPropertyString;
  boolean replaceIdGenerators = true;
  Set<String> idGenerators;
  String idGeneratorsMapSerialized;
  List<String> replaceStringsFunctionDescriptions;
  String replaceStringsPlaceholderToken;
  Set<String> replaceStringsReservedStrings;
  VariableMap replaceStringsInputMap;
  Map<String, CheckLevel> propertyInvalidationErrors;
  boolean transformAMDToCJSModules = false;
  boolean processCommonJSModules = false;
  String commonJSModulePathPrefix = ProcessCommonJSModules.DEFAULT_FILENAME_PREFIX;
  public boolean prettyPrint;
  public boolean lineBreak;
  public boolean preferLineBreakAtEndOfFile;
  public boolean printInputDelimiter;
  public String inputDelimiter = "// Input %num%";
  boolean preferSingleQuotes;
  boolean trustedStrings;
  String reportPath;
  TracerMode tracer;
  private boolean colorizeErrorOutput;
  public ErrorFormat errorFormat;
  private ComposeWarningsGuard warningsGuard = new ComposeWarningsGuard();
  int summaryDetailLevel = 1;
  int lineLengthThreshold = CodePrinter.DEFAULT_LINE_LENGTH_THRESHOLD;
  private boolean externExports;
  String externExportsPath;
  String nameReferenceReportPath;
  String nameReferenceGraphPath;
  public String sourceMapOutputPath;
  public SourceMap.DetailLevel sourceMapDetailLevel = SourceMap.DetailLevel.SYMBOLS;
  public SourceMap.Format sourceMapFormat = SourceMap.Format.DEFAULT;
  public List<SourceMap.LocationMapping> sourceMapLocationMappings = Collections.emptyList();
  String outputCharset;
  boolean looseTypes;
  boolean protectHiddenSideEffects;
  private transient AliasTransformationHandler aliasHandler;
  transient ErrorHandler errorHandler;
  final static AliasTransformationHandler NULL_ALIAS_TRANSFORMATION_HANDLER = new NullAliasTransformationHandler();
  public CompilerOptions() {
    super();
    languageIn = LanguageMode.ECMASCRIPT3;
    acceptConstKeyword = false;
    skipAllPasses = false;
    nameAnonymousFunctionsOnly = false;
    devMode = DevMode.OFF;
    checkSymbols = false;
    aggressiveVarCheck = CheckLevel.OFF;
    checkSuspiciousCode = false;
    checkControlStructures = false;
    checkTypes = false;
    tightenTypes = false;
    reportMissingOverride = CheckLevel.OFF;
    reportUnknownTypes = CheckLevel.OFF;
    checkRequires = CheckLevel.OFF;
    checkProvides = CheckLevel.OFF;
    checkGlobalNamesLevel = CheckLevel.OFF;
    brokenClosureRequiresLevel = CheckLevel.ERROR;
    checkGlobalThisLevel = CheckLevel.OFF;
    checkUnreachableCode = CheckLevel.OFF;
    checkMissingReturn = CheckLevel.OFF;
    checkMissingGetCssNameLevel = CheckLevel.OFF;
    checkMissingGetCssNameBlacklist = null;
    checkCaja = false;
    computeFunctionSideEffects = false;
    chainCalls = false;
    extraAnnotationNames = null;
    foldConstants = false;
    coalesceVariableNames = false;
    deadAssignmentElimination = false;
    inlineConstantVars = false;
    inlineFunctions = false;
    inlineLocalFunctions = false;
    assumeStrictThis = false;
    inlineProperties = false;
    crossModuleCodeMotion = false;
    crossModuleMethodMotion = false;
    inlineGetters = false;
    inlineVariables = false;
    inlineLocalVariables = false;
    smartNameRemoval = false;
    removeDeadCode = false;
    extractPrototypeMemberDeclarations = false;
    removeUnusedPrototypeProperties = false;
    removeUnusedPrototypePropertiesInExterns = false;
    removeUnusedClassProperties = false;
    removeUnusedVars = false;
    removeUnusedLocalVars = false;
    aliasExternals = false;
    collapseVariableDeclarations = false;
    groupVariableDeclarations = false;
    collapseAnonymousFunctions = false;
    Set<String> var_1024 = Collections.emptySet();
    aliasableStrings = var_1024;
    aliasStringsBlacklist = "";
    aliasAllStrings = false;
    outputJsStringUsage = false;
    convertToDottedProperties = false;
    rewriteFunctionExpressions = false;
    optimizeParameters = false;
    optimizeReturns = false;
    variableRenaming = VariableRenamingPolicy.OFF;
    propertyRenaming = PropertyRenamingPolicy.OFF;
    propertyAffinity = false;
    labelRenaming = false;
    generatePseudoNames = false;
    shadowVariables = false;
    renamePrefix = null;
    aliasKeywords = false;
    collapseProperties = false;
    collapsePropertiesOnExternTypes = false;
    collapseObjectLiterals = false;
    devirtualizePrototypeMethods = false;
    disambiguateProperties = false;
    ambiguateProperties = false;
    anonymousFunctionNaming = AnonymousFunctionNamingPolicy.OFF;
    exportTestFunctions = false;
    runtimeTypeCheck = false;
    runtimeTypeCheckLogFunction = null;
    ignoreCajaProperties = false;
    syntheticBlockStartMarker = null;
    syntheticBlockEndMarker = null;
    locale = null;
    markAsCompiled = false;
    removeTryCatchFinally = false;
    closurePass = false;
    jqueryPass = false;
    removeAbstractMethods = true;
    removeClosureAsserts = false;
    stripTypes = Collections.emptySet();
    stripNameSuffixes = Collections.emptySet();
    stripNamePrefixes = Collections.emptySet();
    stripTypePrefixes = Collections.emptySet();
    customPasses = null;
    markNoSideEffectCalls = false;
    defineReplacements = Maps.newHashMap();
    tweakProcessing = TweakProcessing.OFF;
    tweakReplacements = Maps.newHashMap();
    moveFunctionDeclarations = false;
    instrumentationTemplate = null;
    appNameStr = "";
    recordFunctionInformation = false;
    generateExports = false;
    cssRenamingMap = null;
    cssRenamingWhitelist = null;
    processObjectPropertyString = false;
    idGenerators = Collections.emptySet();
    replaceStringsFunctionDescriptions = Collections.emptyList();
    replaceStringsPlaceholderToken = "";
    replaceStringsReservedStrings = Collections.emptySet();
    propertyInvalidationErrors = Maps.newHashMap();
    printInputDelimiter = false;
    prettyPrint = false;
    lineBreak = false;
    preferLineBreakAtEndOfFile = false;
    reportPath = null;
    tracer = TracerMode.OFF;
    colorizeErrorOutput = false;
    errorFormat = ErrorFormat.SINGLELINE;
    debugFunctionSideEffectsPath = null;
    externExports = false;
    nameReferenceReportPath = null;
    nameReferenceGraphPath = null;
    aliasHandler = NULL_ALIAS_TRANSFORMATION_HANDLER;
    errorHandler = null;
  }
  public AliasTransformationHandler getAliasTransformationHandler() {
    return this.aliasHandler;
  }
  Charset getOutputCharset() {
    return outputCharset == null ? null : Charset.forName(outputCharset);
  }
  public CodingConvention getCodingConvention() {
    return codingConvention;
  }
  public LanguageMode getLanguageIn() {
    return languageIn;
  }
  public LanguageMode getLanguageOut() {
    return languageOut;
  }
  public Map<String, Node> getDefineReplacements() {
    return getReplacementsHelper(defineReplacements);
  }
  private static Map<String, Node> getReplacementsHelper(Map<String, Object> source) {
    Map<String, Node> map = Maps.newHashMap();
    for (Map.Entry<String, Object> entry : source.entrySet()) {
      String name = entry.getKey();
      Object value = entry.getValue();
      if(value instanceof Boolean) {
        map.put(name, NodeUtil.booleanNode(((Boolean)value).booleanValue()));
      }
      else 
        if(value instanceof Integer) {
          map.put(name, IR.number(((Integer)value).intValue()));
        }
        else 
          if(value instanceof Double) {
            map.put(name, IR.number(((Double)value).doubleValue()));
          }
          else {
            Preconditions.checkState(value instanceof String);
            map.put(name, IR.string((String)value));
          }
    }
    return map;
  }
  public Map<String, Node> getTweakReplacements() {
    return getReplacementsHelper(tweakReplacements);
  }
  @Override() public Object clone() throws CloneNotSupportedException {
    CompilerOptions clone = (CompilerOptions)super.clone();
    return clone;
  }
  public TracerMode getTracerMode() {
    return tracer;
  }
  public TweakProcessing getTweakProcessing() {
    return tweakProcessing;
  }
  WarningsGuard getWarningsGuard() {
    return warningsGuard;
  }
  public boolean assumeStrictThis() {
    return assumeStrictThis;
  }
  boolean disables(DiagnosticGroup type) {
    return warningsGuard.disables(type);
  }
  boolean enables(DiagnosticGroup type) {
    return warningsGuard.enables(type);
  }
  public boolean getInferTypes() {
    return inferTypes;
  }
  public boolean isExternExportsEnabled() {
    return externExports;
  }
  public boolean isRemoveUnusedClassProperties() {
    return removeUnusedClassProperties;
  }
  public boolean shouldColorizeErrorOutput() {
    return colorizeErrorOutput;
  }
  public void addWarningsGuard(WarningsGuard guard) {
    warningsGuard.addGuard(guard);
  }
  public void disableRuntimeTypeCheck() {
    this.runtimeTypeCheck = false;
  }
  @Deprecated() public void enableExternExports(boolean enabled) {
    this.externExports = enabled;
  }
  public void enableRuntimeTypeCheck(String logFunction) {
    this.runtimeTypeCheck = true;
    this.runtimeTypeCheckLogFunction = logFunction;
  }
  public void resetWarningsGuard() {
    warningsGuard = new ComposeWarningsGuard();
  }
  public void setAcceptConstKeyword(boolean value) {
    this.acceptConstKeyword = value;
  }
  public void setAggressiveVarCheck(CheckLevel level) {
    this.aggressiveVarCheck = level;
  }
  public void setAliasAllStrings(boolean aliasAllStrings) {
    this.aliasAllStrings = aliasAllStrings;
  }
  public void setAliasExternals(boolean aliasExternals) {
    this.aliasExternals = aliasExternals;
  }
  public void setAliasKeywords(boolean aliasKeywords) {
    this.aliasKeywords = aliasKeywords;
  }
  public void setAliasStringsBlacklist(String aliasStringsBlacklist) {
    this.aliasStringsBlacklist = aliasStringsBlacklist;
  }
  public void setAliasTransformationHandler(AliasTransformationHandler changes) {
    this.aliasHandler = changes;
  }
  public void setAliasableGlobals(String names) {
    aliasableGlobals = names;
  }
  public void setAliasableStrings(Set<String> aliasableStrings) {
    this.aliasableStrings = aliasableStrings;
  }
  public void setAmbiguateProperties(boolean ambiguateProperties) {
    this.ambiguateProperties = ambiguateProperties;
  }
  public void setAnonymousFunctionNaming(AnonymousFunctionNamingPolicy anonymousFunctionNaming) {
    this.anonymousFunctionNaming = anonymousFunctionNaming;
  }
  public void setAppNameStr(String appNameStr) {
    this.appNameStr = appNameStr;
  }
  public void setAssumeStrictThis(boolean enable) {
    this.assumeStrictThis = enable;
  }
  public void setBrokenClosureRequiresLevel(CheckLevel level) {
    brokenClosureRequiresLevel = level;
  }
  public void setChainCalls(boolean value) {
    this.chainCalls = value;
  }
  public void setCheckCaja(boolean check) {
    checkCaja = check;
  }
  public void setCheckControlStructures(boolean checkControlStructures) {
    this.checkControlStructures = checkControlStructures;
  }
  public void setCheckGlobalNamesLevel(CheckLevel level) {
    checkGlobalNamesLevel = level;
  }
  public void setCheckGlobalThisLevel(CheckLevel level) {
    this.checkGlobalThisLevel = level;
  }
  public void setCheckMissingGetCssNameBlacklist(String blackList) {
    this.checkMissingGetCssNameBlacklist = blackList;
  }
  public void setCheckMissingGetCssNameLevel(CheckLevel level) {
    this.checkMissingGetCssNameLevel = level;
  }
  public void setCheckMissingReturn(CheckLevel level) {
    this.checkMissingReturn = level;
  }
  public void setCheckProvides(CheckLevel level) {
    checkProvides = level;
  }
  public void setCheckRequires(CheckLevel level) {
    checkRequires = level;
  }
  public void setCheckSuspiciousCode(boolean checkSuspiciousCode) {
    this.checkSuspiciousCode = checkSuspiciousCode;
  }
  public void setCheckSymbols(boolean checkSymbols) {
    this.checkSymbols = checkSymbols;
  }
  public void setCheckTypes(boolean checkTypes) {
    this.checkTypes = checkTypes;
  }
  public void setCheckUnreachableCode(CheckLevel level) {
    this.checkUnreachableCode = level;
  }
  public void setClosurePass(boolean closurePass) {
    this.closurePass = closurePass;
  }
  public void setCoalesceVariableNames(boolean coalesceVariableNames) {
    this.coalesceVariableNames = coalesceVariableNames;
  }
  public void setCodingConvention(CodingConvention codingConvention) {
    this.codingConvention = codingConvention;
  }
  public void setCollapseAnonymousFunctions(boolean enabled) {
    this.collapseAnonymousFunctions = enabled;
  }
  public void setCollapseObjectLiterals(boolean enabled) {
    collapseObjectLiterals = enabled;
  }
  public void setCollapseProperties(boolean collapseProperties) {
    this.collapseProperties = collapseProperties;
  }
  public void setCollapsePropertiesOnExternTypes(boolean collapse) {
    collapsePropertiesOnExternTypes = collapse;
  }
  public void setCollapseVariableDeclarations(boolean enabled) {
    this.collapseVariableDeclarations = enabled;
  }
  public void setColorizeErrorOutput(boolean colorizeErrorOutput) {
    this.colorizeErrorOutput = colorizeErrorOutput;
  }
  public void setCommonJSModulePathPrefix(String commonJSModulePathPrefix) {
    this.commonJSModulePathPrefix = commonJSModulePathPrefix;
  }
  public void setComputeFunctionSideEffects(boolean computeFunctionSideEffects) {
    this.computeFunctionSideEffects = computeFunctionSideEffects;
  }
  public void setConvertToDottedProperties(boolean convertToDottedProperties) {
    this.convertToDottedProperties = convertToDottedProperties;
  }
  public void setCrossModuleCodeMotion(boolean crossModuleCodeMotion) {
    this.crossModuleCodeMotion = crossModuleCodeMotion;
  }
  public void setCrossModuleMethodMotion(boolean crossModuleMethodMotion) {
    this.crossModuleMethodMotion = crossModuleMethodMotion;
  }
  public void setCssRenamingMap(CssRenamingMap cssRenamingMap) {
    this.cssRenamingMap = cssRenamingMap;
  }
  public void setCssRenamingWhitelist(Set<String> whitelist) {
    this.cssRenamingWhitelist = whitelist;
  }
  public void setCustomPasses(Multimap<CustomPassExecutionTime, CompilerPass> customPasses) {
    this.customPasses = customPasses;
  }
  public void setDeadAssignmentElimination(boolean deadAssignmentElimination) {
    this.deadAssignmentElimination = deadAssignmentElimination;
  }
  public void setDebugFunctionSideEffectsPath(String debugFunctionSideEffectsPath) {
    this.debugFunctionSideEffectsPath = debugFunctionSideEffectsPath;
  }
  public void setDefineReplacements(Map<String, Object> defineReplacements) {
    this.defineReplacements = defineReplacements;
  }
  public void setDefineToBooleanLiteral(String defineName, boolean value) {
    defineReplacements.put(defineName, new Boolean(value));
  }
  public void setDefineToDoubleLiteral(String defineName, double value) {
    defineReplacements.put(defineName, new Double(value));
  }
  public void setDefineToNumberLiteral(String defineName, int value) {
    defineReplacements.put(defineName, new Integer(value));
  }
  public void setDefineToStringLiteral(String defineName, String value) {
    defineReplacements.put(defineName, value);
  }
  public void setDependencyOptions(DependencyOptions options) {
    Preconditions.checkNotNull(options);
    this.dependencyOptions = options;
  }
  public void setDevMode(DevMode devMode) {
    this.devMode = devMode;
  }
  public void setDevirtualizePrototypeMethods(boolean devirtualizePrototypeMethods) {
    this.devirtualizePrototypeMethods = devirtualizePrototypeMethods;
  }
  public void setDisambiguateProperties(boolean disambiguateProperties) {
    this.disambiguateProperties = disambiguateProperties;
  }
  public void setErrorFormat(ErrorFormat errorFormat) {
    this.errorFormat = errorFormat;
  }
  public void setErrorHandler(ErrorHandler handler) {
    this.errorHandler = handler;
  }
  public void setExportTestFunctions(boolean exportTestFunctions) {
    this.exportTestFunctions = exportTestFunctions;
  }
  public void setExternExports(boolean externExports) {
    this.externExports = externExports;
  }
  public void setExternExportsPath(String externExportsPath) {
    this.externExportsPath = externExportsPath;
  }
  public void setExtraAnnotationNames(Set<String> extraAnnotationNames) {
    this.extraAnnotationNames = Sets.newHashSet(extraAnnotationNames);
  }
  public void setExtractPrototypeMemberDeclarations(boolean enabled) {
    this.extractPrototypeMemberDeclarations = enabled;
  }
  public void setFlowSensitiveInlineVariables(boolean enabled) {
    this.flowSensitiveInlineVariables = enabled;
  }
  public void setFoldConstants(boolean foldConstants) {
    this.foldConstants = foldConstants;
  }
  public void setGatherCssNames(boolean gatherCssNames) {
    this.gatherCssNames = gatherCssNames;
  }
  public void setGenerateExports(boolean generateExports) {
    this.generateExports = generateExports;
  }
  public void setGeneratePseudoNames(boolean generatePseudoNames) {
    this.generatePseudoNames = generatePseudoNames;
  }
  public void setGroupVariableDeclarations(boolean enabled) {
    this.groupVariableDeclarations = enabled;
  }
  public void setIdGenerators(Set<String> idGenerators) {
    this.idGenerators = Sets.newHashSet(idGenerators);
  }
  public void setIdGeneratorsMap(String previousMappings) {
    this.idGeneratorsMapSerialized = previousMappings;
  }
  public void setIdeMode(boolean ideMode) {
    this.ideMode = ideMode;
  }
  public void setIgnoreCajaProperties(boolean enabled) {
    ignoreCajaProperties = enabled;
  }
  public void setInferTypes(boolean enable) {
    inferTypes = enable;
  }
  public void setInlineConstantVars(boolean inlineConstantVars) {
    this.inlineConstantVars = inlineConstantVars;
  }
  public void setInlineFunctions(boolean inlineFunctions) {
    this.inlineFunctions = inlineFunctions;
  }
  public void setInlineFunctions(Reach reach) {
    switch (reach){
      case ALL:
      this.inlineFunctions = true;
      this.inlineLocalFunctions = true;
      break ;
      case LOCAL_ONLY:
      this.inlineFunctions = false;
      this.inlineLocalFunctions = true;
      break ;
      case NONE:
      this.inlineFunctions = false;
      this.inlineLocalFunctions = false;
      break ;
      default:
      throw new IllegalStateException("unexpected");
    }
  }
  public void setInlineGetters(boolean inlineGetters) {
    this.inlineGetters = inlineGetters;
  }
  public void setInlineLocalFunctions(boolean inlineLocalFunctions) {
    this.inlineLocalFunctions = inlineLocalFunctions;
  }
  public void setInlineLocalVariables(boolean inlineLocalVariables) {
    this.inlineLocalVariables = inlineLocalVariables;
  }
  public void setInlineProperties(boolean enable) {
    inlineProperties = enable;
  }
  public void setInlineVariables(boolean inlineVariables) {
    this.inlineVariables = inlineVariables;
  }
  public void setInlineVariables(Reach reach) {
    switch (reach){
      case ALL:
      this.inlineVariables = true;
      this.inlineLocalVariables = true;
      break ;
      case LOCAL_ONLY:
      this.inlineVariables = false;
      this.inlineLocalVariables = true;
      break ;
      case NONE:
      this.inlineVariables = false;
      this.inlineLocalVariables = false;
      break ;
      default:
      throw new IllegalStateException("unexpected");
    }
  }
  public void setInputAnonymousFunctionNamingMap(VariableMap inputMap) {
    this.inputAnonymousFunctionNamingMap = inputMap;
  }
  public void setInputDelimiter(String inputDelimiter) {
    this.inputDelimiter = inputDelimiter;
  }
  public void setInputPropertyMap(VariableMap inputPropertyMap) {
    this.inputPropertyMap = inputPropertyMap;
  }
  @Deprecated() public void setInputPropertyMapSerialized(byte[] inputPropertyMapSerialized) throws ParseException {
    this.inputPropertyMap = VariableMap.fromBytes(inputPropertyMapSerialized);
  }
  public void setInputVariableMap(VariableMap inputVariableMap) {
    this.inputVariableMap = inputVariableMap;
  }
  @Deprecated() public void setInputVariableMapSerialized(byte[] inputVariableMapSerialized) throws ParseException {
    this.inputVariableMap = VariableMap.fromBytes(inputVariableMapSerialized);
  }
  public void setInstrumentationTemplate(String instrumentationTemplate) {
    this.instrumentationTemplate = instrumentationTemplate;
  }
  public void setLabelRenaming(boolean labelRenaming) {
    this.labelRenaming = labelRenaming;
  }
  public void setLanguageIn(LanguageMode languageIn) {
    this.languageIn = languageIn;
    this.languageOut = languageIn;
  }
  public void setLanguageOut(LanguageMode languageOut) {
    this.languageOut = languageOut;
  }
  public void setLineBreak(boolean lineBreak) {
    this.lineBreak = lineBreak;
  }
  public void setLineLengthThreshold(int lineLengthThreshold) {
    this.lineLengthThreshold = lineLengthThreshold;
  }
  public void setLocale(String locale) {
    this.locale = locale;
  }
  public void setLooseTypes(boolean looseTypes) {
    this.looseTypes = looseTypes;
  }
  public void setManageClosureDependencies(boolean newVal) {
    dependencyOptions.setDependencySorting(newVal || dependencyOptions.shouldSortDependencies());
    dependencyOptions.setDependencyPruning(newVal || dependencyOptions.shouldPruneDependencies());
    dependencyOptions.setMoocherDropping(false);
    manageClosureDependencies = newVal;
  }
  public void setManageClosureDependencies(List<String> entryPoints) {
    Preconditions.checkNotNull(entryPoints);
    setManageClosureDependencies(true);
    dependencyOptions.setEntryPoints(entryPoints);
  }
  public void setMarkAsCompiled(boolean markAsCompiled) {
    this.markAsCompiled = markAsCompiled;
  }
  public void setMarkNoSideEffectCalls(boolean markNoSideEffectCalls) {
    this.markNoSideEffectCalls = markNoSideEffectCalls;
  }
  public void setMessageBundle(MessageBundle messageBundle) {
    this.messageBundle = messageBundle;
  }
  public void setMoveFunctionDeclarations(boolean moveFunctionDeclarations) {
    this.moveFunctionDeclarations = moveFunctionDeclarations;
  }
  public void setNameAnonymousFunctionsOnly(boolean value) {
    this.nameAnonymousFunctionsOnly = value;
  }
  public void setNameReferenceGraphPath(String filePath) {
    nameReferenceGraphPath = filePath;
  }
  public void setNameReferenceReportPath(String filePath) {
    nameReferenceReportPath = filePath;
  }
  public void setOptimizeArgumentsArray(boolean optimizeArgumentsArray) {
    this.optimizeArgumentsArray = optimizeArgumentsArray;
  }
  public void setOptimizeCalls(boolean optimizeCalls) {
    this.optimizeCalls = optimizeCalls;
  }
  public void setOptimizeParameters(boolean optimizeParameters) {
    this.optimizeParameters = optimizeParameters;
  }
  public void setOptimizeReturns(boolean optimizeReturns) {
    this.optimizeReturns = optimizeReturns;
  }
  public void setOutputCharset(String charsetName) {
    this.outputCharset = charsetName;
  }
  public void setOutputJsStringUsage(boolean outputJsStringUsage) {
    this.outputJsStringUsage = outputJsStringUsage;
  }
  public void setPreferLineBreakAtEndOfFile(boolean lineBreakAtEnd) {
    this.preferLineBreakAtEndOfFile = lineBreakAtEnd;
  }
  public void setPreferSingleQuotes(boolean enabled) {
    this.preferSingleQuotes = enabled;
  }
  public void setPrettyPrint(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }
  public void setPrintInputDelimiter(boolean printInputDelimiter) {
    this.printInputDelimiter = printInputDelimiter;
  }
  public void setProcessCommonJSModules(boolean processCommonJSModules) {
    this.processCommonJSModules = processCommonJSModules;
  }
  public void setProcessObjectPropertyString(boolean process) {
    processObjectPropertyString = process;
  }
  public void setPropertyAffinity(boolean useAffinity) {
    this.propertyAffinity = useAffinity;
  }
  public void setPropertyInvalidationErrors(Map<String, CheckLevel> propertyInvalidationErrors) {
    this.propertyInvalidationErrors = Maps.newHashMap(propertyInvalidationErrors);
  }
  public void setPropertyRenaming(PropertyRenamingPolicy propertyRenaming) {
    this.propertyRenaming = propertyRenaming;
  }
  public void setProtectHiddenSideEffects(boolean enable) {
    this.protectHiddenSideEffects = enable;
  }
  public void setRecordFunctionInformation(boolean recordFunctionInformation) {
    this.recordFunctionInformation = recordFunctionInformation;
  }
  public void setRemoveAbstractMethods(boolean remove) {
    this.removeAbstractMethods = remove;
  }
  public void setRemoveClosureAsserts(boolean remove) {
    this.removeClosureAsserts = remove;
  }
  public void setRemoveDeadCode(boolean removeDeadCode) {
    this.removeDeadCode = removeDeadCode;
  }
  public void setRemoveTryCatchFinally(boolean removeTryCatchFinally) {
    this.removeTryCatchFinally = removeTryCatchFinally;
  }
  public void setRemoveUnusedClassProperties(boolean removeUnusedClassProperties) {
    this.removeUnusedClassProperties = removeUnusedClassProperties;
  }
  public void setRemoveUnusedLocalVars(boolean removeUnusedLocalVars) {
    this.removeUnusedLocalVars = removeUnusedLocalVars;
  }
  public void setRemoveUnusedPrototypeProperties(boolean enabled) {
    this.removeUnusedPrototypeProperties = enabled;
  }
  public void setRemoveUnusedPrototypePropertiesInExterns(boolean enabled) {
    this.removeUnusedPrototypePropertiesInExterns = enabled;
  }
  @Deprecated() public void setRemoveUnusedVariable(Reach reach) {
    setRemoveUnusedVariables(reach);
  }
  public void setRemoveUnusedVariables(Reach reach) {
    switch (reach){
      case ALL:
      this.removeUnusedVars = true;
      this.removeUnusedLocalVars = true;
      break ;
      case LOCAL_ONLY:
      this.removeUnusedVars = false;
      this.removeUnusedLocalVars = true;
      break ;
      case NONE:
      this.removeUnusedVars = false;
      this.removeUnusedLocalVars = false;
      break ;
      default:
      throw new IllegalStateException("unexpected");
    }
  }
  public void setRemoveUnusedVars(boolean removeUnusedVars) {
    this.removeUnusedVars = removeUnusedVars;
  }
  public void setRenamePrefix(String renamePrefix) {
    this.renamePrefix = renamePrefix;
  }
  public void setRenamePrefixNamespace(String renamePrefixNamespace) {
    this.renamePrefixNamespace = renamePrefixNamespace;
  }
  public void setRenamingPolicy(VariableRenamingPolicy newVariablePolicy, PropertyRenamingPolicy newPropertyPolicy) {
    this.variableRenaming = newVariablePolicy;
    this.propertyRenaming = newPropertyPolicy;
  }
  public void setReplaceIdGenerators(boolean replaceIdGenerators) {
    this.replaceIdGenerators = replaceIdGenerators;
  }
  public void setReplaceMessagesWithChromeI18n(boolean replaceMessagesWithChromeI18n, String tcProjectId) {
    if(replaceMessagesWithChromeI18n && messageBundle != null && !(messageBundle instanceof EmptyMessageBundle)) {
      throw new RuntimeException("When replacing messages with " + "chrome.i18n.getMessage, a message bundle should not be specified.");
    }
    this.replaceMessagesWithChromeI18n = replaceMessagesWithChromeI18n;
    this.tcProjectId = tcProjectId;
  }
  public void setReplaceStringsConfiguration(String placeholderToken, List<String> functionDescriptors) {
    this.replaceStringsPlaceholderToken = placeholderToken;
    this.replaceStringsFunctionDescriptions = Lists.newArrayList(functionDescriptors);
  }
  public void setReplaceStringsFunctionDescriptions(List<String> replaceStringsFunctionDescriptions) {
    this.replaceStringsFunctionDescriptions = replaceStringsFunctionDescriptions;
  }
  public void setReplaceStringsInputMap(VariableMap serializedMap) {
    this.replaceStringsInputMap = serializedMap;
  }
  public void setReplaceStringsPlaceholderToken(String replaceStringsPlaceholderToken) {
    this.replaceStringsPlaceholderToken = replaceStringsPlaceholderToken;
  }
  public void setReplaceStringsReservedStrings(Set<String> replaceStringsReservedStrings) {
    this.replaceStringsReservedStrings = replaceStringsReservedStrings;
  }
  public void setReportMissingOverride(CheckLevel level) {
    reportMissingOverride = level;
  }
  public void setReportPath(String reportPath) {
    this.reportPath = reportPath;
  }
  public void setReportUnknownTypes(CheckLevel level) {
    reportUnknownTypes = level;
  }
  public void setReserveRawExports(boolean reserveRawExports) {
    this.reserveRawExports = reserveRawExports;
  }
  public void setRewriteFunctionExpressions(boolean rewriteFunctionExpressions) {
    this.rewriteFunctionExpressions = rewriteFunctionExpressions;
  }
  @Deprecated() public void setRewriteNewDateGoogNow(boolean rewrite) {
  }
  public void setRuntimeTypeCheck(boolean runtimeTypeCheck) {
    this.runtimeTypeCheck = runtimeTypeCheck;
  }
  public void setRuntimeTypeCheckLogFunction(String runtimeTypeCheckLogFunction) {
    this.runtimeTypeCheckLogFunction = runtimeTypeCheckLogFunction;
  }
  public void setSaveDataStructures(boolean save) {
    this.saveDataStructures = save;
  }
  public void setShadowVariables(boolean shadow) {
    this.shadowVariables = shadow;
  }
  public void setSkipAllPasses(boolean skipAllPasses) {
    this.skipAllPasses = skipAllPasses;
  }
  public void setSmartNameRemoval(boolean smartNameRemoval) {
    this.smartNameRemoval = smartNameRemoval;
  }
  public void setSourceMapDetailLevel(SourceMap.DetailLevel sourceMapDetailLevel) {
    this.sourceMapDetailLevel = sourceMapDetailLevel;
  }
  public void setSourceMapFormat(SourceMap.Format sourceMapFormat) {
    this.sourceMapFormat = sourceMapFormat;
  }
  public void setSourceMapLocationMappings(List<SourceMap.LocationMapping> sourceMapLocationMappings) {
    this.sourceMapLocationMappings = sourceMapLocationMappings;
  }
  public void setSourceMapOutputPath(String sourceMapOutputPath) {
    this.sourceMapOutputPath = sourceMapOutputPath;
  }
  public void setSpecializeInitialModule(boolean enabled) {
    specializeInitialModule = enabled;
  }
  public void setStripNamePrefixes(Set<String> stripNamePrefixes) {
    this.stripNamePrefixes = stripNamePrefixes;
  }
  public void setStripNameSuffixes(Set<String> stripNameSuffixes) {
    this.stripNameSuffixes = stripNameSuffixes;
  }
  public void setStripTypePrefixes(Set<String> stripTypePrefixes) {
    this.stripTypePrefixes = stripTypePrefixes;
  }
  public void setStripTypes(Set<String> stripTypes) {
    this.stripTypes = stripTypes;
  }
  public void setSummaryDetailLevel(int summaryDetailLevel) {
    this.summaryDetailLevel = summaryDetailLevel;
  }
  public void setSyntheticBlockEndMarker(String syntheticBlockEndMarker) {
    this.syntheticBlockEndMarker = syntheticBlockEndMarker;
  }
  public void setSyntheticBlockStartMarker(String syntheticBlockStartMarker) {
    this.syntheticBlockStartMarker = syntheticBlockStartMarker;
  }
  public void setTightenTypes(boolean tighten) {
    tightenTypes = tighten;
  }
  public void setTracer(TracerMode tracer) {
    this.tracer = tracer;
  }
  public void setTracerMode(TracerMode mode) {
    tracer = mode;
  }
  public void setTransformAMDToCJSModules(boolean transformAMDToCJSModules) {
    this.transformAMDToCJSModules = transformAMDToCJSModules;
  }
  public void setTrustedStrings(boolean yes) {
    trustedStrings = yes;
  }
  public void setTweakProcessing(TweakProcessing tweakProcessing) {
    this.tweakProcessing = tweakProcessing;
  }
  public void setTweakReplacements(Map<String, Object> tweakReplacements) {
    this.tweakReplacements = tweakReplacements;
  }
  public void setTweakToBooleanLiteral(String tweakId, boolean value) {
    tweakReplacements.put(tweakId, new Boolean(value));
  }
  public void setTweakToDoubleLiteral(String tweakId, double value) {
    tweakReplacements.put(tweakId, new Double(value));
  }
  public void setTweakToNumberLiteral(String tweakId, int value) {
    tweakReplacements.put(tweakId, new Integer(value));
  }
  public void setTweakToStringLiteral(String tweakId, String value) {
    tweakReplacements.put(tweakId, value);
  }
  public void setUnaliasableGlobals(String names) {
    unaliasableGlobals = names;
  }
  public void setVariableRenaming(VariableRenamingPolicy variableRenaming) {
    this.variableRenaming = variableRenaming;
  }
  public void setWarningLevel(DiagnosticGroup type, CheckLevel level) {
    addWarningsGuard(new DiagnosticGroupWarningsGuard(type, level));
  }
  public void setWarningsGuard(ComposeWarningsGuard warningsGuard) {
    this.warningsGuard = warningsGuard;
  }
  public void skipAllCompilerPasses() {
    skipAllPasses = true;
  }
  void useEmergencyFailSafe() {
    warningsGuard = warningsGuard.makeEmergencyFailSafeGuard();
  }
  
  public interface AliasTransformation  {
    void addAlias(String alias, String definition);
  }
  
  public interface AliasTransformationHandler  {
    public AliasTransformation logAliasTransformation(String sourceFile, SourcePosition<AliasTransformation> position);
  }
  static enum DevMode {
    OFF(),

    START(),

    START_AND_END(),

    EVERY_PASS(),

  ;
  private DevMode() {
  }
  }
  public static enum LanguageMode {
    ECMASCRIPT3(),

    ECMASCRIPT5(),

    ECMASCRIPT5_STRICT(),

  ;
    public static LanguageMode fromString(String value) {
      if(value.equals("ECMASCRIPT5_STRICT") || value.equals("ES5_STRICT")) {
        return CompilerOptions.LanguageMode.ECMASCRIPT5_STRICT;
      }
      else 
        if(value.equals("ECMASCRIPT5") || value.equals("ES5")) {
          return CompilerOptions.LanguageMode.ECMASCRIPT5;
        }
        else 
          if(value.equals("ECMASCRIPT3") || value.equals("ES3")) {
            return CompilerOptions.LanguageMode.ECMASCRIPT3;
          }
      return null;
    }
  private LanguageMode() {
  }
  }
  
  private static class NullAliasTransformationHandler implements AliasTransformationHandler, Serializable  {
    final private static long serialVersionUID = 0L;
    final private static AliasTransformation NULL_ALIAS_TRANSFORMATION = new NullAliasTransformation();
    @Override() public AliasTransformation logAliasTransformation(String sourceFile, SourcePosition<AliasTransformation> position) {
      position.setItem(NULL_ALIAS_TRANSFORMATION);
      return NULL_ALIAS_TRANSFORMATION;
    }
    
    private static class NullAliasTransformation implements AliasTransformation, Serializable  {
      final private static long serialVersionUID = 0L;
      @Override() public void addAlias(String alias, String definition) {
      }
    }
  }
  public enum Reach {
    ALL(),

    LOCAL_ONLY(),

    NONE(),

  ;
  private Reach() {
  }
  }
  public static enum TracerMode {
    ALL(),

    RAW_SIZE(),

    TIMING_ONLY(),

    OFF(),

  ;
    boolean isOn() {
      return this != OFF;
    }
  private TracerMode() {
  }
  }
  public static enum TweakProcessing {
    OFF(),

    CHECK(),

    STRIP(),

  ;
    public boolean isOn() {
      return this != OFF;
    }
    public boolean shouldStrip() {
      return this == STRIP;
    }
  private TweakProcessing() {
  }
  }
}