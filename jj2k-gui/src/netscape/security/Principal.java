// $Log: Principal.java,v $
// Revision 1.1.1.1  2002/08/27 11:48:59  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.1.1.1  1997/11/03 22:36:57  hopwood
// + Imported to CVS (tagged as 'start').
//
// $Endlog$
package netscape.security;

/**
 * Dummy class.
 */
public final class Principal /* implements netscape.util.Codable */ {
    public static final int CODEBASE_EXACT = 10;
    public static final int CODEBASE_REGEXP = 11;
    public static final int CERT = 12;
    public static final int CERT_FINGERPRINT = 13;
    public static final int CERT_KEY = 14;

    public Principal() { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public Principal(java.net.URL u) { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public Principal(int i, String s) { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public Principal(int i, byte[] ba) { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public Principal(int i, byte[] ba, Class cl) { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public boolean equals(Object o) { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public int hashCode() { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public boolean isCodebase() { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public boolean isCodebaseExact() { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public boolean isCodebaseRegexp() { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public boolean isCert() { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public boolean isCertFingerprint() { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public String toString() { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public String toVerboseString() { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public String getVendor() { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public String toVerboseHtml() { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public String getNickname() { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public static int getZigPtr(Class cl) { throw new NoClassDefFoundError("netscape.security.Principal"); }
/* don't need these
    public void describeClassInfo(netscape.util.ClassInfo) { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public void encode(netscape.util.Encoder) { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public void decode(netscape.util.Decoder) { throw new NoClassDefFoundError("netscape.security.Principal"); }
    public void finishDecoding() { throw new NoClassDefFoundError("netscape.security.Principal"); }
*/
}
