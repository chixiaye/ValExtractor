package org.apache.commons.math3.random;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.ZeroException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

public class ValueServer  {
  final public static int DIGEST_MODE = 0;
  final public static int REPLAY_MODE = 1;
  final public static int UNIFORM_MODE = 2;
  final public static int EXPONENTIAL_MODE = 3;
  final public static int GAUSSIAN_MODE = 4;
  final public static int CONSTANT_MODE = 5;
  private int mode = 5;
  private URL valuesFileURL = null;
  private double mu = 0.0D;
  private double sigma = 0.0D;
  private EmpiricalDistribution empiricalDistribution = null;
  private BufferedReader filePointer = null;
  final private RandomDataImpl randomData;
  public ValueServer() {
    super();
    randomData = new RandomDataImpl();
  }
  @Deprecated() public ValueServer(RandomDataImpl randomData) {
    super();
    this.randomData = randomData;
  }
  public ValueServer(RandomGenerator generator) {
    super();
    this.randomData = new RandomDataImpl(generator);
  }
  public EmpiricalDistribution getEmpiricalDistribution() {
    return empiricalDistribution;
  }
  public URL getValuesFileURL() {
    return valuesFileURL;
  }
  public double getMu() {
    return mu;
  }
  public double getNext() throws IOException, MathIllegalStateException, MathIllegalArgumentException {
    switch (mode){
      case DIGEST_MODE:
      return getNextDigest();
      case REPLAY_MODE:
      return getNextReplay();
      case UNIFORM_MODE:
      return getNextUniform();
      case EXPONENTIAL_MODE:
      return getNextExponential();
      case GAUSSIAN_MODE:
      return getNextGaussian();
      case CONSTANT_MODE:
      return mu;
      default:
      throw new MathIllegalStateException(LocalizedFormats.UNKNOWN_MODE, mode, "DIGEST_MODE", DIGEST_MODE, "REPLAY_MODE", REPLAY_MODE, "UNIFORM_MODE", UNIFORM_MODE, "EXPONENTIAL_MODE", EXPONENTIAL_MODE, "GAUSSIAN_MODE", GAUSSIAN_MODE, "CONSTANT_MODE", CONSTANT_MODE);
    }
  }
  private double getNextDigest() throws MathIllegalStateException {
    if((empiricalDistribution == null) || (empiricalDistribution.getBinStats().size() == 0)) {
      throw new MathIllegalStateException(LocalizedFormats.DIGEST_NOT_INITIALIZED);
    }
    return empiricalDistribution.getNextValue();
  }
  private double getNextExponential() throws MathIllegalArgumentException {
    return randomData.nextExponential(mu);
  }
  private double getNextGaussian() throws MathIllegalArgumentException {
    return randomData.nextGaussian(mu, sigma);
  }
  private double getNextReplay() throws IOException, MathIllegalStateException {
    String str = null;
    if(filePointer == null) {
      resetReplayFile();
    }
    if((str = filePointer.readLine()) == null) {
      closeReplayFile();
      resetReplayFile();
      if((str = filePointer.readLine()) == null) {
        throw new MathIllegalStateException(LocalizedFormats.URL_CONTAINS_NO_DATA, valuesFileURL);
      }
    }
    return Double.parseDouble(str);
  }
  private double getNextUniform() throws MathIllegalArgumentException {
    return randomData.nextUniform(0, 2 * mu);
  }
  public double getSigma() {
    return sigma;
  }
  public double[] fill(int length) throws IOException, MathIllegalStateException, MathIllegalArgumentException {
    double[] out = new double[length];
    for(int i = 0; i < length; i++) {
      out[i] = getNext();
    }
    return out;
  }
  public int getMode() {
    return mode;
  }
  public void closeReplayFile() throws IOException {
    if(filePointer != null) {
      filePointer.close();
      filePointer = null;
    }
  }
  public void computeDistribution() throws IOException, ZeroException, NullArgumentException {
    computeDistribution(EmpiricalDistribution.DEFAULT_BIN_COUNT);
  }
  public void computeDistribution(int binCount) throws NullArgumentException, IOException, ZeroException {
    empiricalDistribution = new EmpiricalDistribution(binCount, randomData);
    empiricalDistribution.load(valuesFileURL);
    mu = empiricalDistribution.getSampleStats().getMean();
    org.apache.commons.math3.stat.descriptive.StatisticalSummary var_3615 = empiricalDistribution.getSampleStats();
    sigma = var_3615.getStandardDeviation();
  }
  public void fill(double[] values) throws IOException, MathIllegalStateException, MathIllegalArgumentException {
    for(int i = 0; i < values.length; i++) {
      values[i] = getNext();
    }
  }
  public void reSeed(long seed) {
    randomData.reSeed(seed);
  }
  public void resetReplayFile() throws IOException {
    if(filePointer != null) {
      try {
        filePointer.close();
        filePointer = null;
      }
      catch (IOException ex) {
      }
    }
    filePointer = new BufferedReader(new InputStreamReader(valuesFileURL.openStream(), "UTF-8"));
  }
  public void setMode(int mode) {
    this.mode = mode;
  }
  public void setMu(double mu) {
    this.mu = mu;
  }
  public void setSigma(double sigma) {
    this.sigma = sigma;
  }
  public void setValuesFileURL(String url) throws MalformedURLException {
    this.valuesFileURL = new URL(url);
  }
  public void setValuesFileURL(URL url) {
    this.valuesFileURL = url;
  }
}