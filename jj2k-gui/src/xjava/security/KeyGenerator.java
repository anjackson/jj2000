// $Id: KeyGenerator.java,v 1.1.1.1 2002/08/27 11:49:30 grosbois Exp $
//
// $Log: KeyGenerator.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:30  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.3  2000/08/17 11:35:24  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.2  1997/11/21 04:31:18  hopwood
// + Committed changes below.
//
// Revision 1.1.1.1.1  1997/11/18  David Hopwood
// + Implement java.security.Parameterized.
//
// Revision 1.1.1.1  1997/11/03 22:36:57  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.3  1997/08/12  David Hopwood
// + Merged Raif's version with my version.
// + Replaced getImpl with getImplementation.
// + Added references to guide/ijce/Algorithms.html in documentation.
// + Removed commented-out methods.
//
// Revision 0.1.0.2  1997/07/22  R. Naffah
// + Commented out the generateKey(String,[B,Z) method. It doesn't seem
//   to fit properly here, and its functionality exists in
//   cryptix.security.keys.RawKeyGenerator.
//
// Revision 0.1.0.1  1997/07/19  R. Naffah
// + Added WeakKeyExceptions where needed.
// + Moved generateKey([B) method to cryptix.security.keys.RawKeyGenerator.
//
// Revision 0.1.0.0  1997/?/0?  David Hopwood
// + Start of history.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package xjava.security;

import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;


/**
 * The KeyGenerator class is used to generate keys for a given
 * algorithm. Key generators are constructed using the
 * <code>getInstance</code> factory methods (static methods that
 * return instances of a given class).
 * <p>
 * The KeyGenerator interface is entirely algorithm independent,
 * and, as for the KeyPairGenerator instances, KeyGenerator instances
 * may be cast to algorithm-specific interfaces defined elsewhere
 * in the Java Cryptography Architecture.
 * <p>
 * A typical set of calls would be:
 * <pre>
 *    import java.security.KeyGenerator;
 * <br>
 *    SecureRandom random = new SecureRandom();
 *    KeyGenerator keygen = KeyGenerator.getInstance("DES");
 *    keygen.initialize(random);
 *    Key key = keygen.generateKey();
 * </pre>
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @author  Raif S. Naffah
 * @since   IJCE 1.0.0
 */
public abstract class KeyGenerator
implements Parameterized
{

// Constants and variables
//...........................................................................

    /**
     * Name of the KeyGenerator algorithm used for this instance.
     */
    private String algorithm;


// Constructor
//............................................................................

    /**
     * Creates a KeyGenerator object for the specified algorithm.
     *
     * @param  algorithm  the standard string name of the algorithm.
     * @exception NullPointerException if algorithm == null
     */
    protected KeyGenerator(String algorithm) {
        if (algorithm == null)
            throw new NullPointerException("algorithm == null");
        this.algorithm = algorithm;
    }


// JCE methods
//...........................................................................

    /**
     * Generates a KeyGenerator object that implements the algorithm
     * requested, as available in the environment.
     * <p>
     * See <a href="../guide/ijce/Algorithms.html#KeyGenerator">
     * <cite>International JCE Standard Algorithm Names</a> for a list
     * of KeyGenerator algorithm names.
     *
     * @param  algorithm    the standard name or an alias for the algorithm.
     * @return the new KeyGenerator object.
     * @exception NoSuchAlgorithmException if the algorithm is not
     *                      available in the environment.
     */
    public static KeyGenerator getInstance(String algorithm)
    throws NoSuchAlgorithmException {
        return (KeyGenerator) (IJCE.getImplementation(algorithm,
            "KeyGenerator"));
    }

    /**
     * Generates a KeyGenerator object implementing the specified
     * algorithm, as supplied from the specified provider, if such an
     * algorithm is available from the provider.
     * <p>
     * See <a href="../guide/ijce/Algorithms.html#KeyGenerator">
     * <cite>International JCE Standard Algorithm Names</a> for a list
     * of KeyGenerator algorithm names.
     *
     * @param  algorithm    the standard name or an alias for the algorithm.
     * @param  provider     the string name of the provider.
     * @return the new KeyGenerator object.
     * @exception NoSuchAlgorithmException if the algorithm is not
     *                      available from the provider.
     * @exception NoSuchProviderException if the provider is not
     *                      available in the environment.
     */
    public static KeyGenerator getInstance(String algorithm, String provider)
    throws NoSuchAlgorithmException, NoSuchProviderException {
        return (KeyGenerator) (IJCE.getImplementation(algorithm, provider,
            "KeyGenerator"));
    }

    /**
     * Returns the standard name of the algorithm for this key generator.
     * <p>
     * See <a href="../guide/ijce/Algorithms.html#KeyGenerator">
     * <cite>International JCE Standard Algorithm Names</a> for a list
     * of KeyGenerator algorithm names.
     *
     * @return the standard string name of the algorithm.
     */
    public String getAlgorithm() { return algorithm; }

    /**
     * Initializes the key generator.
     *
     * @param  random   a source of randomness for this generator.
     */
    public abstract void initialize(SecureRandom random);

    /**
     * Generates a key. This method generates a new key every time
     * it is called.
     *
     * @return the new key.
     */
    public abstract SecretKey generateKey();

    /**
     * Sets the specified algorithm parameter to the specified value.
     * <p>
     * This method supplies a general-purpose mechanism through which it is
     * possible to set the various parameters of this object. A uniform
     * algorithm-specific naming scheme for each parameter is desirable but
     * left unspecified at this time.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @param  param    the string identifier of the parameter.
     * @param  value    the parameter value.
     * @exception NullPointerException if param == null
     * @exception NoSuchParameterException if there is no parameter with name
     *                  param for this key generator implementation.
     * @exception InvalidParameterException if the parameter exists but cannot
     *                  be set.
     * @exception InvalidParameterTypeException if value is the wrong type
     *                  for this parameter.
     */
    public void setParameter(String param, Object value)
    throws NoSuchParameterException, InvalidParameterException,
           InvalidParameterTypeException {
        if (param == null) throw new NullPointerException("param == null");
        engineSetParameter(param, value);
    }

    /**
     * Gets the value of the specified algorithm parameter.
     * <p>
     * This method supplies a general-purpose mechanism through which it is
     * possible to get the various parameters of this object. A uniform
     * algorithm-specific naming scheme for each parameter is desirable but
     * left unspecified at this time.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @param  param    the string name of the parameter.
     * @return the object that represents the parameter value.
     * @exception NullPointerException if param == null
     * @exception NoSuchParameterException if there is no parameter with name
     *                  param for this key generator implementation.
     * @exception InvalidParameterException if the parameter exists but cannot
     *                  be read.
     */
    public Object getParameter(String param)
    throws NoSuchParameterException, InvalidParameterException {
        if (param == null) throw new NullPointerException("param == null");
        return engineGetParameter(param);
    }

    /**
     * <b>SPI</b>: Sets the specified algorithm parameter to the specified
     * value.
     * <p>
     * This method supplies a general-purpose mechanism through which it is
     * possible to set the various parameters of this object. A uniform
     * algorithm-specific naming scheme for each parameter is desirable but
     * left unspecified at this time.
     * <p>
     * The default implementation always throws a NoSuchParameterException.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @param  param    the string name of the parameter. 
     * @param  value    the parameter value.
     * @exception NoSuchParameterException if there is no parameter with name
     *                  param for this key generator implementation.
     * @exception InvalidParameterException if the parameter exists but cannot
     *                  be set.
     * @exception InvalidParameterTypeException if value is the wrong type
     *                  for this parameter.
     */
    protected void engineSetParameter(String param, Object value)
    throws NoSuchParameterException, InvalidParameterException,
           InvalidParameterTypeException {
        throw new NoSuchParameterException(getAlgorithm() + ": " + param);
    }

    /**
     * <b>SPI</b>: Gets the value of the specified algorithm parameter.
     * <p>
     * This method supplies a general-purpose mechanism through which it is
     * possible to get the various parameters of this object. A uniform
     * algorithm-specific naming scheme for each parameter is desirable but
     * left unspecified at this time.
     * <p>
     * The default implementation always throws a NoSuchParameterException.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @param  param    the string name of the parameter. 
     * @return the object that represents the parameter value.
     * @exception NoSuchParameterException if there is no parameter with name
     *                  param for this key generator implementation.
     * @exception InvalidParameterException if the parameter exists but cannot
     *                  be read.
     */
    protected Object engineGetParameter(String param)
    throws NoSuchParameterException, InvalidParameterException {
        throw new NoSuchParameterException(getAlgorithm() + ": " + param);
    }

    /**
     * Returns a clone of this key generator.
     * <p>
     * Note: In JavaSoft's version of JCE, <code>KeyGenerator.clone()</code> is
     * protected. This is not very useful, since then an application (as opposed
     * to the key generator implementation itself) is not able to call it.
     *
     * @exception CloneNotSupportedException if the key generator is not cloneable.
     */
    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        } else {
            throw new CloneNotSupportedException();
        }
    }

    public String toString() {
        return super.toString() + " KeyGenerator [" + algorithm + "]";
    }


// IJCE additional static methods
//...........................................................................

    /**
     * Gets the standard names of all KeyGenerators implemented by a
     * provider.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     * For compatibility you may wish to use
     * <code><a href="java.security.IJCE.html#getAlgorithms(java.security.Provider, java.lang.String)">
     * IJCE.getAlgorithms</a>(provider, "KeyGenerator")</code> instead.
     *
     * @since IJCE 1.0.1
     */
    public static String[] getAlgorithms(Provider provider) {
        return IJCE.getAlgorithms(provider, "KeyGenerator");
    }

    /**
     * Gets the standard names of all KeyGenerators implemented by any
     * installed provider. Algorithm names are not duplicated if
     * they are supported by more than one provider.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     * For compatibility you may wish to use
     * <code><a href="java.security.IJCE.html#getAlgorithms(java.lang.String)">
     * IJCE.getAlgorithms</a>("KeyGenerator")</code> instead.
     *
     * @since IJCE 1.0.1
     */
    public static String[] getAlgorithms() {
        return IJCE.getAlgorithms("KeyGenerator");
    }
}
