package com.google.javascript.jscomp;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.javascript.jscomp.graph.LatticeElement;
import java.util.List;
interface JoinOp<L extends com.google.javascript.jscomp.graph.LatticeElement> extends Function<List<L>, L>  {
  abstract static class BinaryJoinOp<L extends com.google.javascript.jscomp.graph.LatticeElement> implements JoinOp<L>  {
    abstract L apply(L latticeA, L latticeB);
    @Override() final public L apply(List<L> values) {
      Preconditions.checkArgument(!values.isEmpty());
      int size = values.size();
      if(size == 1) {
        L var_1037 = values.get(0);
        return var_1037;
      }
      else 
        if(size == 2) {
          return apply(values.get(0), values.get(1));
        }
        else {
          int mid = computeMidPoint(size);
          return apply(apply(values.subList(0, mid)), apply(values.subList(mid, size)));
        }
    }
    static int computeMidPoint(int size) {
      int midpoint = size >>> 1;
      if(size > 4) {
        midpoint &= -2;
      }
      return midpoint;
    }
  }
}