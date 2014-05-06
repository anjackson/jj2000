// $Id: TrinomialLFSR.java,v 1.1.1.1 2002/08/27 12:32:15 grosbois Exp $
//
// $Log: TrinomialLFSR.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:15  grosbois
// Add cryptix 3.2
//
// Revision 1.2  1997/11/22 07:05:41  raif
// *** empty log message ***
//
// + added handling of case param is null in isSame* methods.
// + optimised the pow method by pre-computing 2*n powers
//   of the base polynomial (the Z factors in Knuth Algorithm
//   A). This option comes into effect by setting the
//   PRE_COMPUTE_POWERS boolean constant to true (default)
//   and recompiling. Coded with 2 alternatives: Hashtable
//   and object array; object array is faster.
//
// Revision 1.1.1.1  1997/11/20 22:05:46  hopwood
// + Moved BigRegister and TrinomialLFSR here from the cryptix.util package.
//
// Revision 1.1.1  1997/11/20  David Hopwood
// + Renamed equals to isSameValue, and sameGroup to isSameGroup.
// + Moved to cryptix.util.math package.
//
// Revision 1.1  1997/11/07 05:53:26  raif
// *** empty log message ***
//
// Revision 0.1.3  1997/10/01 09:04:24  raif
// *** empty log message ***
//
// Revision 0.1.1  1997/09/25  R. Naffah
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team. All rights reserved.
 */
package cryptix.util.math;

import java.io.Serializable;
// import java.util.Hashtable;

/**
 * A class that implements a special category of Linear Feedback Shift
 * Register (LFSR). Formally speaking, it implements a <i>Fibonacci</i>
 * LFSR --LFSR(II)-- with a Monic, Non-Singular, Trinomial Connection
 * (Characteristic) function of the form <i>f(x) = x<font size="-1">
 * <sup>L</sup></font> + x<font size="-1"><sup>K</sup></font> + 1</i>.
 * <p>
 * <a href="#HAC"><cite>Menezes et al.</cite></a> define a generalised
 * LFSR --with an L-degree connection polynomial-- as follows:
 * <p>
 * An LFSR of length L consists of L <i>stages</i> (or <i>delay
 * elements</i>) numbered 0, 1, ..., L-1, each capable of storing one
 * bit and having one input and one output; and a <i>clock</i> which
 * controls the movement of data. During each unit of time the following
 * operations are performed:
 * <ul>
 *     <li>the contents of <i>stage</i> 0 is output and forms part
 *     of the <i>output sequence</i>;
 *     <li>the contents of <i>stage i</i> is moved to <i>stage i-1</i>
 *     for each <i>i, 1 <= i <= L-1</i>; and
 *     <li>the new contents of <i>stage L-1</i> is the <i>feedback bit
 *     s<font size="-1"><sub>j</sub></font></i> which is calculated by
 *     adding together modulo 2 the previous contents of a fixed subset
 *     of <i>stages 0, 1, ..., L-1</i> also called <i>taps</i>.
 * </ul><p>
 * Such an LFSR, referred to as a <i>Fibonacci LFSR</i>, <i>Type II LFSR
 * </i>, or simply <i>LFSR(II)</i>, is denoted by <i>&lt;L, C(D)&gt</i>,
 * where <i>C(D)</i> is called the <i>connection</i> or <i>characteristic
 * polynomial</i> and is defined as:<br>
 * <blockquote>
 *     C(D) = 1 + c<sub><font size="-1">1</font></sub>D + c<sub>
 *     <font size="-1">2</font></sub>D<sup><font size="-1">2</font></sup>
 *     + ... + c<sub><font size="-1">L</font></sub>D<sup><font size="-1">L
 *     </font></sup> <font face="Symbol">&Icirc;</FONT> Z<sub><font size=
 *     "-1">2</font></sub>[D]<br>
 * </blockquote>
 * A Linear Feedback Shift Register (LFSR) with a trinomial function of
 * the form <i>1 + x<font size="-1"><sup>K</sup></font> + x<font size=
 * "-1"><sup>L</sup></font></i> as its <i>connection polynomial</i> can
 * be schematised as follows:
 * <blockquote><pre>
 *  +--------------------XOR<-------------------------------+
 *  |                     ^                                 |
 *  |                     |                                 |
 *  |   +-----+-----+-----+-----+-----+-----+-----+-----+   |
 *  |   |stage|     |stage|stage|     |stage|stage|stage|   |
 *  +---| L-1 | ... | L-K |L-K-1| ... |  2  |  1  |  0  |---+--> output
 *      +-----+-----+-----+-----+-----+-----+-----+-----+
 *        K+1         L-1    0          K-3   K-2   K-1
 *      <------- Powers of the polynomial terms -------->
 
 * </pre></blockquote>
 * The following, collected from the references, are properties and
 * <i>curiosities</i> inherent to such objects:
 * <ul>
 *     <li>Choosing a primitive trinomial over <i>Z<font size="-1">
 *     <sub>2</sub></font></i>, ensures that each of the LFSR's <i>2<font
 *     size="-1"><sup>L</sup></font> - 1</i> initial states produces an
 *     output sequence with maximum possible period <i>2<font size="-1">
 *     <sup>L</sup></font> - 1</i>.
 *     <li>If <i>2<font size="-1"><sup>L</sup></font> - 1</i> is a prime
 *     (a <i>Mersenne prime</i>), then every irreducible polynomial of
 *     degree <i>L</i> in <i>Z<font size="-1"><sub>2</sub></font>[x]</i>
 *     is also primitive.
 *     <li>Irreducible trinomials, <i>f(x)</i>, of degree <i>L</i> can
 *     be used to represent the elements of the finite field <i>F<font
 *     size="-1"><sub>2</sub>L</font> = Z<font size="-1"><sub>2</sub></font>
 *     [x]/(f(x))</i>, the set of all polynomials in <i>Z<font size="-1">
 *     <sub>2</sub></font>[x]</i> of degree less than <i>L</i> where the
 *     addition and multiplication of polynomials is performed modulo
 *     <i>f(x)</i>.
 *     <li>If <i>f(x)</i> is a primitive polynomial of degree <i>L</i>,
 *     then <i>x<font size="-1"><sup>L</sup></font> f(x<font size="-1">
 *     <sup>-1</sup></font>) = f'(x)</i> is also a primitive polynomial.
 *     This is easily shown using the definition of a primitive polynomial,
 *     and the properties of modulo-2 arithmetic. A little thought shows
 *     that the register taps of <i>f'(x)</i> are the mirror image of those
 *     of <i>f(x)</i>. And the sequence produced by the tap-reversed
 *     generator is the time reversal of the original.
 * </ul><p>
 * Some terms and conventions associated with LFSRs, and used in this
 * implementation, are:
 * <ul>
 *     <li><b>tap-sequence</b>: the powers of the terms of the
 *     polynomial. In a monic (starts with <i>1 + ...</i>), non-
 *     singular (the term <i>x<sup><font size="-1">L</font></sup></i>
 *     for an <i>L</i>-long register is part of the polynomial function)
 *     trinomial, the sequence consists of 3 elements: 0, K, and L.
 *
 *     <li><b>mid-tap</b>: the second/middle value of the tap sequence
 *     of a monic, non-singular trinomial.
 *
 *     <li><b>state</b>: the value of the LFSR's contents. Also used
 *     to represent the coefficients of a polynomial in the field
 *     <i>F<font size="-1"><sub>2</sub>L</font></i>.
 *
 *     <li><b>terms of a polynomial</b>: the correspondence between
 *     the LFSR <i>stages</i> and the powers of <i>x</i> (the polynomial
 *     invariate) in the <i>GF[<font size="-1"><sub>2</sub>L</font>]</i>
 *     with <i>f(x) = x<font size="-1"><sup>L</sup></font> + x<font size=
 *     "-1"><sup>K</sup></font> + 1</i> is such that the term with degree
 *     0 is at <i>stage L - K - 1</i>; the term with degree 1 is at <i>
 *     stage L - K - 2</i>, etc... After <i>stage</i> 0, the relationship
 *     restarts from <code>stage</code> <i>L - 1</i>. The reason for this
 *     gymnastic is to facilitate computing the successive powers of <i>x
 *     </i> --based on the mathematical fact that <i>g(x) = x</i> is a
 *     generator of the monic primitive <i>f(x)</i>-- which in turn are
 *     used in the computation of polynomial multiplication modulo <i>f(x)
 *     </i>. When so ordered, multiplying a polynomial <i>p(x)</i> by <i>x
 *     <font size="-1"><sup>t</sup></font></i> modulo <i>f(x)</i> is as
 *     simple as loading the LFSR's register with the terms of the
 *     polynomial, and clocking it by <i>t</i> cycles.
 * </ul>
 * Finally, <a href="#HAC"><cite>Menezes et al.</cite></a> lists (Table 4.9,
 * p.162) some primitive polynomials of degree <i>m</i> over <i>Z<font size=
 * "-1"><sub>2</sub></font>, 2<font size="-1"><sup>m</sup></font> - 1</i> a
 * <i>Mersenne prime</i>. The following is an excerpt from this table for
 * values of <i>m, m <= 4096</i> and <i>k, 1 <= k <= m/2</i>, for which the
 * monic, non-singular trinomial <i>x<font size="-1"><sup>m</sup></font>
 * + x<font size="-1"><sup>k</sup></font> + 1</i> is irreducible over <i>
 * Z<font size="-1"><sub>2</sub></font></i>.
 * <blockquote><pre>
 *    m  |  k
 * ------+------------------------------
 *    2  |    1
 *    3  |    1
 *    5  |    2
 *    7  |    1,    3
 *   17  |    3,    5,     6
 *   31  |    3,    6,     7,   13
 *   89  |   38
 *  127  |    1,    7,    15,   30,  63
 *  521  |   32,   48,   158,  168
 *  607  |  105,  147,   273
 * 1279  |  216,  418
 * 2281  |  715,  915,  1029
 * 3217  |   67,  576
 * </pre></blockquote>
 * <b>Implementation Notes:</b>
 * <p>
 * In order to increase performance of this class, and since it's extending
 * <code>BigRegister</code>, which assumes a bit numbering using bit index
 * 0 as the rightmost one, our LFSR will actually look like so:
 * <blockquote><pre>
 *     +------------------->XOR--------------------------------+
 *     |                     ^                                 |
 *     |                     |                                 |
 *     |   +-----+-----+-----+-----+-----+-----+-----+-----+   |
 *     |   | bit |     | bit | bit |     | bit | bit | bit |   |
 *  <--+---| L-1 | ... | L-K |L-K-1| ... |  2  |  1  |  0  |<--+
 *         +-----+-----+-----+-----+-----+-----+-----+-----+
 *           K-1          0    L-1         K+2   K+1    K
 *         <------- Powers of the polynomial terms -------->
 * </pre></blockquote>
 * Obtaining a normal representation of the powers of the polynomial
 * is done by left rotating the LFSR's contents by <i>K</i> positions.
 * <p>
 * Clocking the LFSR consists of executing the following pseudo-code:
 * <pre>
 *     out = getBit(L-1);
 *     in = out ^ getBit(L-K-1);
 *     shiftLeft(1);
 *     if (in == 1) setBit(0);
 * </pre>
 * <p>
 * <b>References:</b>
 * <ol>
 *   <li> <a name="HAC">[HAC]</a>
 *        A. J. Menezes, P. C. van Oorschot, S. A. Vanstone,
 *        <cite>Handbook of Applied Cryptography</cite>
 *        CRC Press 1997, pp 195-212.
 *        <p>
 *   <li> <a name="AC2">[AC2]</a>
 *        Bruce Schneier,
 *        <cite>Applied Cryptography, 2nd edition</cite>,
 *        John Wiley &amp; Sons, Inc. 1996, pp 372-428.
 *        <p>
 *   <li> <a name="LFSR">[LFSR]</a>
 *        Arthur H. M. Ross,
 *        <cite>Linear Feedback Shift Registers</cite>,
 *        WWW page <a href="http://www.cdg.org/a_ross/LFSR.html">
 *        www.cdg.org/a_ross/LFSR.html</a>
 * </ol>
 * <p>
 * <b>Copyright</b> &copy; 1995-1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  Raif S. Naffah
 * @since   Cryptix 2.2.2
 */
public class TrinomialLFSR
extends BigRegister
implements Cloneable, Serializable
{
// Variables and constants
//...........................................................................

    /**
     * Number of stages/delay elements in this LFSR which is also
     * the <i>degree</i> of the <i>connection trinomial</i>.
     */
    private int L;

    /** Degree (power) of the <i>mid-tap</i> connection. */
    private int K;

    /**
     * Clocking is the process of computing the new feedback bit from
     * the output one and feeding it back to the end of the register.
     * On a bit by bit basis, this looks like so:
     * <pre>
     *     out = getBit(L-1);
     *     in = out ^ getBit(L-K-1);
     *     shiftLeft(1);
     *     if (in == 1) setBit(0);
     * </pre>
     * It is clear from the above that better efficiency and speed
     * can be achieved if we can process a larger chunck of bits at
     * a time than just one bit.
     * <p>
     * This variable is here for exactly this purpose. It tells us
     * how many bits we can alter with maximum efficiency. It is
     * computed at instantiation time as the min(64, K, L-K).
     */
    private int slice;
    private int warpFactor;

    private static final long serialVersionUID = -8054549768481919515L;

    /** Make this false and recompile if space is rare. */
    private static final boolean PRE_COMPUTE_POWERS = true;
//    private static final boolean PRE_COMPUTE_POWERS = false;
    
    /**
     * Table of pre-computed 2n powers (n = 0, 2, 4, 8, 16, ..., 2*(L-1))
     * of the current polynomial. The value for the 0 power, instead
     * of storing the unity polynomial, will consist of the base
     * polynomial itself. This is necessary since the TrinomialLFSR
     * object is mutable and we need to assert if the current contents
     * of the hashtable are the powers of the current state/value of
     * this object.
     * <p>
     * Declared as transient so it doen't get serialised.
     */
    // [] is faster and more straightforward -- RSN.
//    private transient Hashtable powers;
    private transient TrinomialLFSR[] powers;


// Constructor
//...........................................................................

    /**
     * Define an LFSR with <code>L</code> stages and with a connection
     * trinomial of the form: <i>x<font size="-1"><sup>L</sup></font> +
     * x<font size="-1"><sup>K</sup></font> + 1.</i>
     *
     * @param  l  Size of the register; ie. number of levels/stages
     *         of this LFSR. Is also the <i>degree</i> of the <i>
     *         connection trinomial</i>.
     * @param  k  Degree/power of the <i>mid-tap</i> within the LFSR.
     * @exception  IllegalArgumentException  If k <= 0 or k >= l.
     */
    public TrinomialLFSR (int l, int k) {
        super(l);
        if (k < 1 || k > l - 1) throw new IllegalArgumentException();
        L = l;
        K = k;
        warpFactor = Math.min(K, L - K);
        slice = Math.min(64, warpFactor);
    }


// Cloneable method implementation
//...........................................................................

    public Object clone() {
        TrinomialLFSR result = new TrinomialLFSR(L, K);
        result.load(this);
        return result;
    }


// FSR methods
//...........................................................................
    
    /**
     * Repeatedly invoke the <code>engineClock()</code> method until
     * the LFSR has been clocked <code>ticks</code> times.
     *
     * @param  ticks  Number of steps to clock the FSR by. If it is
     *         &lt;= 0 nothing happens.
     */
    public void clock (int ticks) {
        if (ticks < 1) return;
        int morcel = ticks % slice;
        if (morcel != 0) {
            engineClock(morcel);
            ticks -= morcel;
        }
        while (ticks > 0) {
            engineClock(slice);
            ticks -= slice;
        }
    }

    /**
     * Clock the register <i>ticks</i> steps.
     *
     * @param  ticks  Number of steps to clock the register. It is the
     *         responsibility of the caller to ensure that this value
     *         never exceeds that of <code>slice</code>.
     */
    protected void engineClock (int ticks) {
        long io = getBits(L-ticks, ticks) ^ getBits(L-K-ticks, ticks);
        shiftLeft(ticks);
        if (io != 0) setBits(0, ticks, io);
    }

    private void jump (int ticks) {
        if (ticks < 1) return;
        int morcel = ticks % warpFactor;
        if (morcel != 0) {
            clock(morcel);
            ticks -= morcel;
        }
        BigRegister b1, b2;
        while (ticks > 0) {
            b1 = (BigRegister) clone();
            b2 = (BigRegister) clone();
            b2.shiftLeft(warpFactor);
            b2.xor(b1);
            b2.shiftRight(L-warpFactor);
            shiftLeft(warpFactor);
            or(b2);
            ticks -= warpFactor;
        }
    }
    
    /**
     * Return the value of the leftmost <code>count</code> bits of
     * this LFSR and clock it by as many ticks. Note however that
     * only the minimum of <code>count</code> and <code>slice</code>
     * bits, among those returned, are meaningful.
     *
     * @param  count  Number of leftmost bits to consider. If this
     *         value is zero then return 0.
     * @return The value of the leftmost <code>count</code> bits
     *         right justified in a <code>long</code>.
     */
    public long next (int count) {
        if (count < 1) return 0L;
        long result = getBits(L-count, count);
        clock(count);
        return result;
    }


// Arithmetical methods in GF(2**L) over (f(x) = x**L + x**K + 1)
//...........................................................................

    /**
     * Compute <code>this += gx (mod f(x))</code>. Note that this
     * operation is only meaningful, when the monic trinomial is primitive.
     *
     * @param  gx  A representation of the terms of a polynomial to add
     *         to <code>this</code>.
     * @exception  IllegalArgumentException  If the argument is not in
     *         the same group as <code>this</code>.
     */
    public void add (TrinomialLFSR gx) {
        if (! isSameGroup(gx)) throw new IllegalArgumentException();
        xor(gx);
    }

    /**
     * Compute <code>this -= gx (mod f(x))</code>. Note that this
     * operation is only meaningful, when the monic trinomial is
     * primitive. When such is the case the result is the same as
     * that obtained by the <code>add()</code> method since in <i>
     * F<font size="-1"><sub>2</sub>n</font></i> every polynomial is
     * its own additive inverse.
     *
     * @param  gx  A representation of the terms of a polynomial to
     *         subtract from <code>this</code>.
     * @exception  IllegalArgumentException  If the argument is not
     *         in the same group as <code>this</code>.
     */
    public void subtract (TrinomialLFSR gx) { add(gx); }

    /**
     * Compute <code>this *= gx (mod f(x))</code>. Note that this
     * operation is only meaningful, when the monic trinomial is primitive.
     *
     * @param  gx  A representation of the terms of a polynomial to
     *         multiply by <code>this</code>.
     * @exception  IllegalArgumentException  If the argument is not in
     *         the same group as <code>this</code>.
     */
    public void multiply (TrinomialLFSR gx) {
        if (! isSameGroup(gx)) throw new IllegalArgumentException();
        if (gx.countSetBits() == 0) {                        // x * 0 = 0
            reset();
            return;
        }
        TrinomialLFSR X = new TrinomialLFSR(L, K);    // 1st multiplicand
        TrinomialLFSR result = null;
        int t;
        for (int i = 0; i < L; i++) {
            if (gx.testBit(i)) {       // term #i in 2nd multiplicand set
                X.load(this);
                t = degreeAt(i);        // translate index to power/ticks
                if (t != 0) X.jump(t);                // multiply by x**t
                if (result == null)
                    result = (TrinomialLFSR) X.clone();
                else
                    result.add(X);
            }
        }
        load(result);
    }

    /**
     * Return the product of the two arguments modulo <i>f(x))</i>, where
     * both arguments are members of the same polynomial group with the
     * same monic trinomial <i>f(x)</i>. Note that this operation is only
     * meaningful, when the monic trinomial is primitive.
     *
     * @param  p  A representation of the terms of the first polynomial
     *         multiplicand.
     * @param  q  A representation of the terms of the second polynomial
     *         multiplicand.
     * @return The product of the two arguments modulo <i>f(x))</i>.
     * @exception  IllegalArgumentException  If the arguments are not
     *         from the same group.
     */
    public static TrinomialLFSR multiply (TrinomialLFSR p, TrinomialLFSR q) {
        if (! p.isSameGroup(q)) throw new IllegalArgumentException();
        // optimise performance by choosing the second operand to be the
        // one with fewer bits set
        TrinomialLFSR result;
        if (p.countSetBits() > q.countSetBits()) {
            result = (TrinomialLFSR) p.clone();
            result.multiply(q);
        } else {
            result = (TrinomialLFSR) q.clone();
            result.multiply(p);
        }
        return result;
    }

    /**
     * Raise <code>this</code> to the <code>n</code>th power modulo
     * <i>f(x))</i>. Note that this operation is only meaningful,
     * when the monic trinomial is primitive.
     *
     * @param  n  Bit representation of the power to raise this
     *         polynomial representation to.
     * @exception  IllegalArgumentException  If the argument's <code>
     *         size</code> is greater than that of <code>this</code>.
     */
    public void pow (BigRegister n) {
        if (n.getSize() > L) throw new IllegalArgumentException();
        //
        // Algorithm A (adapted from)
        // The Art of Computer Programming, Vol. 2. Donald E. Knuth; p.442.
        //
        int limit = n.highestSetBit();
        if (limit == 0) return;                       // (any)**1 = (any)
        if (limit == -1) {                              // (any)**0 = (1)
            resetX(0);
            return;
        }
        TrinomialLFSR Y = trinomialOne();
        TrinomialLFSR Z = (TrinomialLFSR) clone();
        if (PRE_COMPUTE_POWERS) {
/*
// Hashtable ................................................................
//
            if (powers == null) powers = new Hashtable(L);          // exist?
            boolean ok = powers.size() != 0;
            if (ok) {                                   // table is not empty
                // see if we haven't pre-computed the contents in an earlier
                // invocation. do so by checking if value at #0 is == this.
                TrinomialLFSR x = (TrinomialLFSR) powers.get(new Integer(0));
                ok = this.isSameValue(x);
            }
            if (! ok) {                              // pre-compute the table
                powers.clear();
                powers.put(new Integer(0), (TrinomialLFSR) Z.clone()); // hash #0 is this
                for (int i = 1, j = 2; i < L; i++, j <<= 1) {
                    Z.multiply(Z);
                    powers.put(new Integer(j), (TrinomialLFSR) Z.clone());
                }
            }
            // use it
            Y = n.testBit(0) ? (TrinomialLFSR) clone() : trinomialOne();
            int j = 2;
            for (int i = 1; i < limit; i++, j <<= 1)
                if (n.testBit(i)) {
                    Z = (TrinomialLFSR) powers.get(new Integer(j));
                    Y = multiply(Z, Y);
                }
            Z = (TrinomialLFSR) powers.get(new Integer(j));
*/
// Array ....................................................................
//
            if (powers == null) powers = new TrinomialLFSR[L];      // exist?
            // see if we haven't pre-computed the contents in an earlier
            // invocation. do so by checking if value at #0 is this.
            if (! this.isSameValue(powers[0])) {     // pre-compute the table
                powers[0] = (TrinomialLFSR) Z.clone();     // hash #0 is this
                for (int i = 1; i < L; i++) {
                    Z.multiply(Z);
                    powers[i] = (TrinomialLFSR) Z.clone();
                }
            }
            // use it
            int i;
            for (i = 0; i < limit; i++)
                if (n.testBit(i)) Y = multiply(powers[i], Y);
            Z = powers[i];
//
        } else {
            Y = trinomialOne();                       // multiplicative unity
            for (int i = 0; i < limit; i++) {
                if (n.testBit(i)) Y = multiply(Z, Y);
                Z.multiply(Z);
            }
        }
        load(multiply(Y, Z));
    }


// Other polynomial methods
//...........................................................................

    /**
     * Set the LFSR's initial state to a value that corresponds
     * to the polynomial term of the designated degree.
     *
     * @param  n  Reset the register's contents to all zeroes except
     *         for a <i>1</i> at the index position corresponding to
     *         the term x<font size="-1"><sup>n</sup></font></i>.
     * @exception  IllegalArgumentException  If the argument value
     *         is negative or greater than or equal to <code>this</code>
     *         LFSR's trinomial degree.
     */
    public void resetX (int n) {
        reset();
        setX(n);
    }

    /**
     * Set (to one) </code>this</code> LFSR's polynomial term of
     * the given degree. The other <code>stages</code>, in contrast
     * to the <code>resetX()</code> method, are unaffected.
     *
     * @param  n  Set (to one) the register position of the term
     *         x<font size="-1"><sup>n</sup></font></i>.
     * @exception  IllegalArgumentException  If the argument value
     *         is negative or greater than or equal to <code>this</code>
     *         LFSR's trinomial degree.
     */
    public void setX (int n) { setBit(indexOfX(n)); }

    /**
     * Return the register's index relative to the polynomial
     * term <i>x<font size="-1"><sup>degree</sup></font></i>.
     *
     * @param  degree  The power of the invariate <i>x</i>, for which
     *         the register's index is to be found.
     * @return The register's index relative to the polynomial term
     *         <i>x<font size="-1"><sup>degree</sup></font></i>.
     * @exception  IllegalArgumentException  If the argument value
     *         is negative or greater than or equal to <code>this</code>
     *         LFSR's trinomial degree.
     */
    public int indexOfX (int degree) {
        if (degree < 0 || degree >= L) throw new IllegalArgumentException();
        int index = degree - K;
        if (index < 0) index += L;
        return index;
    }

    /**
     * Return the power of the term <i>x<font size="-1"><sup>result</sup>
     * </font></i> relative to the given register's index.
     *
     * @return The power of the invariate <i>x</i> relative to the given
     *         register's index.
     * @param  index  The register's index relative to the polynomial term
     *         <i>x<font size="-1"><sup>result</sup></font></i>.
     * @exception  IllegalArgumentException  If the argument value
     *         is negative or greater than or equal to <code>this</code>
     *         LFSR's trinomial degree.
     */
    public int degreeAt (int index) {
        if (index < 0 || index >= L) throw new IllegalArgumentException();
        return (index + K) % L;
    }

    /**
     * Return a <code>TrinomialLFSR</code> object whose state is set
     * to the powers of the polynomial <i>p(x)</i> such that <i>p(x)
     * = 1</i> in the polynomial <i>Group</i> defined over the trinomial
     * function of <code>this</code> object.
     *
     * @return A <code>TrinomialLFSR</code> object whose state is set
     *         to the powers of the polynomial <i>p(x)</i> such that
     *         <i>p(x) = 1</i> in the polynomial <i>Group</i> defined
     *         over the trinomial function of <code>this</code> object.
     */
    public TrinomialLFSR trinomialOne () {
        TrinomialLFSR x = (TrinomialLFSR) clone();
        x.resetX(0);
        return x;
    }

    /**
     * Return a <code>TrinomialLFSR</code> object whose state is set
     * to the powers of the polynomial <i>p(x)</i> such that <i>p(x)
     * = x</i> in the polynomial <i>Group</i> defined over the trinomial
     * function of <code>this</code> object.
     *
     * @return A <code>TrinomialLFSR</code> object whose state is set
     *         to the powers of the polynomial <i>p(x)</i> such that
     *         <i>p(x) = x</i> in the polynomial <i>Group</i> defined
     *         over the trinomial function of <code>this</code> object.
     */
    public TrinomialLFSR trinomialX () {
        TrinomialLFSR x = (TrinomialLFSR) clone();
        x.resetX(1);
        return x;
    }


// Accessors
//...........................................................................

    /**
     * Return the number of <i>elements</i> in this LFSR, which is also
     * the <i>degree</i> of the trinomial.
     *
     * @return The number of <i>elements</i> in this LFSR.
     */
    public int getSize () { return L; }

    /**
     * Return the degree/power of the <i>mid-tap</i> element in this LFSR.
     *
     * @return The degree/power of the <i>mid-tap</i> element in this LFSR.
     */
    public int getMidTap () { return K; }

    /**
     * Return the maximum number of meaningful bits in this LFSR, which
     * is also the maximum number of bits that can be processed in one
     * operation without loss of desired output sequence.
     *
     * @return The maximum number of meaningful bits in this LFSR.
     */
    public int getSlice () { return slice; }


// Test and comparison
//...........................................................................

    /**
     * Return true if the TrinomialLFSR <i>x</i> has equal characteristics
     * and contents to this one; false otherwise.
     * <p>
     * NOTE: the <code>equals</code> method is not used, because this is
     * a mutable object (see the requirements for equals in the Java Language
     * Spec).
     *
     * @return true iff x has equal characteristics and contents.
     */
    public boolean isSameValue (TrinomialLFSR x) {
        if (x == null || x.K != K) return false;
        return super.isSameValue((BigRegister) x);
    }

    /**
     * Compare this LFSR to the argument, returning -1, 0 or 1 for
     * less than, equal to, or greater than comparison.
     *
     * @return -1, 0, +1 If the contents of this object are
     *         respectively less than, equal to, or greater than
     *         those of the argument.
     */
    public int compareTo (TrinomialLFSR x) {
        if (L > x.L) return 1;
        if (L < x.L) return -1;
        if (K > x.K) return 1;
        if (K < x.K) return -1;
        BigRegister ba = this.toBigRegister();
        BigRegister bb = x.toBigRegister();
        return ba.compareTo(bb);
    }

    /**
     * Return true iff the argument is a polynomial that belongs to
     * the same <i>Group</i> as <code>this</code>.
     *
     * @return true iff the argument is a polynomial that belongs to
     *         the same <i>Group</i> as <code>this</code>.
     */
    public boolean isSameGroup (TrinomialLFSR x) {
        if (x == null || L != x.L || K != x.K) return false;
        return true;
    }
    

// Visualisation and introspection methods
//...........................................................................

    /**
     * Return the state of <code>this</code> LFSR as a <code>BigRegister
     * </code> object where now the powers of the polynomial terms are
     * ordered in ascending succession starting from power 0 at index 0.
     *
     * @return The state of <code>this</code> LFSR as a <code>BigRegister
     *         </code> object where now the powers of the polynomial terms
     *         are ordered in ascending succession starting from power 0
     *         at index 0.
     */
    public BigRegister toBigRegister () {
        BigRegister result = (BigRegister) clone();
        result.rotateLeft(K);
        return result;
    }

    /**
     * Return a formatted <code>String</code> representation of the binary
     * contents of <code>this</code>.
     *
     * @return A formatted string representation of the binary contents
     *         of <code>this</code>.
     */
    public String toString () {
        StringBuffer sb = new StringBuffer(8 * L + 64);
        sb.append("[...\n TrinomialLFSR <").append(L).append(", x").
            append(L).append(" + ").append((K == 1) ? "x" : "x" + K).
            append(" + 1").append(">...\n").
            append(" current state is: ").append(super.toString()).
            append("...]\n");
        return sb.toString();
    }

    /**
     * Return a formatted <code>String</code> representation of the
     * polynomial form represented by <code>this</code> LFSR's state.
     *
     * @return A formatted string representation of the binary contents
     *         of <code>this</code>.
     */
    public String toPolynomial () {
        StringBuffer sb = new StringBuffer(16);
        sb.append(' ');
        int d = L;
        boolean firstTime = true;
        while (--d >= 0) {
            if (testBit(indexOfX(d))) {
                if (firstTime)
                    firstTime = !firstTime;
                else
                    sb.append(" + ");
                if (d != 0) {
                    sb.append('x');
                    if (d != 1) sb.append(d);
                } else
                    sb.append('1');
            }
        }
        if (firstTime) sb.append('0');
        return sb.append(' ').toString();
    }
}
