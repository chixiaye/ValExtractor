package org.apache.commons.math3.geometry.euclidean.twod;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.math3.exception.MathInternalError;
import org.apache.commons.math3.geometry.euclidean.oned.Euclidean1D;
import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.apache.commons.math3.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.apache.commons.math3.geometry.partitioning.AbstractRegion;
import org.apache.commons.math3.geometry.partitioning.AbstractSubHyperplane;
import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.apache.commons.math3.geometry.partitioning.BSPTreeVisitor;
import org.apache.commons.math3.geometry.partitioning.BoundaryAttribute;
import org.apache.commons.math3.geometry.partitioning.Side;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;
import org.apache.commons.math3.geometry.partitioning.utilities.AVLTree;
import org.apache.commons.math3.geometry.partitioning.utilities.OrderedTuple;
import org.apache.commons.math3.util.FastMath;

public class PolygonsSet extends AbstractRegion<Euclidean2D, Euclidean1D>  {
  private Vector2D[][] vertices;
  public PolygonsSet() {
    super();
  }
  public PolygonsSet(final BSPTree<Euclidean2D> tree) {
    super(tree);
  }
  public PolygonsSet(final Collection<SubHyperplane<Euclidean2D>> boundary) {
    super(boundary);
  }
  public PolygonsSet(final double hyperplaneThickness, final Vector2D ... vertices) {
    super(verticesToTree(hyperplaneThickness, vertices));
  }
  public PolygonsSet(final double xMin, final double xMax, final double yMin, final double yMax) {
    super(boxBoundary(xMin, xMax, yMin, yMax));
  }
  private static BSPTree<Euclidean2D> verticesToTree(final double hyperplaneThickness, final Vector2D ... vertices) {
    final int n = vertices.length;
    if(n == 0) {
      return new BSPTree<Euclidean2D>(Boolean.TRUE);
    }
    final Vertex[] vArray = new Vertex[n];
    for(int i = 0; i < n; ++i) {
      vArray[i] = new Vertex(vertices[i]);
    }
    List<Edge> edges = new ArrayList<Edge>(n);
    for(int i = 0; i < n; ++i) {
      final Vertex start = vArray[i];
      final Vertex end = vArray[(i + 1) % n];
      Line line = start.sharedLineWith(end);
      if(line == null) {
        line = new Line(start.getLocation(), end.getLocation());
      }
      edges.add(new Edge(start, end, line));
      for (final Vertex vertex : vArray) {
        if(vertex != start && vertex != end && FastMath.abs(line.getOffset(vertex.getLocation())) <= hyperplaneThickness) {
          vertex.bindWith(line);
        }
      }
    }
    final BSPTree<Euclidean2D> tree = new BSPTree<Euclidean2D>();
    insertEdges(hyperplaneThickness, tree, edges);
    return tree;
  }
  private static Line[] boxBoundary(final double xMin, final double xMax, final double yMin, final double yMax) {
    final Vector2D minMin = new Vector2D(xMin, yMin);
    final Vector2D minMax = new Vector2D(xMin, yMax);
    final Vector2D maxMin = new Vector2D(xMax, yMin);
    final Vector2D maxMax = new Vector2D(xMax, yMax);
    return new Line[]{ new Line(minMin, maxMin), new Line(maxMin, maxMax), new Line(maxMax, minMax), new Line(minMax, minMin) } ;
  }
  private List<ComparableSegment> followLoop(final AVLTree<ComparableSegment>.Node node, final AVLTree<ComparableSegment> sorted) {
    final ArrayList<ComparableSegment> loop = new ArrayList<ComparableSegment>();
    ComparableSegment segment = node.getElement();
    loop.add(segment);
    final Vector2D globalStart = segment.getStart();
    Vector2D end = segment.getEnd();
    node.delete();
    final boolean open = segment.getStart() == null;
    while((end != null) && (open || (globalStart.distance(end) > 1.0e-10D))){
      AVLTree<ComparableSegment>.Node selectedNode = null;
      ComparableSegment selectedSegment = null;
      double selectedDistance = Double.POSITIVE_INFINITY;
      final ComparableSegment lowerLeft = new ComparableSegment(end, -1.0e-10D, -1.0e-10D);
      final ComparableSegment upperRight = new ComparableSegment(end, +1.0e-10D, +1.0e-10D);
      for(org.apache.commons.math3.geometry.partitioning.utilities.AVLTree<org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet.ComparableSegment>.Node n = sorted.getNotSmaller(lowerLeft); (n != null) && (n.getElement().compareTo(upperRight) <= 0); n = n.getNext()) {
        segment = n.getElement();
        final double distance = end.distance(segment.getStart());
        if(distance < selectedDistance) {
          selectedNode = n;
          selectedSegment = segment;
          selectedDistance = distance;
        }
      }
      if(selectedDistance > 1.0e-10D) {
        return null;
      }
      end = selectedSegment.getEnd();
      loop.add(selectedSegment);
      selectedNode.delete();
    }
    if((loop.size() == 2) && !open) {
      return null;
    }
    if((end == null) && !open) {
      throw new MathInternalError();
    }
    return loop;
  }
  @Override() public PolygonsSet buildNew(final BSPTree<Euclidean2D> tree) {
    return new PolygonsSet(tree);
  }
  public Vector2D[][] getVertices() {
    if(vertices == null) {
      if(getTree(false).getCut() == null) {
        vertices = new Vector2D[0][];
      }
      else {
        final SegmentsBuilder visitor = new SegmentsBuilder();
        getTree(true).visit(visitor);
        final AVLTree<ComparableSegment> sorted = visitor.getSorted();
        final ArrayList<List<ComparableSegment>> loops = new ArrayList<List<ComparableSegment>>();
        while(!sorted.isEmpty()){
          final AVLTree<ComparableSegment>.Node node = sorted.getSmallest();
          final List<ComparableSegment> loop = followLoop(node, sorted);
          if(loop != null) {
            loops.add(loop);
          }
        }
        vertices = new Vector2D[loops.size()][];
        int i = 0;
        for (final List<ComparableSegment> loop : loops) {
          if(loop.size() < 2) {
            final Line line = loop.get(0).getLine();
            vertices[i++] = new Vector2D[]{ null, line.toSpace(new Vector1D(-Float.MAX_VALUE)), line.toSpace(new Vector1D(+Float.MAX_VALUE)) } ;
          }
          else 
            if(loop.get(0).getStart() == null) {
              final Vector2D[] array = new Vector2D[loop.size() + 2];
              int j = 0;
              for (Segment segment : loop) {
                if(j == 0) {
                  double x = segment.getLine().toSubSpace(segment.getEnd()).getX();
                  x -= FastMath.max(1.0D, FastMath.abs(x / 2));
                  array[j++] = null;
                  array[j++] = segment.getLine().toSpace(new Vector1D(x));
                }
                if(j < (array.length - 1)) {
                  array[j++] = segment.getEnd();
                }
                if(j == (array.length - 1)) {
                  double x = segment.getLine().toSubSpace(segment.getStart()).getX();
                  x += FastMath.max(1.0D, FastMath.abs(x / 2));
                  array[j++] = segment.getLine().toSpace(new Vector1D(x));
                }
              }
              vertices[i++] = array;
            }
            else {
              final Vector2D[] array = new Vector2D[loop.size()];
              int j = 0;
              for (Segment segment : loop) {
                array[j++] = segment.getStart();
              }
              vertices[i++] = array;
            }
        }
      }
    }
    return vertices.clone();
  }
  @Override() protected void computeGeometricalProperties() {
    final Vector2D[][] v = getVertices();
    if(v.length == 0) {
      final BSPTree<Euclidean2D> tree = getTree(false);
      if(tree.getCut() == null && (Boolean)tree.getAttribute()) {
        setSize(Double.POSITIVE_INFINITY);
        setBarycenter(Vector2D.NaN);
      }
      else {
        setSize(0);
        setBarycenter(new Vector2D(0, 0));
      }
    }
    else 
      if(v[0][0] == null) {
        setSize(Double.POSITIVE_INFINITY);
        setBarycenter(Vector2D.NaN);
      }
      else {
        double sum = 0;
        double sumX = 0;
        double sumY = 0;
        for (Vector2D[] loop : v) {
          double x1 = loop[loop.length - 1].getX();
          double y1 = loop[loop.length - 1].getY();
          for (final Vector2D point : loop) {
            final double x0 = x1;
            final double y0 = y1;
            x1 = point.getX();
            y1 = point.getY();
            final double factor = x0 * y1 - y0 * x1;
            sum += factor;
            sumX += factor * (x0 + x1);
            sumY += factor * (y0 + y1);
          }
        }
        if(sum < 0) {
          setSize(Double.POSITIVE_INFINITY);
          setBarycenter(Vector2D.NaN);
        }
        else {
          setSize(sum / 2);
          setBarycenter(new Vector2D(sumX / (3 * sum), sumY / (3 * sum)));
        }
      }
  }
  private static void insertEdges(final double hyperplaneThickness, final BSPTree<Euclidean2D> node, final List<Edge> edges) {
    int index = 0;
    Edge inserted = null;
    while(inserted == null && index < edges.size()){
      inserted = edges.get(index++);
      if(inserted.getNode() == null) {
        Line var_1621 = inserted.getLine();
        if(node.insertCut(var_1621)) {
          inserted.setNode(node);
        }
        else {
          inserted = null;
        }
      }
      else {
        inserted = null;
      }
    }
    if(inserted == null) {
      final BSPTree<Euclidean2D> parent = node.getParent();
      if(parent == null || node == parent.getMinus()) {
        node.setAttribute(Boolean.TRUE);
      }
      else {
        node.setAttribute(Boolean.FALSE);
      }
      return ;
    }
    final List<Edge> plusList = new ArrayList<Edge>();
    final List<Edge> minusList = new ArrayList<Edge>();
    for (final Edge edge : edges) {
      if(edge != inserted) {
        final double startOffset = inserted.getLine().getOffset(edge.getStart().getLocation());
        final double endOffset = inserted.getLine().getOffset(edge.getEnd().getLocation());
        Side startSide = (FastMath.abs(startOffset) <= hyperplaneThickness) ? Side.HYPER : ((startOffset < 0) ? Side.MINUS : Side.PLUS);
        Side endSide = (FastMath.abs(endOffset) <= hyperplaneThickness) ? Side.HYPER : ((endOffset < 0) ? Side.MINUS : Side.PLUS);
        switch (startSide){
          case PLUS:
          if(endSide == Side.MINUS) {
            final Vertex splitPoint = edge.split(inserted.getLine());
            minusList.add(splitPoint.getOutgoing());
            plusList.add(splitPoint.getIncoming());
          }
          else {
            plusList.add(edge);
          }
          break ;
          case MINUS:
          if(endSide == Side.PLUS) {
            final Vertex splitPoint = edge.split(inserted.getLine());
            minusList.add(splitPoint.getIncoming());
            plusList.add(splitPoint.getOutgoing());
          }
          else {
            minusList.add(edge);
          }
          break ;
          default:
          if(endSide == Side.PLUS) {
            plusList.add(edge);
          }
          else 
            if(endSide == Side.MINUS) {
              minusList.add(edge);
            }
          break ;
        }
      }
    }
    if(!plusList.isEmpty()) {
      insertEdges(hyperplaneThickness, node.getPlus(), plusList);
    }
    else {
      node.getPlus().setAttribute(Boolean.FALSE);
    }
    if(!minusList.isEmpty()) {
      insertEdges(hyperplaneThickness, node.getMinus(), minusList);
    }
    else {
      node.getMinus().setAttribute(Boolean.TRUE);
    }
  }
  
  private static class ComparableSegment extends Segment implements Comparable<ComparableSegment>  {
    private OrderedTuple sortingKey;
    public ComparableSegment(final Vector2D start, final Vector2D end, final Line line) {
      super(start, end, line);
      sortingKey = (start == null) ? new OrderedTuple(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY) : new OrderedTuple(start.getX(), start.getY());
    }
    public ComparableSegment(final Vector2D start, final double dx, final double dy) {
      super(null, null, null);
      sortingKey = new OrderedTuple(start.getX() + dx, start.getY() + dy);
    }
    @Override() public boolean equals(final Object other) {
      if(this == other) {
        return true;
      }
      else 
        if(other instanceof ComparableSegment) {
          return compareTo((ComparableSegment)other) == 0;
        }
        else {
          return false;
        }
    }
    public int compareTo(final ComparableSegment o) {
      return sortingKey.compareTo(o.sortingKey);
    }
    @Override() public int hashCode() {
      return getStart().hashCode() ^ getEnd().hashCode() ^ getLine().hashCode() ^ sortingKey.hashCode();
    }
  }
  
  private static class Edge  {
    final private Vertex start;
    final private Vertex end;
    final private Line line;
    private BSPTree<Euclidean2D> node;
    public Edge(final Vertex start, final Vertex end, final Line line) {
      super();
      this.start = start;
      this.end = end;
      this.line = line;
      this.node = null;
      start.setOutgoing(this);
      end.setIncoming(this);
    }
    public BSPTree<Euclidean2D> getNode() {
      return node;
    }
    public Line getLine() {
      return line;
    }
    public Vertex getEnd() {
      return end;
    }
    public Vertex getStart() {
      return start;
    }
    public Vertex split(final Line splitLine) {
      final Vertex splitVertex = new Vertex(line.intersection(splitLine));
      splitVertex.bindWith(splitLine);
      final Edge startHalf = new Edge(start, splitVertex, line);
      final Edge endHalf = new Edge(splitVertex, end, line);
      startHalf.node = node;
      endHalf.node = node;
      return splitVertex;
    }
    public void setNode(final BSPTree<Euclidean2D> node) {
      this.node = node;
    }
  }
  
  private static class SegmentsBuilder implements BSPTreeVisitor<Euclidean2D>  {
    private AVLTree<ComparableSegment> sorted;
    public SegmentsBuilder() {
      super();
      sorted = new AVLTree<ComparableSegment>();
    }
    public AVLTree<ComparableSegment> getSorted() {
      return sorted;
    }
    public Order visitOrder(final BSPTree<Euclidean2D> node) {
      return Order.MINUS_SUB_PLUS;
    }
    private void addContribution(final SubHyperplane<Euclidean2D> sub, final boolean reversed) {
      @SuppressWarnings(value = {"unchecked", }) final AbstractSubHyperplane<Euclidean2D, Euclidean1D> absSub = (AbstractSubHyperplane<Euclidean2D, Euclidean1D>)sub;
      final Line line = (Line)sub.getHyperplane();
      final List<Interval> intervals = ((IntervalsSet)absSub.getRemainingRegion()).asList();
      for (final Interval i : intervals) {
        final Vector2D start = Double.isInfinite(i.getInf()) ? null : (Vector2D)line.toSpace(new Vector1D(i.getInf()));
        final Vector2D end = Double.isInfinite(i.getSup()) ? null : (Vector2D)line.toSpace(new Vector1D(i.getSup()));
        if(reversed) {
          sorted.insert(new ComparableSegment(end, start, line.getReverse()));
        }
        else {
          sorted.insert(new ComparableSegment(start, end, line));
        }
      }
    }
    public void visitInternalNode(final BSPTree<Euclidean2D> node) {
      @SuppressWarnings(value = {"unchecked", }) final BoundaryAttribute<Euclidean2D> attribute = (BoundaryAttribute<Euclidean2D>)node.getAttribute();
      if(attribute.getPlusOutside() != null) {
        addContribution(attribute.getPlusOutside(), false);
      }
      if(attribute.getPlusInside() != null) {
        addContribution(attribute.getPlusInside(), true);
      }
    }
    public void visitLeafNode(final BSPTree<Euclidean2D> node) {
    }
  }
  
  private static class Vertex  {
    final private Vector2D location;
    private Edge incoming;
    private Edge outgoing;
    final private List<Line> lines;
    public Vertex(final Vector2D location) {
      super();
      this.location = location;
      this.incoming = null;
      this.outgoing = null;
      this.lines = new ArrayList<Line>();
    }
    public Edge getIncoming() {
      return incoming;
    }
    public Edge getOutgoing() {
      return outgoing;
    }
    public Line sharedLineWith(final Vertex vertex) {
      for (final Line line1 : lines) {
        for (final Line line2 : vertex.lines) {
          if(line1 == line2) {
            return line1;
          }
        }
      }
      return null;
    }
    public Vector2D getLocation() {
      return location;
    }
    public void bindWith(final Line line) {
      lines.add(line);
    }
    public void setIncoming(final Edge incoming) {
      this.incoming = incoming;
      bindWith(incoming.getLine());
    }
    public void setOutgoing(final Edge outgoing) {
      this.outgoing = outgoing;
      bindWith(outgoing.getLine());
    }
  }
}