// $Id: CipherOutputStream.java,v 1.1.1.1 2002/08/27 11:49:30 grosbois Exp $
//
// $Log: CipherOutputStream.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:30  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.6  2000/08/17 11:35:24  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.5  2000/04/25 15:07:30  edwin
// Ouch, this class was horribly broken (at least on JDK 1.2, but possibly also on later JDK 1.1 versions)
// This problem was already reported on 18 June 1999(!) by Sampo Pasanen on the -users list
//
// Revision 1.4  1999/07/13 18:10:42  edwin
// A little difference in the implementation of FilterOutputStream.write(byte[], int, int) between JDK1.1 and JDK1.2 made this class to go into an endless loop.
//
// Revision 1.3  1997/12/01 03:37:28  hopwood
// + Committed changes below.
//
// Revision 1.2.1  1997/11/30  hopwood
// + More debugging calls added.
//
// Revision 1.2  1997/11/29 04:45:13  hopwood
// + Committed changes below.
//
// Revision 1.1.1.1  1997/11/03 22:36:57  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.1  1997/11/03  David Hopwood
// + Removed unused bufferSize variable.
// + Documentation changes.
//
// Revision 0.1.0.0  1997/??/??  David Hopwood
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package xjava.security;

import java.io.*;

/**
 * A FilterOutputStream that encrypts or decrypts the data passing
 * through it.
 * <p>
 * This class has a constructor that takes a Cipher and an output
 * stream as arguments. The cipher is used to encrypt or decrypt all
 * data supplied via calls to one of the <code>write</code> methods.
 * The encryption/decryption result is written to the output stream.
 * <p>
 * For block ciphers, a buffer is used for receiving the data to be
 * encrypted or decrypted. The maximum number of bytes that may be
 * buffered at any given time is given by the
 * <code><a href="#getBufferSize">getBufferSize</a></code> method.
 * For byte-oriented stream ciphers, no buffering is done (and
 * <code>getBufferSize()</code> returns 0).
 * <p>
 * To supply the bytes that need to be encrypted/decrypted, make one
 * or more calls to one of the <code>write</code> methods. After you
 * have supplied all the data, call <code>close()</code> to ensure
 * final processing is done.
 * <p>
 * Note: JavaSoft's JCE required calling <code>flush</code> for final
 * processing, rather than <code>close</code>. However, if you call
 * <code>flush</code> and then write more data, there will not be
 * sufficient information available when reading in the stream (e.g.
 * using <samp>CipherInputStream</samp>), to determine when the flush
 * happened. Unpadding can only work correctly if no further data is
 * written after the final processing, which means that <code>close</code>
 * is the right method to trigger this processing. I'm not sure whether
 * JavaSoft's implementation works if only <code>close</code> is called,
 * but calling <code>flush</code> followed by <code>close</code> should
 * work in both implementations.
 * <p>
 * With a cipher in the ENCRYPT state, the number of bytes not yet
 * encrypted and written to the stream is kept between 0 and
 * <code>cipher.getPlaintextBlockSize()-1</code> inclusive. When
 * <code>close</code> is called, the final data is padded, encrypted, and
 * the result written to the output stream. If the cipher's padding scheme
 * is NONE and the final data does not comprise a complete plaintext
 * block, an <samp>IllegalBlockSizeException</samp> is thrown.
 * <p>
 * With a cipher in the DECRYPT state, [DOCUMENT ME]. When <code>close</code>
 * is called, an exact number of ciphertext blocks should be in
 * the buffer (otherwise an <samp>IllegalBlockSizeException</samp> is
 * thrown). Those blocks are decrypted, unpadded, and written to the
 * output stream.
 * <p>
 * Note: calling methods of a cipher while it is being used by a
 * CipherInputStream (apart from methods that have no side-effects,
 * like <code>getAlgorithm()</code>, <code>get*BlockSize()</code>, etc.)
 * will probably result in incorrect or unexpected output.
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @see java.security.Cipher
 * @see java.security.Cipher#getInputBlockSize
 * @see java.security.Cipher#getOutputBlockSize
 * @see java.security.CipherInputStream
 */
public class CipherOutputStream
extends FilterOutputStream
{

// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = true;
    private static int debuglevel = DEBUG ? IJCE.getDebugLevel("CipherOutputStream") : 0;
    private static PrintWriter err = DEBUG ? IJCE.getDebugOutput() : null;
    private static void debug(String s) { err.println("CipherOutputStream: " + s); }
    private static String dump(byte[] b) {
        if (b == null) return "null";
//        if (b.length <= 32) return cryptix.util.core.Hex.toString(b);
        return b.toString();
    }


//...........................................................................

    private Cipher cipher;
    private final byte[] preallocated = new byte[256];
    
    /**
     * Constructs an output stream using a cipher that must be
     * initialized for either encryption or decryption, that is, a
     * cipher whose state is either <code>ENCRYPT</code> or 
     * <code>DECRYPT</code>.
     *
     * @param  os       the output stream.
     * @param  cipher   an initialized cipher.
     * @exception NullPointerException if os == null || cipher == null
     */
    public CipherOutputStream(OutputStream os, Cipher cipher) {
        super(os);
        if (cipher == null) throw new NullPointerException("cipher == null");

        int state = cipher.getState();
        if (state != Cipher.ENCRYPT && state != Cipher.DECRYPT)
            throw new IllegalStateException("cipher is uninitialized");

        this.cipher = cipher;
    }

    /**
     * Supplies bytes to be used for encryption or decryption, 
     * depending on the cipher state.
     *
     * @param  in       the buffer containing the bytes to be used for
     *                  encryption or decryption.
     * @param  offset   the offset into <i>in</i> indicating the location
     *                  of the first byte to be used.
     * @param  length   the number of bytes to read from <i>in</i>,
     *                  starting at offset <i>offset</i>.
     * @exception ArrayIndexOutOfBoundsException if (long)offset + length > in.length
     * @exception IOException if an error occurs, or the stream has already
     *          been closed.
     */
    public synchronized void write(byte[] in, int offset, int length)
    throws IOException {
        if (cipher == null) throw new IOException("stream closed");

        if (length <= 0) return;

        int outLen = cipher.outBufferSize(length);
        byte[] buf = (outLen <= preallocated.length) ? preallocated
                                                     : new byte[outLen];
        int n = cipher.update(in, offset, length, buf, 0);

if (DEBUG && debuglevel >= 7) debug("  buf = <" + dump(buf) + ">, n = " + n);
        
        for (int i=0; i<n; i++) {
            out.write(buf[i]);
        }
    }

    /**
     * Supplies a byte to be used for encryption or decryption,
     * depending on the cipher state.
     *
     * @param  b    the byte to be used for encryption or decryption.
     * @exception IOException if an error occurs.
     */
    public synchronized void write(int b)
    throws IOException {
        byte[] ba = { (byte) b };
        write(ba,0,1);
    }

    /**
     * Flushes the underlying output stream. Unlike JavaSoft's
     * implementation, this never writes any further data to the stream.
     * To make sure that all data has been written, call <code>close()</code>.
     *
     * @exception IOException if an error occurs.
     */
    public synchronized void flush()
    throws IOException {
        if (cipher == null) throw new IOException("stream closed");
        super.flush();
    }

    /**
     * Closes the output stream. Before it is closed, buffered data
     * is processed, taking into account padding, and the result is
     * written to the stream.
     * <p>
     * For an encrypter stream, any buffered data is encrypted and the 
     * result written to the output stream. If the padding scheme is
     * not "NONE", the data is padded before encryption. If the padding
     * scheme is "NONE" and the final data does not comprise a complete
     * block, an <samp>IllegalBlockSize</samp> exception is thrown.
     * <p>
     * For a decrypter stream, when <code>close</code> is called, 
     * exactly one block should be in the buffer. It is decrypted, unpadded,
     * and written out.
     *
     * @exception IOException if an error occurs.
     */
    public synchronized void close()
    throws IOException {
        if (cipher == null) throw new IOException("stream closed");

        int outLen = cipher.outBufferSizeFinal(0);
        byte[] buf = (outLen <= preallocated.length) ? preallocated
                                                     : new byte[outLen];
        int n = cipher.crypt(new byte[0], 0, 0, buf, 0);

if (DEBUG && debuglevel >= 7) debug("  buf = <" + dump(buf) + ">, n = " + n);
        for (int i=0; i<n; i++) {
            out.write(buf[i]);
        }
        super.flush();
if (DEBUG && debuglevel >= 5) debug("flushed stream");
        super.close();
if (DEBUG && debuglevel >= 5) debug("closed stream");
        cipher = null;
    }
}
