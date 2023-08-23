package org.apache.commons.math3.random;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.MathInternalError;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.ZeroException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;

public class EmpiricalDistribution extends AbstractRealDistribution  {
  final public static int DEFAULT_BIN_COUNT = 1000;
  final private static String FILE_CHARSET = "US-ASCII";
  final private static long serialVersionUID = 5729073523949762654L;
  final protected RandomDataGenerator randomData;
  final private List<SummaryStatistics> binStats;
  private SummaryStatistics sampleStats = null;
  private double max = Double.NEGATIVE_INFINITY;
  private double min = Double.POSITIVE_INFINITY;
  private double delta = 0D;
  final private int binCount;
  private boolean loaded = false;
  private double[] upperBounds = null;
  public EmpiricalDistribution() {
    this(DEFAULT_BIN_COUNT);
  }
  @Deprecated() public EmpiricalDistribution(RandomDataImpl randomData) {
    this(DEFAULT_BIN_COUNT, randomData);
  }
  public EmpiricalDistribution(RandomGenerator generator) {
    this(DEFAULT_BIN_COUNT, generator);
  }
  public EmpiricalDistribution(int binCount) {
    this(binCount, new RandomDataGenerator());
  }
  private EmpiricalDistribution(int binCount, RandomDataGenerator randomData) {
    super(null);
    this.binCount = binCount;
    this.randomData = randomData;
    binStats = new ArrayList<SummaryStatistics>();
  }
  @Deprecated() public EmpiricalDistribution(int binCount, RandomDataImpl randomData) {
    this(binCount, randomData.getDelegate());
  }
  public EmpiricalDistribution(int binCount, RandomGenerator generator) {
    this(binCount, new RandomDataGenerator(generator));
  }
  public List<SummaryStatistics> getBinStats() {
    return binStats;
  }
  protected RealDistribution getKernel(SummaryStatistics bStats) {
    return new NormalDistribution(randomData.getRandomGenerator(), bStats.getMean(), bStats.getStandardDeviation(), NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
  }
  private RealDistribution k(double x) {
    final int binIndex = findBin(x);
    return getKernel(binStats.get(binIndex));
  }
  public StatisticalSummary getSampleStats() {
    return sampleStats;
  }
  public boolean isLoaded() {
    return loaded;
  }
  public boolean isSupportConnected() {
    return true;
  }
  public boolean isSupportLowerBoundInclusive() {
    return true;
  }
  public boolean isSupportUpperBoundInclusive() {
    return true;
  }
  private double cumBinP(int binIndex) {
    return upperBounds[binIndex];
  }
  public double cumulativeProbability(double x) {
    if(x < min) {
      return 0D;
    }
    else 
      if(x >= max) {
        return 1D;
      }
    final int binIndex = findBin(x);
    final double pBminus = pBminus(binIndex);
    final double pB = pB(binIndex);
    final double[] binBounds = getUpperBounds();
    final double kB = kB(binIndex);
    final double lower = binIndex == 0 ? min : binBounds[binIndex - 1];
    final RealDistribution kernel = k(x);
    final double withinBinCum = (kernel.cumulativeProbability(x) - kernel.cumulativeProbability(lower)) / kB;
    return pBminus + pB * withinBinCum;
  }
  public double density(double x) {
    if(x < min || x > max) {
      return 0D;
    }
    final int binIndex = findBin(x);
    final RealDistribution kernel = getKernel(binStats.get(binIndex));
    return kernel.density(x) * pB(binIndex) / kB(binIndex);
  }
  public double getNextValue() throws MathIllegalStateException {
    if(!loaded) {
      throw new MathIllegalStateException(LocalizedFormats.DISTRIBUTION_NOT_LOADED);
    }
    final double x = randomData.nextUniform(0, 1);
    for(int i = 0; i < binCount; i++) {
      if(x <= upperBounds[i]) {
        SummaryStatistics stats = binStats.get(i);
        if(stats.getN() > 0) {
          if(stats.getStandardDeviation() > 0) {
            return getKernel(stats).sample();
          }
          else {
            return stats.getMean();
          }
        }
      }
    }
    throw new MathIllegalStateException(LocalizedFormats.NO_BIN_SELECTED);
  }
  public double getNumericalMean() {
    return sampleStats.getMean();
  }
  public double getNumericalVariance() {
    return sampleStats.getVariance();
  }
  public double getSupportLowerBound() {
    return min;
  }
  public double getSupportUpperBound() {
    return max;
  }
  @Override() public double inverseCumulativeProbability(final double p) throws OutOfRangeException {
    if(p < 0.0D || p > 1.0D) {
      throw new OutOfRangeException(p, 0, 1);
    }
    if(p == 0.0D) {
      return getSupportLowerBound();
    }
    if(p == 1.0D) {
      return getSupportUpperBound();
    }
    int i = 0;
    while(cumBinP(i) < p){
      i++;
    }
    final RealDistribution kernel = getKernel(binStats.get(i));
    final double kB = kB(i);
    final double[] binBounds = getUpperBounds();
    final double lower = i == 0 ? min : binBounds[i - 1];
    final double kBminus = kernel.cumulativeProbability(lower);
    final double pB = pB(i);
    final double pBminus = pBminus(i);
    final double pCrit = p - pBminus;
    if(pCrit <= 0) {
      return lower;
    }
    return kernel.inverseCumulativeProbability(kBminus + pCrit * kB / pB);
  }
  @SuppressWarnings(value = {"deprecation", }) private double kB(int i) {
    final double[] binBounds = getUpperBounds();
    final RealDistribution kernel = getKernel(binStats.get(i));
    return i == 0 ? kernel.cumulativeProbability(min, binBounds[0]) : kernel.cumulativeProbability(binBounds[i - 1], binBounds[i]);
  }
  private double pB(int i) {
    return i == 0 ? upperBounds[0] : upperBounds[i] - upperBounds[i - 1];
  }
  private double pBminus(int i) {
    return i == 0 ? 0 : upperBounds[i - 1];
  }
  @Override() public double probability(double x) {
    return 0;
  }
  @Override() public double sample() {
    return getNextValue();
  }
  public double[] getGeneratorUpperBounds() {
    int len = upperBounds.length;
    double[] out = new double[len];
    System.arraycopy(upperBounds, 0, out, 0, len);
    return out;
  }
  public double[] getUpperBounds() {
    double[] binUpperBounds = new double[binCount];
    for(int i = 0; i < binCount - 1; i++) {
      binUpperBounds[i] = min + delta * (i + 1);
    }
    binUpperBounds[binCount - 1] = max;
    return binUpperBounds;
  }
  private int findBin(double value) {
    return FastMath.min(FastMath.max((int)FastMath.ceil((value - min) / delta) - 1, 0), binCount - 1);
  }
  public int getBinCount() {
    return binCount;
  }
  private void fillBinStats(final DataAdapter da) throws IOException {
    min = sampleStats.getMin();
    max = sampleStats.getMax();
    delta = (max - min) / ((double)binCount);
    if(!binStats.isEmpty()) {
      binStats.clear();
    }
    for(int i = 0; i < binCount; i++) {
      SummaryStatistics stats = new SummaryStatistics();
      binStats.add(i, stats);
    }
    da.computeBinStats();
    upperBounds = new double[binCount];
    upperBounds[0] = ((double)binStats.get(0).getN()) / (double)sampleStats.getN();
    for(int i = 1; i < binCount - 1; i++) {
      upperBounds[i] = upperBounds[i - 1] + ((double)binStats.get(i).getN()) / (double)sampleStats.getN();
    }
    upperBounds[binCount - 1] = 1.0D;
  }
  public void load(double[] in) throws NullArgumentException {
    DataAdapter da = new ArrayDataAdapter(in);
    try {
      da.computeStats();
      fillBinStats(new ArrayDataAdapter(in));
    }
    catch (IOException ex) {
      throw new MathInternalError();
    }
    loaded = true;
  }
  public void load(File file) throws IOException, NullArgumentException {
    MathUtils.checkNotNull(file);
    Charset charset = Charset.forName(FILE_CHARSET);
    InputStream is = new FileInputStream(file);
    BufferedReader in = new BufferedReader(new InputStreamReader(is, charset));
    try {
      DataAdapter da = new StreamDataAdapter(in);
      da.computeStats();
      is = new FileInputStream(file);
      in = new BufferedReader(new InputStreamReader(is, charset));
      fillBinStats(new StreamDataAdapter(in));
      loaded = true;
    }
    finally {
      try {
        in.close();
      }
      catch (IOException ex) {
      }
    }
  }
  public void load(URL url) throws IOException, NullArgumentException, ZeroException {
    MathUtils.checkNotNull(url);
    Charset charset = Charset.forName(FILE_CHARSET);
    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), charset));
    try {
      DataAdapter da = new StreamDataAdapter(in);
      da.computeStats();
      if(sampleStats.getN() == 0) {
        throw new ZeroException(LocalizedFormats.URL_CONTAINS_NO_DATA, url);
      }
      in = new BufferedReader(new InputStreamReader(url.openStream(), charset));
      fillBinStats(new StreamDataAdapter(in));
      loaded = true;
    }
    finally {
      try {
        in.close();
      }
      catch (IOException ex) {
      }
    }
  }
  public void reSeed(long seed) {
    randomData.reSeed(seed);
  }
  @Override() public void reseedRandomGenerator(long seed) {
    randomData.reSeed(seed);
  }
  
  private class ArrayDataAdapter extends DataAdapter  {
    private double[] inputArray;
    public ArrayDataAdapter(double[] in) throws NullArgumentException {
      super();
      MathUtils.checkNotNull(in);
      inputArray = in;
    }
    @Override() public void computeBinStats() throws IOException {
      for(int i = 0; i < inputArray.length; i++) {
        double var_3613 = inputArray[i];
        SummaryStatistics stats = binStats.get(findBin(var_3613));
        stats.addValue(inputArray[i]);
      }
    }
    @Override() public void computeStats() throws IOException {
      sampleStats = new SummaryStatistics();
      for(int i = 0; i < inputArray.length; i++) {
        sampleStats.addValue(inputArray[i]);
      }
    }
  }
  
  abstract private class DataAdapter  {
    abstract public void computeBinStats() throws IOException;
    abstract public void computeStats() throws IOException;
  }
  
  private class StreamDataAdapter extends DataAdapter  {
    private BufferedReader inputStream;
    public StreamDataAdapter(BufferedReader in) {
      super();
      inputStream = in;
    }
    @Override() public void computeBinStats() throws IOException {
      String str = null;
      double val = 0.0D;
      while((str = inputStream.readLine()) != null){
        val = Double.parseDouble(str);
        SummaryStatistics stats = binStats.get(findBin(val));
        stats.addValue(val);
      }
      inputStream.close();
      inputStream = null;
    }
    @Override() public void computeStats() throws IOException {
      String str = null;
      double val = 0.0D;
      sampleStats = new SummaryStatistics();
      while((str = inputStream.readLine()) != null){
        val = Double.parseDouble(str);
        sampleStats.addValue(val);
      }
      inputStream.close();
      inputStream = null;
    }
  }
}