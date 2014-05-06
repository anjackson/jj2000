/*
// $Log: FeedbackCipher.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:30  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.2  2000/08/17 11:35:24  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.1.1.1  1997/11/03 22:36:57  hopwood
// + Imported to CVS (tagged as 'start').
//
// $Endlog$
*/

package xjava.security;

/**
 * An interface implemented by algorithms that use feedback.  Examples
 * include block ciphers in <code>CBC</code>, <code>CFB</code>, or
 * <code>OFB</code> mode. <p>
 * This interface is used to set or get the initialization vector,
 * which is the data first used as feedback. <p>
 * Note that initialization vectors are not secret, and should not 
 * be used for data (such as cipher keys) where secrecy is required.
 */
public interface FeedbackCipher {
    /**
     * Sets the initialization vector for this object. Note that 
     * FeedbackCiphers will usually default to a randomly generated 
     * vector if none is provided.
     * <p>This method may only be called on an uninitialized cipher 
     * (one in the <code>UNINITIALIZED</code> state) that implements 
     * FeedbackCipher.
     *
     * @param iv  the initialization vector.
     * @exception InvalidParameterException if the initialization vector
     *          is of the wrong length or otherwise invalid.
     */
    void setInitializationVector(byte iv[]);

    /**
     * Gets the initialization vector for this object. This may be
     * called on a cipher implementing FeedbackCipher. It will return
     * <code>null</code> if the initialization vector has not been set.
     *
     * @return the initialization vector for this cipher object.
     */
    byte[] getInitializationVector();

    /**
     * Returns the size of the initialization vector expected by
     * <code>setInitializationVector</code>.
     *
     * @return the required size of the argument to
     *          <code>setInitializationVector</code>.
     */
    int getInitializationVectorLength();
}
