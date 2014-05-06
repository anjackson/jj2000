// This file is currently unlocked (change this line if you lock the file)
//
// $Log: RSAFactors.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:31  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.2  2000/08/17 11:35:28  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.1.1.1  1997/11/03 22:36:58  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.1  1997/08/23  David Hopwood
// + Renamed the following methods (and updated documentation):
//   - getSmallerFactor and getLargerFactor => getP and getQ
//   - getInverseOfSmallerModLarger => getInverseOfQModP
// + The coefficient is now q^-1 (mod p), not p^-1 (mod q).
//   Apart from PGP, this is more commonly used (e.g. see the
//   P1363 draft).
//
// Revision 0.1.0.0  1997/08/19  David Hopwood
// + Start of history.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 *
 * This file may be modified and redistributed without restriction.
 */

package xjava.security.interfaces;

import java.math.BigInteger;

/**
 * An interface with methods to return the factors and coefficient of an
 * RSA modulus. These are useful for speeding up RSA operations using the
 * Chinese Remainder Theorem.
 * <p>
 * It is not specified which factor is the larger, but the coefficient
 * must be equal to the inverse of <i>q</i> modulo <i>p</i>.
 * <p>
 * Note that if the encryption exponent <i>e</i>, and the two factors are
 * available to an attacker, the decryption exponent <i>d</i> can be found.
 * This means that the factors must always be kept secret.
 * <p>
 * The intention is for implementations of RSAPrivateKey (and possibly
 * RSAPublicKey) to optionally implement this interface. Users of RSA keys can
 * check whether the key object is an <code>instanceof RSAFactors</code>,
 * and if so make a further check that the factors are known for this key
 * (i.e. that <code>getP</code>, <code>getQ</code> and
 * <code>getInverseOfQModP</code> return non-null values).
 * <p>
 * For example:
 * <pre>
 *    import java.security.interfaces.*;
 * <br>
 *    void foo(RSAPrivateKey key) {
 *        BigInteger p = null, q = null, u = null;
 * <br>
 *        if (key instanceof RSAFactors) {
 *            RSAFactors factors = (RSAFactors) key;
 *            p = factors.getP();
 *            q = factors.getQ();
 *            u = factors.getInverseOfQModP();
 *        }
 *        if (p != null) {
 *            // efficient code (q and u can be assumed to be non-null)
 *        } else {
 *            // less efficient code
 *        }
 *    }
 * </pre>
 * <p>
 * Key implementations should ensure that either all of the methods defined
 * in this interface return null, or that none of them do.
 * <p>
 * <strong><a href="../guide/ijce/JCEDifferences.html">This interface
 * is not supported in JavaSoft's version of JCE.</a></strong>.
 * To maintain compatibility, the RSAFactors classfile should be included
 * with any application that uses it and may be linked against JavaSoft's
 * JCE, so that the application will not fail with a NoClassDefFoundError.
 * <p>
 *
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 *
 * <p><b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @since   IJCE 1.0.1
 *
 * @see java.security.interfaces.RSAKey
 */
public interface RSAFactors {
    /**
     * Returns the first prime factor, <i>p</i>, or null if the factors of
     * the modulus are unknown.
     */
    BigInteger getP();

    /**
     * Returns the second prime factor, <i>q</i>, or null if the factors of
     * the modulus are unknown.
     */
    BigInteger getQ();

    /**
     * Returns the coefficient, equal to the multiplicative inverse of
     * <i>q</i> modulo <i>p</i>, or null if the factors of the modulus are
     * unknown.
     */
    BigInteger getInverseOfQModP();
}
