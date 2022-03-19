package pt.fcul.masters.util;
/******************************************************************************
 *  Compilation:  javac ComplexNumber.java
 *  Execution:    java ComplexNumber
 *
 *  Data type for complex numbers.
 *
 *  The data type is "immutable" so once you create and initialize
 *  a ComplexNumber object, you cannot change it. The "final" keyword
 *  when declaring re and im enforces this rule, making it a
 *  compile-time error to change the .re or .im instance variables after
 *  they've been initialized.
 *
 *  % java ComplexNumber
 *  a            = 5.0 + 6.0i
 *  b            = -3.0 + 4.0i
 *  Re(a)        = 5.0
 *  Im(a)        = 6.0
 *  b + a        = 2.0 + 10.0i
 *  a - b        = 8.0 + 2.0i
 *  a * b        = -39.0 + 2.0i
 *  b * a        = -39.0 + 2.0i
 *  a / b        = 0.36 - 1.52i
 *  (a / b) * b  = 5.0 + 6.0i
 *  conj(a)      = 5.0 - 6.0i
 *  |a|          = 7.810249675906654
 *  tan(a)       = -6.685231390246571E-6 + 1.0000103108981198i
 *
 ******************************************************************************/

import java.util.Objects;

public class ComplexNumber {
    private final double re;   // the real part
    private final double im;   // the imaginary part

    /**
     *  create a new object with the given real and imaginary parts
     */
    public ComplexNumber(double real, double imag) {
        re = real;
        im = imag;
    }

    /**
     *  return a string representation of the invoking ComplexNumber object
     */
    public String toString() {
        if (im == 0) return re + "";
        if (re == 0) return im + "i";
        if (im <  0) return re + " - " + (-im) + "i";
        return re + " + " + im + "i";
    }

    /**
     * @return abs/modulus/magnitude
     */
    public double abs() {
        return Math.hypot(re, im);
    }

    /**
     *  @return angle/phase/argument, normalized to be between -pi and pi
     */
    public double phase() {
        return Math.atan2(im, re);
    }

    /**
     * @return a new ComplexNumber object whose value is (this + b)
     */
    public ComplexNumber plus(ComplexNumber b) {
        ComplexNumber a = this;             // invoking object
        double real = a.re + b.re;
        double imag = a.im + b.im;
        return new ComplexNumber(real, imag);
    }

    /**
     *  return a new ComplexNumber object whose value is (this - b)
     */
    public ComplexNumber minus(ComplexNumber b) {
        ComplexNumber a = this;
        double real = a.re - b.re;
        double imag = a.im - b.im;
        return new ComplexNumber(real, imag);
    }

    /**
     * return a new ComplexNumber object whose value is (this * b)
     */
    public ComplexNumber times(ComplexNumber b) {
        ComplexNumber a = this;
        double real = a.re * b.re - a.im * b.im;
        double imag = a.re * b.im + a.im * b.re;
        return new ComplexNumber(real, imag);
    }

    /**
     *  return a new object whose value is (this * alpha)
     */
    public ComplexNumber scale(double alpha) {
        return new ComplexNumber(alpha * re, alpha * im);
    }

    /**
     * return a new ComplexNumber object whose value is the conjugate of this
     */
    public ComplexNumber conjugate() {
        return new ComplexNumber(re, -im);
    }

    /**
     *  return a new ComplexNumber object whose value is the reciprocal of this
     */
    public ComplexNumber reciprocal() {
        double scale = re*re + im*im;
        return new ComplexNumber(re / scale, -im / scale);
    }

    // return the real or imaginary part
    public double re() { return re; }
    public double im() { return im; }

    /**
     *  return a / b
     */
    public ComplexNumber divides(ComplexNumber b) {
        ComplexNumber a = this;
        return a.times(b.reciprocal());
    }

    /**
     *  return a new ComplexNumber object whose value is the complex exponential of this
     */
    public ComplexNumber exp() {
        return new ComplexNumber(Math.exp(re) * Math.cos(im), Math.exp(re) * Math.sin(im));
    }

    /**
     *  return a new ComplexNumber object whose value is the complex sine of this
     */
    public ComplexNumber sin() {
        return new ComplexNumber(Math.sin(re) * Math.cosh(im), Math.cos(re) * Math.sinh(im));
    }

    /**
     *  return a new ComplexNumber object whose value is the complex cosine of this
     */
    public ComplexNumber cos() {
        return new ComplexNumber(Math.cos(re) * Math.cosh(im), -Math.sin(re) * Math.sinh(im));
    }

    /**
     *  return a new ComplexNumber object whose value is the complex tangent of this
     */
    public ComplexNumber tan() {
        return sin().divides(cos());
    }
    


    /**
     *  a static version of plus
     */
    public static ComplexNumber plus(ComplexNumber a, ComplexNumber b) {
        double real = a.re + b.re;
        double imag = a.im + b.im;
        ComplexNumber sum = new ComplexNumber(real, imag);
        return sum;
    }

    public boolean equals(Object x) {
        if (x == null) return false;
        if (this.getClass() != x.getClass()) return false;
        ComplexNumber that = (ComplexNumber) x;
        return (this.re == that.re) && (this.im == that.im);
    }

    public int hashCode() {
        return Objects.hash(re, im);
    }

    // sample client for testing
    public static void main(String[] args) {
        ComplexNumber a = new ComplexNumber(5.0, 6.0);
        ComplexNumber b = new ComplexNumber(-3.0, 4.0);

        System.out.println("a            = " + a);
        System.out.println("b            = " + b);
        System.out.println("Re(a)        = " + a.re());
        System.out.println("Im(a)        = " + a.im());
        System.out.println("b + a        = " + b.plus(a));
        System.out.println("a - b        = " + a.minus(b));
        System.out.println("a * b        = " + a.times(b));
        System.out.println("b * a        = " + b.times(a));
        System.out.println("a / b        = " + a.divides(b));
        System.out.println("(a / b) * b  = " + a.divides(b).times(b));
        System.out.println("conj(a)      = " + a.conjugate());
        System.out.println("|a|          = " + a.abs());
        System.out.println("tan(a)       = " + a.tan());
    }

}