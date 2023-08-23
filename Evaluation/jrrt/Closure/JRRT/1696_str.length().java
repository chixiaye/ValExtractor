package com.google.javascript.jscomp;
import com.google.javascript.rhino.Node;

class InlineCostEstimator  {
  final private static String ESTIMATED_IDENTIFIER = "ab";
  final static int ESTIMATED_IDENTIFIER_COST = ESTIMATED_IDENTIFIER.length();
  private InlineCostEstimator() {
    super();
  }
  static int getCost(Node root) {
    return getCost(root, Integer.MAX_VALUE);
  }
  static int getCost(Node root, int costThreshhold) {
    CompiledSizeEstimator estimator = new CompiledSizeEstimator(costThreshhold);
    estimator.add(root);
    return estimator.getCost();
  }
  
  private static class CompiledSizeEstimator extends CodeConsumer  {
    private int maxCost;
    private int cost = 0;
    private char last = '\u0000';
    private boolean continueProcessing = true;
    CompiledSizeEstimator(int costThreshhold) {
      super();
      this.maxCost = costThreshhold;
    }
    @Override() boolean continueProcessing() {
      return continueProcessing;
    }
    @Override() char getLastChar() {
      return last;
    }
    int getCost() {
      return cost;
    }
    void add(Node root) {
      CodeGenerator cg = CodeGenerator.forCostEstimation(this);
      cg.add(root);
    }
    @Override() void addConstant(String newcode) {
      add("0");
    }
    @Override() void addIdentifier(String identifier) {
      add(ESTIMATED_IDENTIFIER);
    }
    @Override() void append(String str) {
      int var_1696 = str.length();
      last = str.charAt(var_1696 - 1);
      cost += str.length();
      if(maxCost <= cost) {
        continueProcessing = false;
      }
    }
  }
}