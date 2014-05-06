// $Id: CipherInputStream.java,v 1.1.1.1 2002/08/27 11:49:30 grosbois Exp $
//
// $Log: CipherInputStream.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:30  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.10  2000/08/17 11:35:24  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.9  1997/12/03 03:36:06  hopwood
// + Committed changes below.
//
// Revision 1.7.1  1997/12/02  hopwood
// + If the return value from read([B,I,I) would be 0 on the last read before
//   EOF, force it to -1.
// + outBuf was missing an index pointer, so if more than one consecutive
//   read was from outBuf, all but the first would return junk.
// + Added close() method, to be consistent with CipherOutputStream.
//
// Revision 1.7  1997/12/02 03:19:50  hopwood
// + Committed changes below.
//
// Revision 1.6.1  1997/12/02  hopwood
// + Previously buffered data was not being taken into account in the return
//   value of read([B,I,I).
// + The loop in readFully would sometimes not terminate.
// + Added tempBuf to hold the single byte in read(). Neither of the
//   preallocated arrays should be used for this, as they will be overwritten.
// + Changed read() to correctly handle a return value of 0 from read([B,I,I).
//
// Revision 1.6  1997/12/01 05:20:27  raif
// + Added test for cipher==null in available(). That method
//   is called by Reader subclasses even when the cipher object
//   is no more.
//
// Revision 1.5  1997/12/01 03:37:28  hopwood
// + Committed changes below.
//
// Revision 1.4.1  1997/11/30  hopwood
// + The previous implementation was broken because the cipher might output more
//   bytes than were requested as input from the stream. An extra buffer, outBuf,
//   is needed to fix this.
// + A single read from the underlying stream might not be sufficient; now we
//   repeat until the correct number of bytes is read.
// + "if (length < out.length - offset)" should be "if (length > out.length - offset)".
// + More debugging calls added.
//
// Revision 1.4  1997/11/29 04:45:12  hopwood
// + Committed changes below.
//
// Revision 1.3.1  1997/11/18  David Hopwood
// + Changed behaviour of read(I,I,I) for short output arrays back to
//   Raif's 1.2 revision.
//
// Revision 1.3  1997/11/21 04:31:18  hopwood
// + Committed changes below.
//
// Revision 1.2.2  1997/11/20  David Hopwood
// + Made skip and available synchronized.
//
// Revision 1.2.1  1997/11/18  David Hopwood
// + Reversed the change made in 1.2. If the client passes an array that
//   is not long enough for the requested number of bytes, it's a bug
//   in the client code, not an end-of-stream condition.
//
// Revision 1.2  1997/11/07 05:53:27  raif
// + Crippled the ArrayIndexOutOfBoundsException in read() since
//   semantically a return of -1 should/would indicate this.
//
// Revision 1.1.1.1  1997/11/03 22:36:57  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.2  1997/11/01  David Hopwood
// + Implemented skip, available, and markSupported methods.
// + Changed the buffering behaviour to distinguish padding block ciphers
//   in DECRYPT mode from other ciphers. This is to ensure that the minimum
//   amount is always buffered.
// + Updated documentation.
//
// Revision 0.1.0.1  1997/07/20  R. Naffah
// + Tests OK.
// + Fixed the read() methods.
//
// Revision 0.1.0.0  1997/??/??  David Hopwood
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team. All rights reserved.
 */

package xjava.security;

import java.io.*;

/**
 * A FilterInputStream that encrypts or decrypts the data passing through
 * it. Typically, this stream would be used as a filter to read
 * an encrypted file. It can also be used to encrypt network communications
 * (although this would normally require use of a stream cipher, or a block
 * cipher in a stream-like mode such as CFB or OFB).
 * <p>
 * This class has a constructor that takes an input stream and a Cipher
 * as arguments. The cipher is used to encrypt or decrypt all data read
 * through the stream. 
 * <p>
 * The data is encrypted or decrypted, depending on the initialisation
 * state of the Cipher. To get the encryption/decryption result bytes,
 * make one or more calls to the <code>read</code> methods. One 
 * <code><a href="#read()">read</a></code> method, with no arguments,
 * returns the next result byte. The other
 * <code><a href="#read(byte[], int, int)">read</a></code> method returns
 * multiple result bytes at once.
 * <p>
 * For block ciphers, some buffering of the data received from the input
 * stream is done. The buffer length is calculated as follows:
 * <pre>
 *    buffer length = cipher.getInputBlockSize() +
 *      (cipher.isPaddingBlockCipher() &&
 *       cipher.getState() == Cipher.DECRYPT) ? 1 : 0
 * </pre>
 * Each read operation will attempt to completely fill the buffer. The maximum
 * number of bytes that can remain unprocessed after each <code>read</code> call
 * is one less than the buffer length. In the case of a padding block cipher in
 * DECRYPT mode, this means that one full ciphertext block may remain
 * unprocessed, because it is necessary to read an extra byte in order to
 * determine whether this is the last block of the stream. (It would be
 * incorrect to process a block without making this check, since it may have
 * padding bytes that need to be stripped out.)
 * <p>
 * When EOF is reached for a padding/unpadding cipher in DECRYPT mode, the
 * the decryption result is unpadded. If EOF is encountered part-way through
 * a block, an <samp>IllegalBlockSizeException</samp> is thrown.
 * <p>
 * When EOF is reached for a padding/unpadding cipher in ENCRYPT mode, the
 * last block is padded before encryption. If the cipher does not support
 * padding and the last block is incomplete, an <samp>IllegalBlockSizeException</samp>
 * is thrown.
 * <p>
 * For stream ciphers, that is, ciphers capable of encrypting or decrypting 
 * a byte at a time, no buffering is necessary. 
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
 * @author  David Hopwood
 * @author  Raif S. Naffah
 * @since   IJCE 1.0.0
 *
 * @see java.security.Cipher
 * @see java.security.Cipher#getInputBlockSize
 * @see java.security.Cipher#getOutputBlockSize
 * @see java.security.CipherOutputStream
 */
public class CipherInputStream
extends FilterInputStream
{

// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = true;
    private static int debuglevel = DEBUG ? IJCE.getDebugLevel("CipherInputStream") : 0;
    private static PrintWriter err = DEBUG ? IJCE.getDebugOutput() : null;
    private static void debug(String s) { err.println("CipherInputStream: " + s); }
    private static String dump(byte[] b) {
        if (b == null) return "null";
//        if (b.length <= 32) return cryptix.util.core.Hex.toString(b);
        return b.toString();
    }


// Variables
//...........................................................................

    private Cipher cipher;
    private final byte[] preallocated1 = new byte[256];
    private final byte[] preallocated2 = new byte[256];
    private final byte[] tempByte = new byte[1];
    private byte[] outBuf;
    private int outPtr;
    private int buffered;
    private boolean isDPBC; // is a decrypting padding block cipher


// Constructor
//...........................................................................

    /**
     * Constructs an input stream using a cipher that must be
     * initialised for either encryption or decryption, that is, a
     * cipher whose state is either <code>ENCRYPT</code> or 
     * <code>DECRYPT</code>.
     *
     * @param  in       the input stream.
     * @param  cipher   an initialised cipher.
     * @exception NullPointerException if is == null || cipher == null
     * @exception IllegalStateException if cipher.getState() == UNINITIALIZED
     *
     * @see java.security.Cipher
     */
    public CipherInputStream(InputStream is, Cipher cipher) {
        super(is);
        if (cipher == null) throw new NullPointerException("cipher");

        int state = cipher.getState();
        if (state != Cipher.ENCRYPT && state != Cipher.DECRYPT)
            throw new IllegalStateException("cipher is uninitialized");

        outBuf = new byte[cipher.getOutputBlockSize()];
            // this is an estimate of the required length of outBuf; doesn't
            // matter if it is incorrect.
        outPtr = buffered = 0;

        isDPBC = (cipher.isPaddingBlockCipher() && state == Cipher.DECRYPT);
        this.cipher = cipher;
    }


// FilterInputStream methods
//...........................................................................

    /**
     * Fills up the specified bytes of the <i>out</i> array with the next
     * <i>len</i> encrypted or decrypted bytes (depending on the cipher state).
     *
     * @param  out      the byte array into which the encrypted or decrypted
     *                  bytes will be read.
     * @param  offset   the offset into <i>out</i> indicating where the
     *                  first encrypted or decrypted byte should be read.
     * @param  length   the number of encrypted/decrypted bytes to read.
     * @return the number of bytes read into <i>out</i>, or -1 if no
     *                  encrypted or decrypted bytes remained.
     * @exception IOException if an error occurs.
     *
     * @see java.security.Cipher#ENCRYPT
     * @see java.security.Cipher#DECRYPT
     * @see java.security.Cipher#getState
     */
    public synchronized int read(byte[] out, int offset, int length)
    throws IOException {
if (DEBUG && debuglevel >= 5) debug("read(<" + out + ">, " + offset + ", " + length + ") ...");

        // was the stream closed last time we read it?
        if (cipher == null) {
if (DEBUG && debuglevel >= 7) debug("... stream closed");
            return -1;
        }

        if (length <= 0) return 0; // consistent with other stream implementations.

        if (offset < 0) throw new ArrayIndexOutOfBoundsException("offset < 0");
//        if (length > out.length - offset)
//            length = out.length - offset;

        // first output any buffered data.
        int k = 0;
        if (buffered > 0) {
            k = buffered < length ? buffered : length;
            System.arraycopy(outBuf, outPtr, out, offset, k);
            outPtr += k;
            buffered -= k;
            offset += k;
            length -= k;
if (DEBUG && debuglevel >= 7) debug("  outBuf = <" + dump(outBuf) + ">, outPtr = " + outPtr + ", buffered = " + buffered + ", offset = " + offset + ", length = " + length);

            if (buffered == 0)
                outPtr = 0;

            if (length == 0) {
if (DEBUG && debuglevel >= 5) debug("... = " + k);
                return k;
            }
        }

        int inLen = cipher.inBufferSize(length);
        if (isDPBC) inLen++;
        byte[] in = (inLen <= preallocated1.length) ? preallocated1
                                                    : new byte[inLen];

if (DEBUG && debuglevel >= 7) debug("  inLen = " + inLen);

        // now try reading as many bytes as needed.
        int l = readFully(in, 0, inLen);

        // crypt the input into a temporary buffer.
        byte[] temp;
        int n;
        if (l < inLen) { // EOF encountered
            Cipher _cipher = cipher;
            cipher = null;  // must be done here, since crypt or outBufferSize may
                            // throw an exception

            int tempLen = _cipher.outBufferSizeFinal(l);
            temp = (tempLen <= preallocated2.length) ? preallocated2
                                                     : new byte[tempLen];
            n = _cipher.crypt(in, 0, l, temp, 0);
        } else {
            int tempLen = cipher.outBufferSize(l);
            temp = (tempLen <= preallocated2.length) ? preallocated2
                                                     : new byte[tempLen];
            n = cipher.update(in, 0, l, temp, 0);
        }

if (DEBUG && debuglevel >= 7) debug("  temp = <" + dump(temp) + ">, n = " + n);

        // if more data was returned than requested, buffer the excess, making
        // sure that the buffer is long enough.
        if (n > length) {
            buffered = n - length;
            if (buffered > outBuf.length)
                outBuf = new byte[buffered];

            System.arraycopy(temp, length, outBuf, 0, buffered);
            n = length;
if (DEBUG && debuglevel >= 7) debug("  buffered = " + buffered + ", length = " + length + ", n = " + n);
        }

        // n is now the length of output to be copied to the out array.
        System.arraycopy(temp, 0, out, offset, n);

        // take into account k bytes previously copied.
        n += k;

        if (n == 0 && cipher == null)
            n = -1;

if (DEBUG && debuglevel >= 5) debug("... = " + n);
        return n;
    }

    /**
     * Read bytes from the underlying stream, until <i>len</i> bytes or EOF.
     */
    private int readFully(byte[] in, int offset, int length) throws IOException {
        int n = 0, k = 0;
        do {
            n += k;
            k = super.read(in, n, length-n);
if (DEBUG && debuglevel >= 7) debug("  n = " + n + ", k = " + k);
        } while (k >= 0 && n < length);

        return n;
    }

    /**
     * Returns the next encrypted or decrypted byte, depending on the
     * cipher state.
     *
     * @return the next encrypted or decrypted byte, or -1 if the
     *           last encrypted/decrypted byte was already returned.
     * @exception IOException if an error occurs.
     *
     * @see java.security.Cipher#ENCRYPT
     * @see java.security.Cipher#DECRYPT
     * @see java.security.Cipher#getState
     */
    public synchronized int read()
    throws IOException {
        if (read(tempByte, 0, 1) < 1)
            return -1;
        else
            return tempByte[0] & 0xFF;
    }

    /**
     * Skips over and discards <i>n</i> bytes of data from the
     * input stream. The <code>skip</code> method may, for a variety of
     * reasons, end up skipping over some smaller number of bytes,
     * possibly 0. The actual number of bytes skipped is returned.
     *
     * @param  n    the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @exception IOException if an I/O error occurs.
     */
    public synchronized long skip(long n) throws IOException {
        // FIXME: this is horribly inefficient for large n.
        final int FUDGE_LEN = 100000;
        int length = (n < FUDGE_LEN) ? (int) n : FUDGE_LEN;
        byte[] out = new byte[length];
        long m = n;
        while (m > 0) {
            length = this.read(out, 0, length);
            if (length < 0) return n-m;
            m -= length;
            length = (m < FUDGE_LEN) ? (int) m : FUDGE_LEN;
        }
        return n;
    }

    /**
     * Returns the number of bytes that can be guaranteed to be read from this
     * input stream without blocking.
     *
     * @exception IOException if an I/O error occurs.
     */
    public synchronized int available() throws IOException {
        // Note: we don't know whether the count returned by super.available()
        // will include the final block. However, outBufferSize should always
        // return <= outBufferSizeFinal, and it is OK for available() to
        // return an underestimate of the number of available bytes.
        if (cipher == null) return 0;
        return buffered + cipher.outBufferSize(super.available());
    }

    /**
     * Closes the input stream.
     *
     * @exception IOException if an error occurs.
     */
    public synchronized void close()
    throws IOException {
        cipher = null;
        super.close();
    }

    /**
     * Does nothing, since this class does not support mark/reset.
     */
    public void mark(int readlimit) {}

    /**
     * Always throws an IOException, since this class does not support mark/reset.
     */
    public void reset() throws IOException {
        throw new IOException("CipherInputStream does not support mark/reset");
    }

    /**
     * Tests if this input stream supports the <code>mark</code> and
     * <code>reset</code> methods of InputStream, which it does not.
     *
     * @return <code>false</code>, since this class does not support the
     *         <code>mark</code> and <code>reset</code> methods.
     */
    public boolean markSupported() { return false; }
}
