// $Id: Cipher.java,v 1.1.1.1 2002/08/27 11:49:29 grosbois Exp $
//
// $Log: Cipher.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:29  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.9  2000/08/17 11:35:23  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.8  1997/12/03 03:36:06  hopwood
// + Committed changes below.
//
// Revision 1.7.1  1997/12/03  hopwood
// + Removed temporary dependency on cryptix.util.core.Hex.
//
// Revision 1.7  1997/12/03 01:13:16  raif
// + Fixed a bug in the updateInternal() method when isFinal is true
//   for a decryption with padding and in the input length is 0. This
//   is what CipherInputStream uses to force the padding.
//
// Revision 1.6  1997/12/01 03:37:27  hopwood
// + Committed changes below.
//
// Revision 1.5.1  1997/11/30  hopwood
// + Fixed bugs when used with CipherInputStream and a padding cipher.
// + More debugging calls added.
//
// Revision 1.5  1997/11/29 04:45:12  hopwood
// + Committed changes below.
//
// Revision 1.4.1  1997/11/28  hopwood
// + Completed padding implementation in updateInternal.
// + Fixed some bugs in updateInternal.
// + Changed semantics of getInstance(String algorithm, String provider): it
//   now looks for all components from the given provider, rather than just
//   the cipher. Added example documentation of how to specify the provider
//   for each component.
// + Made output of toString more concise.
// + Added doFinal methods, to ease migration to JCE 1.2.
//
// Revision 1.4  1997/11/21 04:31:18  hopwood
// + Committed changes below.
//
// Revision 1.3.2  1997/11/18  David Hopwood
// + Don't set the block size for the padding scheme until initEncrypt/
//   initDecrypt is called, because it might not be valid before then.
//   Note that only the plaintext block size is relevant for padding.
// + Debug output now goes to IJCE.getDebugOutput().
// + Got rid of implPadding (i.e. we now throw an exception if it is true).
//   Allowing the cipher implementation to handle padding is too complicated,
//   and IMHO unnecessary.
// + A call to engineSetPaddingScheme was missing from
//   getInstance(Cipher, Mode, PaddingScheme).
// + Rewrote updateInternal again (still not finished).
//
// Revision 1.3.1  1997/11/18  David Hopwood
// + Implement java.security.Parameterized.
// + Made getParameter and setParameter non-final.
//
// Revision 1.3  1997/11/07 14:32:47  raif
// *** empty log message ***
//
// Revision 1.2  1997/11/07 05:53:26  raif
// + Fixed padding handling. See also java.security.PaddingScheme.
//
// Revision 1.1.1.1  1997/11/03 22:36:57  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.10  1997/11/03  David Hopwood
// + Added inBufferSize, inBufferSizeFinal and related methods, similar to
//   outBufferSize etc. These are needed by CipherInputStream.
// + Fixed bugs in outBufferSizeInternal.
// + Removed getFinalCiphertextSize, engineFinalCiphertextSize, and
//   unpadBufferSize, which were not used.
// + Slightly changed the criteria for a "padding block cipher", to use
//   getPaddingScheme() != null instead of !getPadding().equals("NONE").
//
// Revision 0.1.0.9  1997/11/01  David Hopwood
// + Changed PaddingScheme.setBlockSize to engineSetBlockSize.
//
// Revision 0.1.0.8  1997/10/26  David Hopwood
// + Added isPaddingBlockCipher method.
//
// Revision 0.1.0.7  1997/08/17  David Hopwood
// + Rewrote part of updateInternal method.
//
// Revision 0.1.0.6  1997/08/12  David Hopwood
// + Replaced getImpl with getImplementation.
// + Added references to guide/ijce/Algorithms.html in documentation.
//
// Revision 0.1.0.5  1997/08/11  David Hopwood
// + Changed debugging to use properties file.
//
// Revision 0.1.0.4  1997/08/09  David Hopwood
// + Merge of Raif's version and my version.
// + Changed state back to private (the same as in JavaSoft's
//   JCE).
// + Changed implMultiblock back to implBuffering.
// + Removed constructor that takes a PaddingScheme (this can now
//   be done by calling engineSetPaddingScheme).
// + Renamed the following private fields:
//   - algorithm => cipherName
//   - mode => modeName
//   - padding => paddingName
//   - paddingObj => padding
// + Corrected getInstance(Cipher, Mode, PaddingScheme) to be static.
//
// Revision 0.1.0.3  1997/07/20  R. Naffah
// + Tested OK.
// + Fixed where paddingObj.setBlockSize() whould be called.
// + Replaced the updateInternal() with new implementation.
//
// Revision 0.1.0.2  1997/07/20  R. Naffah
// + Use setBlockSize() now defined in PaddingScheme when instantiating
//   a new cipher.
//
// Revision 0.1.0.1  1997/07/11  R. Naffah
// + Made the state variable protected instead of private so cipher
//   algorithm and Mode implementations can r/w it. Should remain so
//   until the setParameter() method is working.
// + Modified outBufferSizeInternal() to handle case when no paddingObj
//   is set.
//
// Revision 0.1.0.0  1997/??/??  David Hopwood
// + Start of history (IJCE 1.0.0).
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package xjava.security;

import java.io.PrintWriter;

import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;


/**
 * This class is used to provide the functionality of a general purpose
 * encryption algorithm, such as DES or RSA. Encryption is used to ensure
 * confidentiality of digital data.
 * <p>
 * This class follows the general algorithm architecture found elsewhere
 * in the security API: the base class provides an algorithm-independent
 * interface to basic encryption functionality, with provider implementation
 * subclassing a subset of the behaviours.
 * <p>
 * Like other algorithm-based classes in Java Security, the Cipher
 * class is separated between application and provider interfaces:
 * <dl>
 *   <dt> <b>Cipher API</b> (Application Programming Interface)
 *     <dd> This is the interface of methods called by applications
 *          needing encryption services. The API consists of all public
 *          methods.
 *   <dt> <b>Cipher SPI</b> (Service Provider Interface)
 *     <dd> This is the interface implemented by providers that supply
 *          specific algorithms. It consists of all methods whose names
 *          are prefixed by <code>engine</code>. Each such method is
 *          usually called by a correspondingly-named public API method.
 *          For example, the <code>engineInitEncrypt</code> method is
 *          called by the <code>initEncrypt</code> method.
 * </dl>
 * <p>
 * Ciphers represented by this class satisfy the following constraints:
 * <ul>
 *   <li> At any point in time the cipher has a plaintext block size, and
 *        a ciphertext block size, both expressed in bytes. These sizes may
 *        change when initEncrypt or initDecrypt are called, and they may
 *        be invalid before the cipher is initialized, but they do not
 *        change at other times.
 *   <li> A plaintext block encrypts to exactly one ciphertext block, and
 *        a ciphertext block decrypts to exactly one plaintext block.
 * </ul>
 * <p>
 * Byte-oriented stream ciphers (or ciphers in CFB and OFB modes, for
 * example) have plaintext and ciphertext block sizes of 1 byte. For
 * public key ciphers, it is common for the block sizes to be dependent
 * on the length of some parameter of the public key.
 * <p>
 * A block cipher implementation may either implement its own buffering
 * (by passing <code>implBuffering == true</code> to the constructor), or leave
 * it to the Cipher superclass (<code>implBuffering</code> == false). When
 * the implementation handles buffering, data passed to <code>update</code> is
 * passed directly on to <code>engineUpdate</code>, and data passed to
 * <code>crypt</code> is passed to <code>engineUpdate</code>, followed
 * immediately by a call to <code>engineCrypt</code>.
 * <p>
 * When the Cipher superclass handles buffering, up to one block is buffered,
 * in order to ensure that the length of data passed to <code>engineUpdate</code>
 * is always a multiple of the block size. In this case the <code>engineCrypt</code>
 * method is not used.
 * <p>
 * Cipher implementations are not required or expected to be threadsafe.
 * If methods of a single Cipher object are called simultaneously by more
 * than one thread, the result will be unpredictable.
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
public abstract class Cipher
extends IJCE_Traceable
implements Parameterized
{

// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = true;
    private static int debuglevel = DEBUG ? IJCE.getDebugLevel("Cipher") : 0;
    private static PrintWriter err = DEBUG ? IJCE.getDebugOutput() : null;
    private static void debug(String s) { err.println("Cipher: " + s); }
    private static String dump(byte[] b) {
        if (b == null) return "null";
//        if (b.length <= 32) return cryptix.util.core.Hex.toString(b);
        return b.toString();
    }


//...........................................................................

    /**
     * The state of the cipher object when it is uninitialized, 
     * that is, the state it is in right after it has been created.
     */
    public static final int UNINITIALIZED = 0;

    /**
     * The state of the cipher when it is ready to encrypt, that is,
     * the state it is in right after a call to <code>initEncrypt</code>.
     *
     * @see #initEncrypt
     */
    public static final int ENCRYPT = 1;

    /**
     * The state of the cipher when it is ready to decrypt, that is,
     * the state it is in right after a call to <code>initDecrypt</code>.
     *
     * @see #initDecrypt
     */
    public static final int DECRYPT = 2;

    private boolean implBuffering;
//    private boolean implPadding;
    private byte[] buffer;
    private int buffered;
    private int inputSize;
    private int outputSize;
    private String provider;
    private String cipherName;
    private String modeName;
    private String paddingName;
    private PaddingScheme padding;
    private int state; // defaults to UNINITIALIZED = 0

    /**
     * The JCE docs say: "Constructor used for dynamic instantiation."
     * I don't understand why this is needed. --DJH
     * @deprecated
     */
    protected Cipher() {
        super("Cipher");
    }

    /**
     * Constructor for a Cipher. This constructor is only for use
     * by subclasses, which should pass the correct arguments to convey
     * their behaviour to the superclass. Applications cannot call this
     * constructor directly.
     * <p>
     * For byte-oriented stream ciphers (where the input block size is 1),
     * buffering is not needed, and the <i>implBuffering</i> parameter has
     * no effect.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">In this version
     * of IJCE, <i>implPadding</i> must be false. If it is true, an
     * <samp>IllegalArgumentException</samp> is thrown.</a></strong>
     *
     * @param  implBuffering    if true, this argument indicates that data
     *                          will always be passed from update/crypt to
     *                          engineUpdate/engineCrypt without modification.
     * @param  implPadding      must be false.
     * @param  provider         the name of the provider of the underlying
     *                          cryptographic engine.
     */
    protected
    Cipher(boolean implBuffering, boolean implPadding, String provider) {
        super("Cipher");
        if (implPadding)
            throw new IllegalArgumentException("IJCE does not support ciphers for which implPadding == true");

        this.implBuffering = implBuffering;
        this.provider = provider;
    }

    /**
     * This constructor is identical to the previous one (with arguments
     * boolean, boolean, String), except that it does not have the redundant
     * <i>implPadding</i> parameter, and also allows the algorithm name
     * to be specified.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This constructor
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @param  implBuffering    if true, this argument indicates that data
     *                          will always be passed from update/crypt to
     *                          engineUpdate/engineCrypt without modification.
     * @param  provider         the name of the provider of the underlying
     *                          cryptographic engine.
     * @param  algorithm        the name of this algorithm (optionally with
     *                          mode and padding, separated by '/'), as it is
     *                          configured in the properties file.
     */
    protected Cipher(boolean implBuffering, String provider, String algorithm) {
        super("Cipher");
        this.implBuffering = implBuffering;
        this.provider = provider;
        parseAlgorithm(algorithm);
    }

    private void parseAlgorithm(String algorithm) {
        int p = algorithm.indexOf('/');
        if (p == -1)
            cipherName = algorithm;
        else {            // "cipher/..."
            cipherName = algorithm.substring(0, p);
            int q = algorithm.indexOf('/', p+1);
            if (q == -1)  // "cipher/mode"
                modeName = algorithm.substring(p+1);
            else {        // "cipher/mode/padding"
                modeName = algorithm.substring(p+1, q);
                paddingName = algorithm.substring(q+1);
            }
        }
    }

    private void
    setNames(String cipherName, String modeName, String paddingName, String provider) {
        if (this.cipherName == null)  this.cipherName = cipherName;
        if (this.modeName == null)    this.modeName = modeName;
        if (this.paddingName == null) this.paddingName = paddingName;
        if (this.provider == null)    this.provider = provider;
    }

    /**
     * Returns the object implementing padding for this cipher, or null if
     * there is no such object.
     */
    protected final PaddingScheme getPaddingScheme() {
        return padding;
    }

    /**
     * Generates a Cipher object that implements the given algorithm. If the
     * default provider package contains a class implementing the algorithm,
     * an instance of that class is returned. If it is not available in the
     * default package, other packages are searched.
     * <p>
     * Any of the following formats can be used for the algorithm name:
     * <p>
     * <dl>
     *   <dt> cipher "/" mode "/" padding
     *     <dd> all algorithm components - cipher, mode, and padding -
     *          are specified (these may be aliases).
     *   <dt> cipher "/" mode
     *     <dd> the padding is assumed to be "NONE".
     *   <dt> cipher
     *     <dd> the mode is assumed to be "ECB", and the padding to be "NONE".
     * </dl>
     * <p>
     * See <a href="../guide/ijce/Algorithms.html#Cipher">
     * <cite>International JCE Standard Algorithm Names</cite></a> for a list
     * of Cipher algorithm names.
     *
     * @param  algorithm    the algorithm name, as described above.
     * @return the new Cipher object, in the UNINITIALIZED state.
     * @exception NoSuchAlgorithmException if the algorithm is
     *                      not available from any provider.
     */
    public static Cipher getInstance(String algorithm)
    throws NoSuchAlgorithmException {
        try { return getInstance(algorithm, null); }
        catch (NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(e.getMessage());
        }
    }

    /**
     * Generates a Cipher object that implements the given cipher, from
     * the given provider. The format of the cipher specification is as
     * described for <code>getInstance(String algorithm)</code>.
     * <p>
     * If the algorithm is implemented using more than one component
     * (cipher, mode, and padding scheme), all of them must come from the
     * given provider. More flexibility can be obtained by using the
     * <code>getInstance(Cipher, Mode, PaddingScheme)</code> factory method.
     * For example, to request "DES" from the SUN provider, "CBC" from the
     * Cryptix provider, and "PKCS#5" from any provider, use:
     * <pre>
     *    Cipher.getInstance(
     *        Cipher.getInstance("DES", "SUN"),
     *        Mode.getInstance("CBC", "Cryptix"),
     *        PaddingScheme.getInstance("PKCS#5")
     *    )
     * </pre>
     * <p>
     * See <a href="../guide/ijce/Algorithms.html#Cipher">
     * <cite>International JCE Standard Algorithm Names</cite></a> for a list
     * of Cipher algorithm names.
     *
     * @param  algorithm    the cipher specification.
     * @return the new Cipher object, in the UNINITIALIZED state.
     * @exception NoSuchAlgorithmException if the algorithm is not
     *                      available from the provider.
     * @exception NoSuchProviderException if the provider is not
     *                      available in the environment.
     */
    public static Cipher getInstance(String algorithm, String provider)
    throws NoSuchAlgorithmException, NoSuchProviderException {
        if (algorithm == null)
            throw new NullPointerException("algorithm == null");

        // parse the algorithm spec
        String cipherName = algorithm;
        String modeName = "ECB";
        String paddingName = "NONE";

        int p = algorithm.indexOf('/');
        if (p != -1) {      // "cipher/..."
            cipherName = algorithm.substring(0, p);
            int q = algorithm.indexOf('/', p+1);
            if (q == -1)  // "cipher/mode"
                modeName = algorithm.substring(p+1);
            else {        // "cipher/mode/padding"
                modeName = algorithm.substring(p+1, q);
                paddingName = algorithm.substring(q+1);
            }
        }
        return getInstance(cipherName, modeName, paddingName, provider);
    }

    /**
     * Generates a Cipher object implementing the specified algorithm, mode,
     * and padding from the specified provider.
     *
     * @param  algorithmName    the string name of the algorithm.
     * @param  modeName         the string name of the mode.
     * @param  paddingName      the string name of the padding scheme.
     * @return the new Cipher object, in the UNINITIALIZED state.
     * @exception NoSuchAlgorithmException if the algorithm is not
     *                          available from the provider.
     * @exception NoSuchProviderException if the provider is not
     *                          available in the environment.
     */
    private static Cipher
    getInstance(String cipherName, String modeName, String paddingName, String provider)
    throws NoSuchAlgorithmException, NoSuchProviderException {

if (DEBUG && debuglevel >= 3) debug("Entered getInstance(\"" + cipherName + "\", \"" +
    modeName + "\", \"" + paddingName + "\", \"" + provider + "\")");

        // Note: we use IJCE.getImplementation, not Security.getImpl, below,
        // because Security.getImpl is not guaranteed to exist in all
        // implementations of JCA.
        cipherName = IJCE.getStandardName(cipherName, "Cipher");
        modeName = IJCE.getStandardName(modeName, "Mode");
        paddingName = IJCE.getStandardName(paddingName, "PaddingScheme");

        Cipher result, nested = null;
        PaddingScheme padding = null;
        try {   // first check for a combined "cipher/mode/padding".
            result = (Cipher) (IJCE.getImplementation(cipherName + "/" + modeName +
                "/" + paddingName, provider, "Cipher"));
        } catch (NoSuchAlgorithmException e) {
            if (modeName.equals("ECB"))
                result = (Cipher) (IJCE.getImplementation(cipherName, provider,
                    "Cipher"));
            else {
                try {   // check for "cipher/mode".
                    result = (Cipher) (IJCE.getImplementation(cipherName + "/" +
                        modeName, provider, "Cipher"));
                } catch (NoSuchAlgorithmException e2) {
                    // otherwise, the object returned to the user will be a
                    // subclass of Mode.
                    nested = (Cipher) (IJCE.getImplementation(cipherName, provider,
                        "Cipher"));
                    nested.setNames(cipherName, "ECB", "NONE", provider);
                    result = (Cipher) (IJCE.getImplementation(modeName, provider,
                        "Mode"));
                }
            }
            if (!paddingName.equals("NONE")) {
                padding = (PaddingScheme) (IJCE.getImplementation(paddingName,
                    provider, "PaddingScheme"));
            }
        }
        result.setNames(cipherName, modeName, paddingName, provider);

        // We used to set the input block size for the padding scheme here, but
        // it might not be valid until the cipher is initialized, so set it in
        // initEncrypt/Decrypt instead.

        if (nested != null) ((Mode) result).engineSetCipher(nested);
        if (padding != null) result.engineSetPaddingScheme(padding);

if (DEBUG && debuglevel >= 3) debug("Created cipher [1]: " + result);

        return result;
    }

    /**
     * Generates a new Cipher object by composing the given Cipher, Mode and
     * PaddingScheme objects. <i>mode</i> may be null (indicating ECB),
     * and <i>padding</i> may be null (indicating NONE).
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @exception NullPointerException if cipher == null
     */
    public static Cipher
    getInstance(Cipher cipher, Mode mode, PaddingScheme padding) {
        if (cipher == null) throw new NullPointerException("cipher == null");
        String cipherName = cipher.getAlgorithm();
        String modeName = (mode == null) ? "ECB" : mode.getAlgorithm();
        String paddingName =
            (padding == null) ? "NONE" : padding.getAlgorithm();
        String provider = cipher.getProvider();

        Cipher result, nested = null;
        if (mode == null)
            result = cipher;
        else {
            nested = cipher;
            result = mode;
        }
        result.setNames(cipherName, modeName, paddingName, provider);
        if (nested != null)((Mode) result).engineSetCipher(nested);
        if (padding != null) result.engineSetPaddingScheme(padding);

if (DEBUG && debuglevel >= 3) debug("Created cipher [2]: " + result);

        return result;
    }

    /**
     * Returns the state of this Cipher object. Possible states are:
     * <p>
     * <dl>
     *   <dt> UNINITIALIZED
     *     <dd> The cipher has not been initialized. 
     *   <dt> ENCRYPT
     *     <dd> The cipher has been initialized for encryption. It may be
     *          used for encryption only.
     *   <dt> DECRYPT
     *     <dd> The cipher has been initialized for decryption. It may be
     *          used for decryption only.
     * </dl>
     *
     * @return the state of this cipher object.
     *
     * @see #UNINITIALIZED
     * @see #ENCRYPT
     * @see #DECRYPT
     */
    public final int getState() { return state; }

    /**
     * Returns this algorithm's standard cipher name (<em>not</em> including
     * mode and padding).
     * <p>
     * See <a href="../guide/ijce/Algorithms.html#Cipher">
     * <cite>International JCE Standard Algorithm Names</cite></a> for a list
     * of Cipher algorithm names.
     *
     * @return the standard cipher name (such as "DES").
     */
    public final String getAlgorithm() { return cipherName; }

    /**
     * Returns this algorithm's standard mode name.
     * <p>
     * See <a href="../guide/ijce/Algorithms.html#Mode">
     * <cite>International JCE Standard Algorithm Names</cite></a> for a list
     * of Mode algorithm names.
     *
     * @return the algorithm's standard mode name (such as "CBC")
     */
    public final String getMode() {
        return (modeName == null) ? "ECB" : modeName;
    }

    /**
     * Returns this algorithm's standard padding scheme name.
     * <p>
     * See <a href="../guide/ijce/Algorithms.html#PaddingScheme">
     * <cite>International JCE Standard Algorithm Names</cite></a> for a list
     * of PaddingScheme algorithm names.
     *
     * @return the algorithm's standard padding scheme name (such as
     *         "PKCS#7" or "NONE")
     */
    public final String getPadding() {
        return (paddingName == null) ? "NONE" : paddingName;
    }

    /**
     * Returns the name of the provider of this cipher.
     *
     * @return the provider name (such as "SUN" or "Cryptix")
     */
    public final String getProvider() { return provider; }

    /**
     * Returns true if this cipher is a padding block cipher.
     * <p>
     * A cipher is a padding block cipher iff <code>getPlaintextBlockSize() > 1
     * && getPaddingScheme() != null</code>.
     * If getPlaintextBlockSize throws an exception, so will this method.
     * <p>
     * This method is needed because
     * <samp><a href="java.security.CipherInputStream.html">CipherInputStream</a></samp> and
     * <samp><a href="java.security.CipherOutputStream.html">CipherOutputStream</a></samp>
     * use a different buffering algorithm for padding block ciphers.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     */
    public final boolean isPaddingBlockCipher() {
        return getPlaintextBlockSize() > 1 && getPaddingScheme() != null;
    }

    /**
     * Returns the size of the buffer necessary to hold the output
     * resulting from a call to <code>update</code> (i.e. not including
     * padding). This call takes into account any incomplete block
     * currently being buffered.
     *
     * @param  inLen    the number of bytes to process.
     * @exception IllegalArgumentException if inLen < 0
     */
    public final int outBufferSize(int inLen) {
        return outBufferSizeInternal(inLen, false);
    }

    /**
     * Returns the size of the buffer necessary to hold the output
     * resulting from a call to <code>crypt</code> (including padding
     * for the final block of the stream, if applicable). This call takes
     * into account any incomplete block currently being buffered.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @param  inLen    the number of bytes to process.
     * @exception IllegalArgumentException if inLen < 0
     */
    public final int outBufferSizeFinal(int inLen) {
        return outBufferSizeInternal(inLen, true);
    }

    /**
     * Returns the minimum number of bytes of input, that will cause an
     * output of <i>outLen</i> bytes from a call to <code>update</code> (i.e.
     * not including padding). This call takes into account any incomplete
     * block currently being buffered.
     * <p>
     * This is used by <samp>CipherInputStream</samp>, for example, to
     * calculate how much data must be read from its underlying stream before
     * encryption or decryption.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @param  outLen   the number of bytes of output required.
     * @exception IllegalArgumentException if outLen < 0
     */
    public final int inBufferSize(int outLen) {
        return inBufferSizeInternal(outLen, false);
    }

    /**
     * Returns the minimum number of bytes of input, that will cause an
     * output of <i>outLen</i> bytes from a call to <code>crypt</code>
     * (including padding for the final block of the stream, if applicable).
     * This call takes into account any incomplete block currently being
     * buffered.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @param  outLen   the number of bytes of output required.
     * @exception IllegalArgumentException if outLen < 0
     */
    public final int inBufferSizeFinal(int outLen) {
        return inBufferSizeInternal(outLen, true);
    }

    /**
     * Returns the length of a block for this cipher. If plaintext and
     * ciphertext blocks are different lengths, this method throws an
     * IllegalBlockSizeException.
     *
     * @return the length in bytes of a block.
     * @exception IllegalBlockSizeException if getPlaintextBlockSize() !=
     *                  getCiphertextBlockSize()
     */
    public final int blockSize() {
        int blocksize = enginePlaintextBlockSize();
        if (blocksize != engineCiphertextBlockSize())
            throw new IllegalBlockSizeException(
                "blockSize() called when plaintext and ciphertext block sizes differ");
        return blocksize;
    }

    /**
     * Returns the length of an input block, in bytes. When the cipher is
     * in encryption mode, this is the length of a plaintext block. When in
     * decryption mode, it is the length of a ciphertext block.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @return the length in bytes of an input block for this cipher.
     * @exception Error if the cipher is uninitialized.
     */
    public final int getInputBlockSize() {
        switch (getState()) {
            case ENCRYPT: return enginePlaintextBlockSize();
            case DECRYPT: return engineCiphertextBlockSize();
            default: IJCE.reportBug("invalid Cipher state: " + getState());
            case UNINITIALIZED: throw new Error("cipher uninitialized");
        }
    }

    /**
     * Returns the length of an output block, in bytes. When the cipher is
     * in encryption mode, this is the length of a ciphertext block. When in
     * decryption mode, it is the length of a plaintext block.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @return the length in bytes of an output block for this cipher.
     * @exception Error if the cipher is uninitialized.
     */
    public final int getOutputBlockSize() {
        switch (getState()) {
            case ENCRYPT: return engineCiphertextBlockSize();
            case DECRYPT: return enginePlaintextBlockSize();
            default: IJCE.reportBug("invalid Cipher state: " + getState());
            case UNINITIALIZED: throw new Error("cipher uninitialized");
        }
    }

    /**
     * Returns the length of a plaintext block, in bytes.
     * For byte-oriented stream ciphers, this method returns 1.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @return the length in bytes of a plaintext block for this cipher.
     */
    public final int getPlaintextBlockSize() {
        return enginePlaintextBlockSize();
    }

    /**
     * Returns the length of a ciphertext block, in bytes.
     * For byte-oriented stream ciphers, this method returns 1.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @return the length in bytes of a ciphertext block for this cipher.
     */
    public final int getCiphertextBlockSize() {
        return engineCiphertextBlockSize();
    }

    /**
     * Initializes this cipher for encryption, using the specified
     * key. A successful call to this method puts the cipher in the
     * ENCRYPT state. This method may be called on a cipher in any
     * state. Any state information (key, feedback buffer, ...) is
     * lost and reset.
     *
     * @param  key  the key to use for encryption.
     * @exception NullPointerException if key == null
     * @exception KeyException if the key is invalid.
     */
    public final void initEncrypt(Key key) throws KeyException {
        if (key == null) throw new NullPointerException("key == null");
        if (tracing) traceVoidMethod("engineInitEncrypt(<" + key + ">)");
        engineInitEncrypt(key);
        state = ENCRYPT;

        inputSize = enginePlaintextBlockSize();
        outputSize = engineCiphertextBlockSize();
        if (inputSize < 1 || outputSize < 1) {
            state = UNINITIALIZED;
            throw new Error("input or output block size < 1");
        }
        buffer = (!implBuffering && inputSize > 1) ? new byte[inputSize] : null;
        buffered = 0;

        if (padding != null)
            padding.engineSetBlockSize(inputSize);
    }

    /**
     * Initializes this cipher for decryption, using the specified
     * key. A successful call to this method puts the cipher in the
     * DECRYPT state. This method may be called on a cipher in any
     * state. Any state information (key, feedback buffer, ...) is
     * lost and reset.
     *
     * @param  key  the key to use for decryption.
     * @exception NullPointerException if key == null
     * @exception KeyException if the key is invalid.
     */
    public final void initDecrypt(Key key) throws KeyException {
        if (key == null) throw new NullPointerException("key == null");
        if (tracing) traceVoidMethod("engineInitDecrypt(<" + key + ">)");
        engineInitDecrypt(key);
        state = DECRYPT;

        inputSize = engineCiphertextBlockSize();
        outputSize = enginePlaintextBlockSize();
        if (inputSize < 1 || outputSize < 1) {
            state = UNINITIALIZED;
            throw new Error("input or output block size < 1");
        }
        buffer = (!implBuffering && inputSize > 1) ? new byte[inputSize] : null;
        buffered = 0;

        if (padding != null)
            padding.engineSetBlockSize(outputSize);
    }

    /**
     * Encrypts or decrypts the specified array of data, which is not the
     * final data in this stream. For block ciphers, if the last block so
     * far is incomplete, it will be buffered and processed in subsequent
     * calls to <code>update</code> or <code>crypt</code>.
     * <p>
     * Whether the data is encrypted or decrypted depends on the cipher's
     * initialization state. This method will automatically allocate
     * an output buffer of the right size.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong> However,
     * an equivalent method is declared in the JCE 1.2 preview documentation
     * for <samp>javax.crypto.Cipher</samp>.
     *
     * @param  in   the input data.
     * @return the encryption or decryption result.
     * @exception NullPointerException if in == null
     */
    public final byte[] update(byte[] in) {
        return update(in, 0, in.length);
    }

    /**
     * Encrypts or decrypts the specified subarray of data, which is not the
     * final data in this stream. For block ciphers, if the last block so
     * far is incomplete, it will be buffered and processed in subsequent
     * calls to <code>update</code> or <code>crypt</code>.
     * <p>
     * Whether the data is encrypted or decrypted depends on the cipher's
     * initialization state. This method will automatically allocate
     * an output buffer of the right size.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong> However,
     * an equivalent method is declared in the JCE 1.2 preview documentation
     * for <samp>javax.crypto.Cipher</samp>.
     *
     * @param  in       the input data.
     * @param  offset   the offset indicating where the subarray starts in the
     *                  <i>in</i> array.
     * @param  length   the length of the subarray.
     * @return the encryption or decryption result.
     * @exception NullPointerException if in == null
     * @exception IllegalArgumentException if length < 0
     * @exception ArrayIndexOutOfBoundsException if offset < 0 ||
     *                  (long) offset + length > in.length
     */
    public final byte[] update(byte[] in, int offset, int length) {
        byte[] out = new byte[outBufferSizeInternal(length, false)];
        int outlen = updateInternal(in, offset, length, out, 0, false);
        if (outlen != out.length) {
            byte[] newout = new byte[outlen];
            System.arraycopy(out, 0, newout, 0, outlen);
            return newout;
        } else
            return out;
    }

    /**
     * Encrypts or decrypts the specified subarray of data, which is not the
     * final data in this stream, and places the result in the specified
     * output buffer (starting at offset 0). For block ciphers, if the last
     * block so far is incomplete, it will be buffered and processed in
     * subsequent calls to <code>update</code> or <code>crypt</code>.
     * <p>
     * Whether the data is encrypted or decrypted depends on the cipher's
     * initialization state. <code>out.length</code> must be at least
     * <code>outBufferSize(inLen)</code>, otherwise an
     * ArrayIndexOutOfBoundsException will be thrown (in this case it is
     * not specified how much, if any, of the output will have been written).
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong> However,
     * an equivalent method is declared in the JCE 1.2 preview documentation
     * for <samp>javax.crypto.Cipher</samp>.
     *
     * @param  in           the input data.
     * @param  inOffset     the offset indicating where the subarray starts in
     *                      the <i>in</i> array.
     * @param  inLen        the length of the subarray.
     * @param  out          the output buffer.
     * @return the number of bytes written.
     * @exception NullPointerException if in == null || out == null
     * @exception IllegalArgumentException if inLen < 0
     * @exception ArrayIndexOutOfBoundsException if inOffset < 0 ||
     *                      outOffset < 0 || (long) inOffset + inLen > in.length ||
     *                      outBufferSize(inLen) > out.length
     */
    public final int
    update (byte[] in, int inOffset, int inLen, byte[] out) {
        return updateInternal(in, inOffset, inLen, out, 0, false);
    }

    /**
     * Encrypts or decrypts the specified subarray of data, which is not the
     * final data in this stream, and places the result in the specified
     * output buffer. For block ciphers, if the last block so far is
     * incomplete, it will be buffered and processed in subsequent calls to
     * <code>update</code> or <code>crypt</code>.
     * <p>
     * Whether the data is encrypted or decrypted depends on the cipher's
     * initialization state. <code>out.length</code> must be at least
     * <code>(long) outOffset + outBufferSize(inLen)</code>, otherwise an
     * ArrayIndexOutOfBoundsException will be thrown (in this case it is
     * not specified how much, if any, of the output will have been written).
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong> However,
     * an equivalent method is declared in the JCE 1.2 preview documentation
     * for <samp>javax.crypto.Cipher</samp>.
     *
     * @param  in           the input data.
     * @param  inOffset     the offset indicating where the subarray starts in
     *                      the <i>in</i> array.
     * @param  inLen        the length of the subarray.
     * @param  out          the output buffer.
     * @param  outOffset    the offset indicating where to start writing the
     *                      result into the output buffer.
     * @return the number of bytes written.
     * @exception NullPointerException if in == null || out == null
     * @exception IllegalArgumentException if inLen < 0
     * @exception ArrayIndexOutOfBoundsException if inOffset < 0 ||
     *                      outOffset < 0 || (long) inOffset + inLen > in.length ||
     *                      (long) outOffset + outBufferSize(inLen) > out.length
     */
    public final int
    update (byte[] in, int inOffset, int inLen, byte[] out, int outOffset) {
        return updateInternal(in, inOffset, inLen, out, outOffset, false);
    }

    /**
     * Encrypts or decrypts the specified array of data, which will be
     * automatically padded/unpadded as necessary.
     * <p>
     * Whether the data is encrypted or decrypted depends on the cipher's
     * initialization state. This method will automatically allocate
     * an output buffer of the right size.
     * <p>
     * If the final block is incomplete, the cipher must have a padding scheme
     * other than "NONE", and it must be in the ENCRYPT state. If this is not
     * the case, an IllegalBlockSizeException will be thrown.
     * <p>
     * If the cipher is in the DECRYPT state and padding is being used,
     * at least one full ciphertext block should be passed to <code>crypt</code>.
     * This is necessary because the last block contains information needed to
     * determine the length of the original plaintext.
     *
     * @param  in   the input data.
     * @return the encryption or decryption result.
     * @exception NullPointerException if in == null
     * @exception IllegalBlockSizeException if the final block cannot be
     *              padded or unpadded.
     */
    public final byte[] crypt(byte[] in)
    throws IllegalBlockSizeException {
        return crypt(in, 0, in.length);
    }

    /**
     * Encrypts or decrypts the specified array of data, which will be
     * automatically padded/unpadded as necessary.
     * <p>
     * Whether the data is encrypted or decrypted depends on the cipher's
     * initialization state. This method will automatically allocate 
     * an output buffer of the right size.
     * <p>
     * If the final block is incomplete, the cipher must have a padding scheme
     * other than "NONE", and it must be in the ENCRYPT state. If this is not
     * the case, an IllegalBlockSizeException will be thrown.
     * <p>
     * If the cipher is in the DECRYPT state and padding is being used,
     * at least one full ciphertext block should be passed to <code>crypt</code>.
     * This is necessary because the last block contains information needed to
     * determine the length of the original plaintext.
     *
     * @param  in      the input data.
     * @param  offset  the offset indicating where the subarray starts in the
     *                 <i>in</i> array.
     * @param  length  the length of the subarray.
     * @return the encryption or decryption result.
     * @exception NullPointerException if in == null
     * @exception IllegalArgumentException if length < 0
     * @exception ArrayIndexOutOfBoundsException if offset < 0 ||
     *                 (long) offset + length > in.length
     * @exception IllegalBlockSizeException if the final block cannot be padded
     *                 or unpadded.
     */
    public final byte[] crypt(byte[] in, int offset, int length)
    throws IllegalBlockSizeException {
        byte[] out = new byte[outBufferSizeInternal(length, true)];
        int outlen = updateInternal(in, offset, length, out, 0, true);
        if (outlen != out.length) {
            byte[] newout = new byte[outlen];
            System.arraycopy(out, 0, newout, 0, outlen);
            return newout;
        } else
            return out;
    }

    /**
     * Encrypts or decrypts the specified subarray of data, pads or unpads
     * it as necessary, and places the result in the specified output buffer.
     * <p>
     * Whether the data is encrypted or decrypted depends on the cipher's
     * initialization state.
     * <p>
     * If the final block is incomplete, the cipher must have a padding scheme
     * other than "NONE", and it must be in the ENCRYPT state. If this is not
     * the case, an IllegalBlockSizeException will be thrown.
     * <p>
     * If the cipher is in the DECRYPT state and padding is being used,
     * at least one full ciphertext block should be passed to <code>crypt</code>.
     * This is necessary because the last block contains information needed to
     * determine the length of the original plaintext.
     *
     * @param  in           the input data.
     * @param  inOffset     the offset indicating where the subarray starts in
     *                      the <i>in</i> array.
     * @param  inLen        the length of the subarray.
     * @param  out          the output buffer.
     * @param  outOffset    the offset indicating where to start writing the
     *                      result into the output buffer.
     * @return the number of bytes written.
     * @exception NullPointerException if in == null || out == null
     * @exception IllegalArgumentException if inLen < 0
     * @exception ArrayIndexOutOfBoundsException if inOffset < 0 ||
     *                      outOffset < 0 || (long) inOffset + inLen > in.length ||
     *                      (long) outOffset + outBufferSize(inLen) > out.length
     */
    public final int
    crypt(byte[] in, int inOffset, int inLen, byte[] out, int outOffset)
    throws IllegalBlockSizeException {
        return updateInternal(in, inOffset, inLen, out, outOffset, true);
    }

    /**
     * Equivalent to <code>crypt(in)</code>.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong> However,
     * an equivalent method is declared in the JCE 1.2 preview documentation
     * for <samp>javax.crypto.Cipher</samp>.
     */
    public final byte[] doFinal(byte[] in)
    throws IllegalBlockSizeException {
        return crypt(in, 0, in.length);
    }

    /**
     * Equivalent to <code>crypt(in, offset, length)</code>.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong> However,
     * an equivalent method is declared in the JCE 1.2 preview documentation
     * for <samp>javax.crypto.Cipher</samp>.
     */
    public final byte[] doFinal(byte[] in, int offset, int length)
    throws IllegalBlockSizeException {
        return crypt(in, offset, length);
    }

    /**
     * Equivalent to <code>crypt(in, inOffset, inLen, out, 0)</code>.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong> However,
     * an equivalent method is declared in the JCE 1.2 preview documentation
     * for <samp>javax.crypto.Cipher</samp>.
     */
    public final int
    doFinal(byte[] in, int inOffset, int inLen, byte[] out)
    throws IllegalBlockSizeException {
        return crypt(in, inOffset, inLen, out, 0);
    }

    /**
     * Equivalent to <code>crypt(in, inOffset, inLen, out, outOffset)</code>.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong> However,
     * an equivalent method is declared in the JCE 1.2 preview documentation
     * for <samp>javax.crypto.Cipher</samp>.
     */
    public final int
    doFinal(byte[] in, int inOffset, int inLen, byte[] out, int outOffset)
    throws IllegalBlockSizeException {
        return crypt(in, inOffset, inLen, out, outOffset);
    }

    /**
     * Internal method to take into account padding and buffering before
     * calling engineOutBufferSize. (engineOutBufferSize should not be
     * called other than via this method).
     *
     * @param inLen     the number of bytes of input
     * @param isFinal   whether the last block is to be padded
     * @return the length in bytes of the output block
     * @exception IllegalArgumentException if inLen < 0
     */
    private int outBufferSizeInternal(int inLen, boolean isFinal) {
        if (inLen < 0) throw new IllegalArgumentException("inLen < 0");
        if (!implBuffering) {
            inLen += buffered;
            int remainder = inLen % inputSize;
            inLen -= remainder;
            if (isFinal && state == ENCRYPT &&
                (padding != null || remainder > 0))
                inLen += inputSize;
        }
        if (inLen < 0) IJCE.reportBug("inLen < 0");
        if (tracing) traceMethod("engineOutBufferSize(" + inLen + ", " + isFinal + ")");
        int result = engineOutBufferSize(inLen, isFinal);
        if (tracing) traceResult(result);
        return result;
    }

    /**
     * Internal method to take into account padding and buffering before
     * calling engineInBufferSize. (engineInBufferSize should not be
     * called other than via this method).
     *
     * @param outLen    the number of bytes of output
     * @param isFinal   whether the last block is to be padded
     * @return the minimum number of bytes of input
     * @exception IllegalArgumentException if outLen < 0
     */
    private int inBufferSizeInternal(int outLen, boolean isFinal) {
        if (!implBuffering) {
            int remainder = outLen % outputSize;
            if (remainder > 0) outLen += outputSize - remainder;
        }
        if (tracing) traceMethod("engineInBufferSize(" + outLen + ", " + isFinal + ")");
        int result = engineInBufferSize(outLen, isFinal);
        if (tracing) traceResult(result);
        if (!implBuffering) {
            if (isFinal && state == ENCRYPT && padding != null)
                result -= inputSize;
            result -= buffered;
        }
        if (result < 0) result = 0;
        return result;
    }

    /**
     * This method handles buffering, padding, and calling the cipher's
     * engineUpdate and engineCrypt methods.
     */
    private int updateInternal(byte[] in, int inOffset, int inLen, byte[] out,
                               int outOffset, boolean isFinal) {
if (DEBUG && debuglevel >= 5 && tracing) traceMethod("updateInternal(<" + dump(in) + ">, " + inOffset + ", " + inLen + ", <" + dump(out) + ">, " + outOffset + ", " + isFinal + ")");
boolean exception = false;
int outStart = outOffset;
try {

        if (state == UNINITIALIZED) throw new IllegalStateException("cipher uninitialized");
        if (inLen < 0) throw new IllegalArgumentException("inLen < 0");
        if (inOffset < 0 || outOffset < 0 || (long)inOffset+inLen > in.length) {
if (DEBUG && debuglevel >= 1) debug("inOffset = " + inOffset + ", inLen = " + inLen + ", outOffset = " + outOffset + ", in.length = " + in.length);
            throw new ArrayIndexOutOfBoundsException(
                "inOffset < 0  || outOffset < 0 || (long)inOffset+inLen > in.length");
        }
        // if (in == null) exception has already been thrown.
        if (out == null) throw new NullPointerException();

//        int outStart = outOffset;

        if (buffer == null) {
            if (tracing) {
                traceMethod("engineUpdate(<" + dump(in) + ">, " + inOffset + ", " + inLen + ", <" + dump(out) + ">, " + outOffset + ")");
                int result = engineUpdate(in, inOffset, inLen, out, outOffset);
                traceResult(result);
                outOffset += result;
                if (isFinal && implBuffering) {
                    traceMethod("engineCrypt(<" + dump(out) + ">, " + outOffset + ")");
                    result = engineCrypt(out, outOffset);
                    traceResult(result);
                    outOffset += result;
                }
            } else {
                outOffset += engineUpdate(in, inOffset, inLen, out, outOffset);
                if (isFinal && implBuffering)
                     outOffset += engineCrypt(out, outOffset);
            }
            return outOffset-outStart;
        }

        // Avoid overlapping input and output regions.
        if (in == out) {
            if ((outOffset >= inOffset && outOffset < (long)inOffset+inLen) ||
                (inOffset >= outOffset && inOffset < (long)outOffset+
                     outBufferSizeInternal(inLen, isFinal))) {
                byte[] newin = new byte[inLen];
                System.arraycopy(in, inOffset, newin, 0, inLen);
                in = newin;
                inOffset = 0;
            }
        }

        if (isFinal) {
            if (state == ENCRYPT) {
                // Simulate calling update. After this, the data to be padded will
                // be in the buffer.
                outOffset += updateInternal(in, inOffset, inLen, out, outOffset, false);

                // If no padding scheme is set, there should be no data to pad.
                if (padding == null) {
                    if (buffered > 0) {
                        buffered = 0;
                        throw new IllegalBlockSizeException(getAlgorithm() +
                            ": Non-padding cipher in ENCRYPT state with an incomplete final block");
                    }
                    return outOffset-outStart;
                }

                // Pad the buffer in-place (there should be enough room).
                padding.pad(buffer, 0, buffered);
                buffered = 0;

                // Encrypt it.
                if (tracing) traceMethod("engineUpdate(<" + dump(buffer) + ">, 0, " + inputSize + ", <" + dump(out) + ">, " + outOffset + ")");
                int result = engineUpdate(buffer, 0, inputSize, out, outOffset);
                if (tracing) traceResult(result);
                outOffset += result;
                return outOffset-outStart;
            } else if (padding != null) {
                if (inLen == 0) return 0;
//                    throw new IllegalBlockSizeException(getAlgorithm() +
//                        ": Cipher in DECRYPT state with an incomplete final block");

                // Simulate calling update on all but the last byte.
                outOffset += updateInternal(in, inOffset, inLen-1, out, outOffset, false);

                // The amount of buffered data should now be inputSize-1.
                if (buffered != inputSize-1) {
                    buffered = 0;
                    throw new IllegalBlockSizeException(getAlgorithm() +
                        ": Cipher in DECRYPT state with an incomplete final block");
                }

                // Copy the last byte.
                buffer[buffered] = in[inOffset+inLen-1];
                buffered = 0;

                // Decrypt the buffer.
                byte[] temp = new byte[outBufferSizeInternal(inputSize, false)];
                if (tracing) traceMethod("engineUpdate(<" + dump(buffer) + ">, 0, " + inputSize + ", <" + dump(temp) + ">, 0)");
                int result = engineUpdate(buffer, 0, inputSize, temp, 0);
                if (tracing) traceResult(result);

                // Unpad the resulting block, and copy it to the output array.
                int len = padding.unpad(temp, 0, temp.length);
                System.arraycopy(temp, 0, out, outOffset, len);
                outOffset += len;
                return outOffset-outStart;
            }
            /* state == DECRYPT && padding == null: fall through */
        }

        if (buffered > 0) {
            // There is data currently buffered. Test whether the buffered
            // data and the supplied data together make up at least one
            // block.
            if ((long)inLen+buffered < inputSize) {
                // no, so nothing to crypt
                System.arraycopy(in, inOffset, buffer, buffered, inLen);
                buffered += inLen;
                return 0;
            }
            // Complete the buffered block and crypt it.
            int remainder = inputSize-buffered;
            System.arraycopy(in, inOffset, buffer, buffered, remainder);
            inOffset += remainder;
            inLen -= remainder;
            if (tracing) traceMethod("engineUpdate(<" + dump(buffer) + ">, 0, " + inputSize + ", <" + dump(out) + ">, " + outOffset + ")");
            int result = engineUpdate(buffer, 0, inputSize, out, outOffset);
            if (tracing) traceResult(result);
            outOffset += result;
        }

        // Buffer anything after the last supplied complete block.
        buffered = inLen % inputSize;
        if (buffered > 0) {
            System.arraycopy(in, inOffset+inLen-buffered, buffer, 0, buffered);
            inLen -= buffered;
        }

        // inLen is now a multiple of inputSize.
        while (inLen > 0) {
            if (tracing) traceMethod("engineUpdate(<" + dump(in) + ">, " + inOffset + ", " + inputSize + ", <" + dump(out) + ">, " + outOffset + ")");
            int result = engineUpdate(in, inOffset, inputSize, out, outOffset);
            if (tracing) traceResult(result);
            outOffset += result;
            inOffset += inputSize;
            inLen -= inputSize;
        }
        return outOffset-outStart;

} catch (RuntimeException e) {
    if (tracing) e.printStackTrace();
    exception = true;
    throw e;
} finally {
    if (DEBUG && debuglevel >= 5 && tracing && !exception) traceResult(outOffset-outStart);
}
    }

    /**
     * Sets the specified algorithm parameter to the specified value.
     * <p>
     * This method supplies a general-purpose mechanism through which it is
     * possible to set the various parameters of this object. A parameter may
     * be any settable parameter for the algorithm, such as block size,
     * a source of random bits for IV generation (if appropriate), or an
     * indication of whether or not to perform a specific but optional
     * computation. A uniform algorithm-specific naming scheme for each
     * parameter is desirable but left unspecified at this time.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @param  param    the string identifier of the parameter.
     * @param  value    the parameter value.
     * @exception NullPointerException if param == null
     * @exception NoSuchParameterException if there is no parameter with name
     *                  param for this cipher implementation.
     * @exception InvalidParameterException if the parameter exists but cannot
     *                  be set (for example because the cipher is in the
     *                  wrong state).
     * @exception InvalidParameterTypeException if value is the wrong type
     *                  for this parameter.
     */
    public void setParameter(String param, Object value)
    throws NoSuchParameterException, InvalidParameterException,
           InvalidParameterTypeException {
        if (param == null) throw new NullPointerException("param == null");
        if (tracing) traceVoidMethod("engineSetParameter(\"" + param + "\", <" + value + ">)");
        engineSetParameter(param, value);
    }

    /**
     * Gets the value of the specified algorithm parameter.
     * <p>
     * This method supplies a general-purpose mechanism through which it
     * is possible to get the various parameters of this object. A parameter
     * may be any settable parameter for the algorithm, such as block size,
     * a source of random bits for IV generation (if appropriate), or an
     * indication of whether or not to perform a specific but optional
     * computation. A uniform algorithm-specific naming scheme for each
     * parameter is desirable but left unspecified at this time.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @param  param    the string name of the parameter.
     * @return the object that represents the parameter value.
     * @exception NullPointerException if param == null
     * @exception NoSuchParameterException if there is no parameter with name
     *                  param for this cipher implementation.
     * @exception InvalidParameterException if the parameter exists but cannot
     *                  be read.
     */
    public Object getParameter(String param)
    throws NoSuchParameterException, InvalidParameterException {
        if (param == null) throw new NullPointerException("param == null");
        if (tracing) traceMethod("engineGetParameter(\"" + param + "\")");
        Object result = engineGetParameter(param);
        if (tracing) traceResult("<" + result + ">");
        return result;
    }

    /**
     * Returns a clone of this cipher.
     * <p>
     * Note: In JavaSoft's version of JCE, <code>Cipher.clone()</code> is
     * protected. This is not very useful, since then an application (as opposed
     * to the cipher implementation itself) is not able to call it.
     *
     * @exception CloneNotSupportedException if the cipher is not cloneable.
     */
    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        } else {
            throw new CloneNotSupportedException();
        }
    }

    public String toString() {
        return "Cipher [" + getProvider() + " " +
            getAlgorithm() + "/" + getMode() + "/" + getPadding() + "]";
    }


// SPI methods
//............................................................................

    /**
     * <b>SPI</b>: Sets the object that will implement padding for this cipher.
     * <p>
     * Cipher implementations may override this method in order to be notified
     * when the padding scheme is set (in this case they should always call
     * <code>super.engineSetPaddingScheme(padding)</code>). Normally, overriding
     * this method is not required.
     *
     * @exception IllegalStateException if the cipher is already initialized.
     */
    protected void engineSetPaddingScheme(PaddingScheme padding) {
        if (state != UNINITIALIZED)
            throw new IllegalStateException("Cipher is already initialized");
        this.padding = padding;
    }

    /**
     * <b>SPI</b>: Returns the length of a block, in bytes. Ciphers for
     * which plaintext and ciphertext blocks are the same size may override
     * this method. Otherwise, both enginePlaintextBlockSize and
     * engineCiphertextBlockSize should be overridden.
     * <p>
     * The value may change when <code>initEncrypt</code> or
     * <code>initDecrypt</code> is called, but it should not change at
     * other times.
     *
     * @return the length in bytes of a block for this cipher.
     */
    protected int engineBlockSize() {
        throw new Error(
            "cipher classes must implement either engineBlockSize, or " +
            "enginePlaintextBlockSize and engineCiphertextBlockSize");
    }

    /**
     * <b>SPI</b>: Returns the length of a plaintext block, in bytes.
     * For byte-oriented stream ciphers, this method should return 1.
     * <p>
     * The value may change when <code>initEncrypt</code> or
     * <code>initDecrypt</code> is called, but it should not change at
     * other times.
     * <p>
     * The default implementation returns <code>engineBlockSize()</code>.
     *
     * @return the length in bytes of a plaintext block for this cipher.
     */
    protected int enginePlaintextBlockSize() { return engineBlockSize(); }

    /**
     * <b>SPI</b>: Returns the length of a ciphertext block, in bytes.
     * For byte-oriented stream ciphers, this method should return 1.
     * <p>
     * The value may change when <code>initEncrypt</code> or
     * <code>initDecrypt</code> is called, but it should not change at
     * other times.
     * <p>
     * The default implementation returns <code>engineBlockSize()</code>.
     *
     * @return the length in bytes of a ciphertext block for this cipher.
     */
    protected int engineCiphertextBlockSize() { return engineBlockSize(); }

    /**
     * <b>SPI</b>: Returns the length of output buffer required for a given
     * length of input, in bytes. <code>isFinal</code> is true when this
     * is the final block of input.
     * <p>
     * If <code>implBuffering</code> is false, the <code>inLen</code>
     * parameter already takes into account the length of any required
     * padding, and buffered data. In this case <code>inLen</code> will be
     * a multiple of the input block size (this may only be true for IJCE,
     * not for other implementations of JCE).
     * <p>
     * The default implementation assumes that the output will have the
     * same number of blocks as the input (i.e. the result is
     * <code>inLen / getInputBlockSize() * getOutputBlockSize()</code>).
     * <p>
     * You will need to override this method if the cipher handles its
     * own buffering.
     *
     * @param  inLen    the number of bytes of input
     * @param  isFinal  whether the last block is to be padded
     * @return the length in bytes of the output block
     */
    protected int engineOutBufferSize(int inLen, boolean isFinal) {
        return inLen / inputSize * outputSize;
    }

    /**
     * <b>SPI</b>: Returns the minimum number of bytes of input, that
     * will cause an output of <i>outLen</i> bytes from a call to
     * <code>engineUpdate</code> (when <i>isFinal</i> is false), or
     * successive calls to both <code>engineUpdate</code> and
     * <code>engineCrypt</code> (when <i>isFinal</i> is true).
     * <p>
     * If <code>implBuffering</code> is false, this method need not take
     * into account padding or buffered data when calculating the result.
     * In this case <code>outLen</code> will be a multiple of the output
     * block size (this may only be true for IJCE, not for other
     * implementations of JCE).
     * <p>
     * The default implementation assumes that the output will have the
     * same number of blocks as the input (i.e. the result is
     * <code>outLen / getOutputBlockSize() * getInputBlockSize()</code>).
     * <p>
     * You will need to override this method if the cipher handles its
     * own buffering.
     *
     * @param  outLen   the number of bytes of output
     * @param  isFinal  whether the input includes the last block
     * @return the length in bytes of the output block
     */
    protected int engineInBufferSize(int outLen, boolean isFinal) {
        return outLen / outputSize * inputSize;
    }

    /**
     * <b>SPI</b>: Initializes this cipher for encryption, using the
     * specified key.
     * <p>
     * After a call to this method, the cipher's state is set to ENCRYPT.
     *
     * @param key   the key to use for encryption.
     * @exception   KeyException if the key is invalid.
     */
    protected abstract void engineInitEncrypt(Key key) throws KeyException;

    /**
     * <b>SPI</b>: Initializes this cipher for decryption, using the
     * specified key.
     * <p>
     * After a call to this method, the cipher's state is set to DECRYPT.
     *
     * @param key   the key to use for decryption.
     * @exception   KeyException if the key is invalid.
     */
    protected abstract void engineInitDecrypt(Key key) throws KeyException;

    /**
     * <b>SPI</b>: This is the main engine method for updating data.
     * <p>
     * When <code>implBuffering</code> is true, this method will be called
     * with the same data that is passed to <code>update</code> or
     * <code>crypt</code> (i.e. there will be exactly one call to
     * <code>engineUpdate</code> for each call to <code>update</code> or
     * <code>crypt</code>).
     * <p>
     * When <code>implBuffering</code> is false, the Cipher superclass will
     * ensure that when it calls this method, <i>inLen</i> is a non-negative
     * multiple of <code>getInputBlockSize()</code>.
     * <p>
     * <i>in</i> and <i>out</i> may be the same array, and the input and
     * output regions may overlap. A cipher implementation <strong>should
     * not</strong> use any part of <i>in</i> or <i>out</i> as working storage
     * for intermediate steps of the algorithm. It should copy only the final
     * result into the <i>out</i> array.
     * <p>
     * SECURITY: if array arguments are passed to native code, the
     * implementation must ensure that a buffer overflow or illegal memory
     * access cannot occur, <strong>regardless of the arguments passed to
     * this method</strong>. I.e. the native library should not be called if:
     * <pre>
     *   in == null || out == null || inLen < 0 || inLen % (block size) != 0 ||
     *   inOffset < 0 || (long)inOffset + inLen > in.length ||
     *   outOffset < 0 || (long)outOffset + (number of bytes to be written) > out.length
     * </pre>
     * <p>
     * Note that the <code>(long)</code> casts are essential, because
     * <i>inOffset</i> or <i>outOffset</i> could be close to
     * <code>Integer.MAX_VALUE</code>. The native method being called
     * should be private. This is important because untrusted code could
     * subclass the cipher implementation, and call this method directly
     * with any arguments.
     *
     * @param  in           the input data.
     * @param  inOffset     the offset into <i>in</i> specifying where the
     *                      data starts.
     * @param  inLen        the length of the subarray.
     * @param  out          the output array.
     * @param  outOffset    the offset indicating where to start writing into
     *                      the <i>out</i> array.
     * @return the number of bytes written.
     */
    protected abstract int
    engineUpdate(byte[] in, int inOffset, int inLen, byte[] out, int outOffset);

    /**
     * <b>SPI</b>: Process data buffered by this cipher implementation.
     * <p>
     * This method is overriden by ciphers that handle their own buffering
     * (i.e. <code>implBuffering == true</code>). It should flush the internal
     * buffer, and process any remaining data. By default, this method
     * returns 0.
     * <p>
     * Ciphers that pass <code>implBuffering == false</code> to the superclass
     * constructor need not override this method.
     * <p>
     * SECURITY: if array arguments are passed to native code, the
     * implementation must ensure that a buffer overflow or illegal memory
     * access cannot occur, <strong>regardless of the arguments passed to
     * this method</strong>. I.e. the native library should not be called if:
     * <pre>
     *   out == null || outOffset < 0 ||
     *   (long)outOffset + (number of bytes to be written) > out.length
     * </pre>
     * <p>
     * Note that the <code>(long)</code> cast is essential, because
     * <i>outOffset</i> could be close to <code>Integer.MAX_VALUE</code>.
     * The native method being called should be private. This is important
     * because untrusted code could subclass the cipher implementation, and
     * call this method directly with any arguments.
     *
     * @param  out  the output buffer into which to write the result.
     * @return the number of bytes written.
     */
    protected int engineCrypt(byte[] out, int outOffset) {
        return 0;
    }

    /**
     * <b>SPI</b>: Sets the specified algorithm parameter to the specified
     * value.
     * <p>
     * This method supplies a general-purpose mechanism through which it is
     * possible to set the various parameters of this object. A parameter may
     * be any settable parameter for the algorithm, such as block size,
     * a source of random bits for IV generation (if appropriate), or an
     * indication of whether or not to perform a specific but optional
     * computation. A uniform algorithm-specific naming scheme for each
     * parameter is desirable but left unspecified at this time.
     * <p>
     * The default implementation always throws a NoSuchParameterException.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @param  param    the string name of the parameter. 
     * @param  value    the parameter value.
     * @exception NoSuchParameterException if there is no parameter with name
     *                  param for this cipher implementation.
     * @exception InvalidParameterException if the parameter exists but cannot
     *                  be set (for example because the cipher is in the
     *                  wrong state).
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
     * possible to get the various parameters of this object. A parameter may
     * be any settable parameter for the algorithm, such as block size, a source
     * of random bits for IV generation (if appropriate), or an indication of
     * whether or not to perform a specific but optional computation. A
     * uniform algorithm-specific naming scheme for each parameter is desirable
     * but left unspecified at this time.
     * <p>
     * The default implementation always throws a NoSuchParameterException.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @param  param    the string name of the parameter. 
     * @return the object that represents the parameter value.
     * @exception NoSuchParameterException if there is no parameter with name
     *                  param for this cipher implementation.
     * @exception InvalidParameterException if the parameter exists but cannot
     *                  be read.
     */
    protected Object engineGetParameter(String param)
    throws NoSuchParameterException, InvalidParameterException {
        throw new NoSuchParameterException(getAlgorithm() + ": " + param);
    }


// IJCE additional static methods
//............................................................................

    /**
     * Gets the standard names of all Ciphers implemented by a provider.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     * For compatibility you may wish to use
     * <code><a href="java.security.IJCE.html#getAlgorithms(java.security.Provider, java.lang.String)">
     * IJCE.getAlgorithms</a>(provider, "Cipher")</code> instead.
     *
     * @since IJCE 1.0.1
     */
    public static String[] getAlgorithms(Provider provider) {
        return IJCE.getAlgorithms(provider, "Cipher");
    }

    /**
     * Gets the standard names of all Ciphers implemented by any
     * installed provider. Algorithm names are not duplicated if
     * they are supported by more than one provider (but specialized
     * cipher/mode implementations are treated as distinct, for
     * example "DES/CBC" is different from "DES").
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     * For compatibility you may wish to use
     * <code><a href="java.security.IJCE.html#getAlgorithms(java.lang.String)">
     * IJCE.getAlgorithms</a>("Cipher")</code> instead.
     *
     * @since IJCE 1.0.1
     */
    public static String[] getAlgorithms() {
        return IJCE.getAlgorithms("Cipher");
    }
}
