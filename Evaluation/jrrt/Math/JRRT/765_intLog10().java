package org.apache.commons.math3.dfp;

public class DfpDec extends Dfp  {
  public DfpDec(final Dfp d) {
    super(d);
    round(0);
  }
  protected DfpDec(final DfpField factory) {
    super(factory);
  }
  protected DfpDec(final DfpField factory, byte x) {
    super(factory, x);
  }
  protected DfpDec(final DfpField factory, double x) {
    super(factory, x);
    round(0);
  }
  protected DfpDec(final DfpField factory, final String s) {
    super(factory, s);
    round(0);
  }
  protected DfpDec(final DfpField factory, final byte sign, final byte nans) {
    super(factory, sign, nans);
  }
  protected DfpDec(final DfpField factory, int x) {
    super(factory, x);
  }
  protected DfpDec(final DfpField factory, long x) {
    super(factory, x);
  }
  @Override() public Dfp newInstance() {
    return new DfpDec(getField());
  }
  @Override() public Dfp newInstance(final byte x) {
    return new DfpDec(getField(), x);
  }
  @Override() public Dfp newInstance(final byte sign, final byte nans) {
    return new DfpDec(getField(), sign, nans);
  }
  @Override() public Dfp newInstance(final double x) {
    return new DfpDec(getField(), x);
  }
  @Override() public Dfp newInstance(final int x) {
    return new DfpDec(getField(), x);
  }
  @Override() public Dfp newInstance(final String s) {
    return new DfpDec(getField(), s);
  }
  @Override() public Dfp newInstance(final long x) {
    return new DfpDec(getField(), x);
  }
  @Override() public Dfp newInstance(final Dfp d) {
    if(getField().getRadixDigits() != d.getField().getRadixDigits()) {
      getField().setIEEEFlagsBits(DfpField.FLAG_INVALID);
      final Dfp result = newInstance(getZero());
      result.nans = QNAN;
      return dotrap(DfpField.FLAG_INVALID, "newInstance", d, result);
    }
    return new DfpDec(d);
  }
  @Override() public Dfp nextAfter(Dfp x) {
    final String trapName = "nextAfter";
    if(getField().getRadixDigits() != x.getField().getRadixDigits()) {
      getField().setIEEEFlagsBits(DfpField.FLAG_INVALID);
      final Dfp result = newInstance(getZero());
      result.nans = QNAN;
      return dotrap(DfpField.FLAG_INVALID, trapName, x, result);
    }
    boolean up = false;
    Dfp result;
    Dfp inc;
    if(this.lessThan(x)) {
      up = true;
    }
    if(equals(x)) {
      return newInstance(x);
    }
    if(lessThan(getZero())) {
      up = !up;
    }
    if(up) {
      int var_765 = intLog10();
      inc = power10(var_765 - getDecimalDigits() + 1);
      inc = copysign(inc, this);
      if(this.equals(getZero())) {
        inc = power10K(MIN_EXP - mant.length - 1);
      }
      if(inc.equals(getZero())) {
        result = copysign(newInstance(getZero()), this);
      }
      else {
        result = add(inc);
      }
    }
    else {
      inc = power10(intLog10());
      inc = copysign(inc, this);
      if(this.equals(inc)) {
        inc = inc.divide(power10(getDecimalDigits()));
      }
      else {
        inc = inc.divide(power10(getDecimalDigits() - 1));
      }
      if(this.equals(getZero())) {
        inc = power10K(MIN_EXP - mant.length - 1);
      }
      if(inc.equals(getZero())) {
        result = copysign(newInstance(getZero()), this);
      }
      else {
        result = subtract(inc);
      }
    }
    if(result.classify() == INFINITE && this.classify() != INFINITE) {
      getField().setIEEEFlagsBits(DfpField.FLAG_INEXACT);
      result = dotrap(DfpField.FLAG_INEXACT, trapName, x, result);
    }
    if(result.equals(getZero()) && this.equals(getZero()) == false) {
      getField().setIEEEFlagsBits(DfpField.FLAG_INEXACT);
      result = dotrap(DfpField.FLAG_INEXACT, trapName, x, result);
    }
    return result;
  }
  protected int getDecimalDigits() {
    return getRadixDigits() * 4 - 3;
  }
  @Override() protected int round(int in) {
    int msb = mant[mant.length - 1];
    if(msb == 0) {
      return 0;
    }
    int cmaxdigits = mant.length * 4;
    int lsbthreshold = 1000;
    while(lsbthreshold > msb){
      lsbthreshold /= 10;
      cmaxdigits--;
    }
    final int digits = getDecimalDigits();
    final int lsbshift = cmaxdigits - digits;
    final int lsd = lsbshift / 4;
    lsbthreshold = 1;
    for(int i = 0; i < lsbshift % 4; i++) {
      lsbthreshold *= 10;
    }
    final int lsb = mant[lsd];
    if(lsbthreshold <= 1 && digits == 4 * mant.length - 3) {
      return super.round(in);
    }
    int discarded = in;
    final int n;
    if(lsbthreshold == 1) {
      n = (mant[lsd - 1] / 1000) % 10;
      mant[lsd - 1] %= 1000;
      discarded |= mant[lsd - 1];
    }
    else {
      n = (lsb * 10 / lsbthreshold) % 10;
      discarded |= lsb % (lsbthreshold / 10);
    }
    for(int i = 0; i < lsd; i++) {
      discarded |= mant[i];
      mant[i] = 0;
    }
    mant[lsd] = lsb / lsbthreshold * lsbthreshold;
    final boolean inc;
    switch (getField().getRoundingMode()){
      case ROUND_DOWN:
      inc = false;
      break ;
      case ROUND_UP:
      inc = (n != 0) || (discarded != 0);
      break ;
      case ROUND_HALF_UP:
      inc = n >= 5;
      break ;
      case ROUND_HALF_DOWN:
      inc = n > 5;
      break ;
      case ROUND_HALF_EVEN:
      inc = (n > 5) || (n == 5 && discarded != 0) || (n == 5 && discarded == 0 && ((lsb / lsbthreshold) & 1) == 1);
      break ;
      case ROUND_HALF_ODD:
      inc = (n > 5) || (n == 5 && discarded != 0) || (n == 5 && discarded == 0 && ((lsb / lsbthreshold) & 1) == 0);
      break ;
      case ROUND_CEIL:
      inc = (sign == 1) && (n != 0 || discarded != 0);
      break ;
      case ROUND_FLOOR:
      default:
      inc = (sign == -1) && (n != 0 || discarded != 0);
      break ;
    }
    if(inc) {
      int rh = lsbthreshold;
      for(int i = lsd; i < mant.length; i++) {
        final int r = mant[i] + rh;
        rh = r / RADIX;
        mant[i] = r % RADIX;
      }
      if(rh != 0) {
        shiftRight();
        mant[mant.length - 1] = rh;
      }
    }
    if(exp < MIN_EXP) {
      getField().setIEEEFlagsBits(DfpField.FLAG_UNDERFLOW);
      return DfpField.FLAG_UNDERFLOW;
    }
    if(exp > MAX_EXP) {
      getField().setIEEEFlagsBits(DfpField.FLAG_OVERFLOW);
      return DfpField.FLAG_OVERFLOW;
    }
    if(n != 0 || discarded != 0) {
      getField().setIEEEFlagsBits(DfpField.FLAG_INEXACT);
      return DfpField.FLAG_INEXACT;
    }
    return 0;
  }
}