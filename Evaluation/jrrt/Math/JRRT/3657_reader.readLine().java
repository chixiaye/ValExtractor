package org.apache.commons.math3.random;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.apache.commons.math3.exception.MathInternalError;
import org.apache.commons.math3.exception.MathParseException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.util.FastMath;

public class SobolSequenceGenerator implements RandomVectorGenerator  {
  final private static int BITS = 52;
  final private static double SCALE = FastMath.pow(2, BITS);
  final private static int MAX_DIMENSION = 1000;
  final private static String RESOURCE_NAME = "/assets/org/apache/commons/math3/random/new-joe-kuo-6.1000";
  final private static String FILE_CHARSET = "US-ASCII";
  final private int dimension;
  private int count = 0;
  final private long[][] direction;
  final private long[] x;
  public SobolSequenceGenerator(final int dimension) throws OutOfRangeException {
    super();
    if(dimension < 1 || dimension > MAX_DIMENSION) {
      throw new OutOfRangeException(dimension, 1, MAX_DIMENSION);
    }
    final InputStream is = getClass().getResourceAsStream(RESOURCE_NAME);
    if(is == null) {
      throw new MathInternalError();
    }
    this.dimension = dimension;
    direction = new long[dimension][BITS + 1];
    x = new long[dimension];
    try {
      initFromStream(is);
    }
    catch (IOException e) {
      throw new MathInternalError();
    }
    catch (MathParseException e) {
      throw new MathInternalError();
    }
    finally {
      try {
        is.close();
      }
      catch (IOException e) {
      }
    }
  }
  public SobolSequenceGenerator(final int dimension, final InputStream is) throws NotStrictlyPositiveException, MathParseException, IOException {
    super();
    if(dimension < 1) {
      throw new NotStrictlyPositiveException(dimension);
    }
    this.dimension = dimension;
    direction = new long[dimension][BITS + 1];
    x = new long[dimension];
    int lastDimension = initFromStream(is);
    if(lastDimension < dimension) {
      throw new OutOfRangeException(dimension, 1, lastDimension);
    }
  }
  public double[] nextVector() {
    final double[] v = new double[dimension];
    if(count == 0) {
      count++;
      return v;
    }
    int c = 1;
    int value = count - 1;
    while((value & 1) == 1){
      value >>= 1;
      c++;
    }
    for(int i = 0; i < dimension; i++) {
      x[i] = x[i] ^ direction[i][c];
      v[i] = (double)x[i] / SCALE;
    }
    count++;
    return v;
  }
  public double[] skipTo(final int index) throws NotPositiveException {
    if(index == 0) {
      Arrays.fill(x, 0);
    }
    else {
      final int i = index - 1;
      final long grayCode = i ^ (i >> 1);
      for(int j = 0; j < dimension; j++) {
        long result = 0;
        for(int k = 1; k <= BITS; k++) {
          final long shift = grayCode >> (k - 1);
          if(shift == 0) {
            break ;
          }
          final long ik = shift & 1;
          result ^= ik * direction[j][k];
        }
        x[j] = result;
      }
    }
    count = index;
    return nextVector();
  }
  public int getNextIndex() {
    return count;
  }
  private int initFromStream(final InputStream is) throws MathParseException, IOException {
    for(int i = 1; i <= BITS; i++) {
      direction[0][i] = 1L << (BITS - i);
    }
    final Charset charset = Charset.forName(FILE_CHARSET);
    final BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
    int dim = -1;
    try {
      String var_3657 = reader.readLine();
      int lineNumber = 2;
      int index = 1;
      String line = null;
      while((line = reader.readLine()) != null){
        StringTokenizer st = new StringTokenizer(line, " ");
        try {
          dim = Integer.parseInt(st.nextToken());
          if(dim >= 2 && dim <= dimension) {
            final int s = Integer.parseInt(st.nextToken());
            final int a = Integer.parseInt(st.nextToken());
            final int[] m = new int[s + 1];
            for(int i = 1; i <= s; i++) {
              m[i] = Integer.parseInt(st.nextToken());
            }
            initDirectionVector(index++, a, m);
          }
          if(dim > dimension) {
            return dim;
          }
        }
        catch (NoSuchElementException e) {
          throw new MathParseException(line, lineNumber);
        }
        catch (NumberFormatException e) {
          throw new MathParseException(line, lineNumber);
        }
        lineNumber++;
      }
    }
    finally {
      reader.close();
    }
    return dim;
  }
  private void initDirectionVector(final int d, final int a, final int[] m) {
    final int s = m.length - 1;
    for(int i = 1; i <= s; i++) {
      direction[d][i] = ((long)m[i]) << (BITS - i);
    }
    for(int i = s + 1; i <= BITS; i++) {
      direction[d][i] = direction[d][i - s] ^ (direction[d][i - s] >> s);
      for(int k = 1; k <= s - 1; k++) {
        direction[d][i] ^= ((a >> (s - 1 - k)) & 1) * direction[d][i - k];
      }
    }
  }
}