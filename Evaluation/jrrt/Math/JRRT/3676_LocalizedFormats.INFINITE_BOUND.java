package org.apache.commons.math3.random;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Collection;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.CauchyDistribution;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.apache.commons.math3.distribution.PascalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.exception.MathInternalError;
import org.apache.commons.math3.exception.NotANumberException;
import org.apache.commons.math3.exception.NotFiniteNumberException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.MathArrays;

public class RandomDataGenerator implements RandomData, Serializable  {
  final private static long serialVersionUID = -626730818244969716L;
  private RandomGenerator rand = null;
  private RandomGenerator secRand = null;
  public RandomDataGenerator() {
    super();
  }
  public RandomDataGenerator(RandomGenerator rand) {
    super();
    this.rand = rand;
  }
  public Object[] nextSample(Collection<?> c, int k) throws NumberIsTooLargeException, NotStrictlyPositiveException {
    int len = c.size();
    if(k > len) {
      throw new NumberIsTooLargeException(LocalizedFormats.SAMPLE_SIZE_EXCEEDS_COLLECTION_SIZE, k, len, true);
    }
    if(k <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES, k);
    }
    Object[] objects = c.toArray();
    int[] index = nextPermutation(len, k);
    Object[] result = new Object[k];
    for(int i = 0; i < k; i++) {
      result[i] = objects[index[i]];
    }
    return result;
  }
  public RandomGenerator getRandomGenerator() {
    if(rand == null) {
      initRan();
    }
    return rand;
  }
  private RandomGenerator getSecRan() {
    if(secRand == null) {
      secRand = RandomGeneratorFactory.createRandomGenerator(new SecureRandom());
      secRand.setSeed(System.currentTimeMillis() + System.identityHashCode(this));
    }
    return secRand;
  }
  public String nextHexString(int len) throws NotStrictlyPositiveException {
    if(len <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.LENGTH, len);
    }
    RandomGenerator ran = getRandomGenerator();
    StringBuilder outBuffer = new StringBuilder();
    byte[] randomBytes = new byte[(len / 2) + 1];
    ran.nextBytes(randomBytes);
    for(int i = 0; i < randomBytes.length; i++) {
      Integer c = Integer.valueOf(randomBytes[i]);
      String hex = Integer.toHexString(c.intValue() + 128);
      if(hex.length() == 1) {
        hex = "0" + hex;
      }
      outBuffer.append(hex);
    }
    return outBuffer.toString().substring(0, len);
  }
  public String nextSecureHexString(int len) throws NotStrictlyPositiveException {
    if(len <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.LENGTH, len);
    }
    final RandomGenerator secRan = getSecRan();
    MessageDigest alg = null;
    try {
      alg = MessageDigest.getInstance("SHA-1");
    }
    catch (NoSuchAlgorithmException ex) {
      throw new MathInternalError(ex);
    }
    alg.reset();
    int numIter = (len / 40) + 1;
    StringBuilder outBuffer = new StringBuilder();
    for(int iter = 1; iter < numIter + 1; iter++) {
      byte[] randomBytes = new byte[40];
      secRan.nextBytes(randomBytes);
      alg.update(randomBytes);
      byte[] hash = alg.digest();
      for(int i = 0; i < hash.length; i++) {
        Integer c = Integer.valueOf(hash[i]);
        String hex = Integer.toHexString(c.intValue() + 128);
        if(hex.length() == 1) {
          hex = "0" + hex;
        }
        outBuffer.append(hex);
      }
    }
    return outBuffer.toString().substring(0, len);
  }
  public double nextBeta(double alpha, double beta) {
    return new BetaDistribution(getRandomGenerator(), alpha, beta, BetaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY).sample();
  }
  public double nextCauchy(double median, double scale) {
    return new CauchyDistribution(getRandomGenerator(), median, scale, CauchyDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY).sample();
  }
  public double nextChiSquare(double df) {
    return new ChiSquaredDistribution(getRandomGenerator(), df, ChiSquaredDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY).sample();
  }
  public double nextExponential(double mean) throws NotStrictlyPositiveException {
    return new ExponentialDistribution(getRandomGenerator(), mean, ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY).sample();
  }
  public double nextF(double numeratorDf, double denominatorDf) throws NotStrictlyPositiveException {
    return new FDistribution(getRandomGenerator(), numeratorDf, denominatorDf, FDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY).sample();
  }
  public double nextGamma(double shape, double scale) throws NotStrictlyPositiveException {
    return new GammaDistribution(getRandomGenerator(), shape, scale, GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY).sample();
  }
  public double nextGaussian(double mu, double sigma) throws NotStrictlyPositiveException {
    if(sigma <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.STANDARD_DEVIATION, sigma);
    }
    return sigma * getRandomGenerator().nextGaussian() + mu;
  }
  public double nextT(double df) throws NotStrictlyPositiveException {
    return new TDistribution(getRandomGenerator(), df, TDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY).sample();
  }
  public double nextUniform(double lower, double upper) throws NumberIsTooLargeException, NotFiniteNumberException, NotANumberException {
    return nextUniform(lower, upper, false);
  }
  public double nextUniform(double lower, double upper, boolean lowerInclusive) throws NumberIsTooLargeException, NotFiniteNumberException, NotANumberException {
    if(lower >= upper) {
      throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND, lower, upper, false);
    }
    if(Double.isInfinite(lower)) {
      LocalizedFormats var_3676 = LocalizedFormats.INFINITE_BOUND;
      throw new NotFiniteNumberException(var_3676, lower);
    }
    if(Double.isInfinite(upper)) {
      throw new NotFiniteNumberException(LocalizedFormats.INFINITE_BOUND, upper);
    }
    if(Double.isNaN(lower) || Double.isNaN(upper)) {
      throw new NotANumberException();
    }
    final RandomGenerator generator = getRandomGenerator();
    double u = generator.nextDouble();
    while(!lowerInclusive && u <= 0.0D){
      u = generator.nextDouble();
    }
    return u * upper + (1.0D - u) * lower;
  }
  public double nextWeibull(double shape, double scale) throws NotStrictlyPositiveException {
    return new WeibullDistribution(getRandomGenerator(), shape, scale, WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY).sample();
  }
  public int nextBinomial(int numberOfTrials, double probabilityOfSuccess) {
    return new BinomialDistribution(getRandomGenerator(), numberOfTrials, probabilityOfSuccess).sample();
  }
  public int nextHypergeometric(int populationSize, int numberOfSuccesses, int sampleSize) throws NotPositiveException, NotStrictlyPositiveException, NumberIsTooLargeException {
    return new HypergeometricDistribution(getRandomGenerator(), populationSize, numberOfSuccesses, sampleSize).sample();
  }
  public int nextInt(final int lower, final int upper) throws NumberIsTooLargeException {
    return new UniformIntegerDistribution(getRandomGenerator(), lower, upper).sample();
  }
  public int nextPascal(int r, double p) throws NotStrictlyPositiveException, OutOfRangeException {
    return new PascalDistribution(getRandomGenerator(), r, p).sample();
  }
  public int nextSecureInt(final int lower, final int upper) throws NumberIsTooLargeException {
    return new UniformIntegerDistribution(getSecRan(), lower, upper).sample();
  }
  public int nextZipf(int numberOfElements, double exponent) throws NotStrictlyPositiveException {
    return new ZipfDistribution(getRandomGenerator(), numberOfElements, exponent).sample();
  }
  private int[] getNatural(int n) {
    int[] natural = new int[n];
    for(int i = 0; i < n; i++) {
      natural[i] = i;
    }
    return natural;
  }
  public int[] nextPermutation(int n, int k) throws NumberIsTooLargeException, NotStrictlyPositiveException {
    if(k > n) {
      throw new NumberIsTooLargeException(LocalizedFormats.PERMUTATION_EXCEEDS_N, k, n, true);
    }
    if(k <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.PERMUTATION_SIZE, k);
    }
    int[] index = getNatural(n);
    MathArrays.shuffle(index, getRandomGenerator());
    return MathArrays.copyOf(index, k);
  }
  public long nextLong(final long lower, final long upper) throws NumberIsTooLargeException {
    if(lower >= upper) {
      throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND, lower, upper, false);
    }
    final long max = (upper - lower) + 1;
    if(max <= 0) {
      final RandomGenerator rng = getRandomGenerator();
      while(true){
        final long r = rng.nextLong();
        if(r >= lower && r <= upper) {
          return r;
        }
      }
    }
    else 
      if(max < Integer.MAX_VALUE) {
        return lower + getRandomGenerator().nextInt((int)max);
      }
      else {
        return lower + nextLong(getRandomGenerator(), max);
      }
  }
  private static long nextLong(final RandomGenerator rng, final long n) throws IllegalArgumentException {
    if(n > 0) {
      final byte[] byteArray = new byte[8];
      long bits;
      long val;
      do {
        rng.nextBytes(byteArray);
        bits = 0;
        for (final byte b : byteArray) {
          bits = (bits << 8) | (((long)b) & 0xffL);
        }
        bits = bits & 0x7fffffffffffffffL;
        val = bits % n;
      }while(bits - val + (n - 1) < 0);
      return val;
    }
    throw new NotStrictlyPositiveException(n);
  }
  public long nextPoisson(double mean) throws NotStrictlyPositiveException {
    return new PoissonDistribution(getRandomGenerator(), mean, PoissonDistribution.DEFAULT_EPSILON, PoissonDistribution.DEFAULT_MAX_ITERATIONS).sample();
  }
  public long nextSecureLong(final long lower, final long upper) throws NumberIsTooLargeException {
    if(lower >= upper) {
      throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND, lower, upper, false);
    }
    final RandomGenerator rng = getSecRan();
    final long max = (upper - lower) + 1;
    if(max <= 0) {
      while(true){
        final long r = rng.nextLong();
        if(r >= lower && r <= upper) {
          return r;
        }
      }
    }
    else 
      if(max < Integer.MAX_VALUE) {
        return lower + rng.nextInt((int)max);
      }
      else {
        return lower + nextLong(rng, max);
      }
  }
  private void initRan() {
    rand = new Well19937c(System.currentTimeMillis() + System.identityHashCode(this));
  }
  public void reSeed() {
    getRandomGenerator().setSeed(System.currentTimeMillis() + System.identityHashCode(this));
  }
  public void reSeed(long seed) {
    getRandomGenerator().setSeed(seed);
  }
  public void reSeedSecure() {
    getSecRan().setSeed(System.currentTimeMillis());
  }
  public void reSeedSecure(long seed) {
    getSecRan().setSeed(seed);
  }
  public void setSecureAlgorithm(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
    secRand = RandomGeneratorFactory.createRandomGenerator(SecureRandom.getInstance(algorithm, provider));
  }
}