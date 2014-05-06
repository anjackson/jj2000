// $Log: Privilege.java,v $
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
public final class Privilege /* implements netscape.util.Codable */ {
    public static final int N_PERMISSIONS = 3;
    public static final int FORBIDDEN = 0;
    public static final int ALLOWED = 1;
    public static final int BLANK = 2;

    public static final int N_DURATIONS = 3;
    public static final int SCOPE = 0;
    public static final int SESSION = 1;
    public static final int FOREVER = 2;

    public Privilege() { throw new NoClassDefFoundError("netscape.security.Privilege"); }
    public static Privilege findPrivilege(int i, int j) { throw new NoClassDefFoundError("netscape.security.Privilege"); }
    public static int add(int i, int j) { throw new NoClassDefFoundError("netscape.security.Privilege"); }
    public static Privilege add(Privilege p1, Privilege p2) { throw new NoClassDefFoundError("netscape.security.Privilege"); }
    public boolean samePermission(Privilege p) { throw new NoClassDefFoundError("netscape.security.Privilege"); }
    public boolean samePermission(int i) { throw new NoClassDefFoundError("netscape.security.Privilege"); }
    public boolean sameDuration(Privilege p) { throw new NoClassDefFoundError("netscape.security.Privilege"); }
    public boolean sameDuration(int i) { throw new NoClassDefFoundError("netscape.security.Privilege"); }
    public boolean isAllowed() { throw new NoClassDefFoundError("netscape.security.Privilege"); }
    public boolean isForbidden() { throw new NoClassDefFoundError("netscape.security.Privilege"); }
    public boolean isBlank() { throw new NoClassDefFoundError("netscape.security.Privilege"); }
    public int getPermission() { throw new NoClassDefFoundError("netscape.security.Privilege"); }
    public int getDuration() { throw new NoClassDefFoundError("netscape.security.Privilege"); }
    public String toString() { throw new NoClassDefFoundError("netscape.security.Privilege"); }
/* don't need these
    public void describeClassInfo(netscape.util.ClassInfo) { throw new NoClassDefFoundError("netscape.security.Privilege"); }
    public void encode(netscape.util.Encoder) { throw new NoClassDefFoundError("netscape.security.Privilege"); }
    public void decode(netscape.util.Decoder) { throw new NoClassDefFoundError("netscape.security.Privilege"); }
    public void finishDecoding() { throw new NoClassDefFoundError("netscape.security.Privilege"); }
*/
}
