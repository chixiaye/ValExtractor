package com.google.javascript.jscomp;
import com.google.javascript.jscomp.regex.RegExpTree;
import com.google.javascript.rhino.Node;

class PeepholeSimplifyRegExp extends AbstractPeepholeOptimization  {
  @Override() Node optimizeSubtree(Node subtree) {
    if(subtree.isRegExp()) {
      Node var_470 = subtree.getFirstChild();
      String pattern = var_470.getString();
      String flags = subtree.getChildCount() == 2 ? subtree.getLastChild().getString() : "";
      RegExpTree regexTree;
      try {
        regexTree = RegExpTree.parseRegExp(pattern, flags);
      }
      catch (IllegalArgumentException ex) {
        return subtree;
      }
      regexTree = regexTree.simplify(flags);
      String literal = regexTree.toString();
      String newPattern = literal.substring(1, literal.length() - 1);
      String newFlags = ((flags.contains("g") && (!RegExpTree.matchesWholeInput(regexTree, flags) || regexTree.hasCapturingGroup()) ? "g" : "") + (flags.contains("i") && regexTree.isCaseSensitive() ? "i" : "") + (flags.contains("m") && regexTree.containsAnchor() ? "m" : ""));
      if(!(newPattern.equals(pattern) && newFlags.equals(flags))) {
        subtree.getFirstChild().setString(newPattern);
        if(!"".equals(newFlags)) {
          subtree.getLastChild().setString(newFlags);
        }
        else 
          if(subtree.getChildCount() == 2) {
            subtree.getLastChild().detachFromParent();
          }
        reportCodeChange();
      }
    }
    return subtree;
  }
}