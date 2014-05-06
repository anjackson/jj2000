// $Log: Target.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:00  grosbois
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
public class Target /* implements netscape.util.Codable */ {
    public Target() { /* do nothing */ }
    public Target(String s, Principal p) { /* do nothing */ }
    public Target(String s) { /* do nothing */ }
    public Target(String s, Principal p, Target[] ta) { /* do nothing */ }
    public Target(String s1, Principal p, String s2, String s3, String s4, String s5) { /* do nothing */ }
    public Target(String s1, Principal p, String s2, String s3, String s4, String s5, Target[] ta) { /* do nothing */ }
    public Target registerTarget() { return this; }
    public static Target findTarget(String s) { throw new NoClassDefFoundError("netscape.security.Target"); }
    public static Target findTarget(String s, Principal p) { throw new NoClassDefFoundError("netscape.security.Target"); }
    public static Target findTarget(Target t) { throw new NoClassDefFoundError("netscape.security.Target"); }
    public Privilege checkPrivilegeEnabled(Principal[] pa, Object o) { throw new NoClassDefFoundError("netscape.security.Target"); }
    public Privilege checkPrivilegeEnabled(Principal[] pa) { throw new NoClassDefFoundError("netscape.security.Target"); }
    public Privilege checkPrivilegeEnabled(Principal p, Object o) { throw new NoClassDefFoundError("netscape.security.Target"); }
    public Privilege enablePrivilege(Principal p, Object o) { throw new NoClassDefFoundError("netscape.security.Target"); }
    public String getRisk() { throw new NoClassDefFoundError("netscape.security.Target"); }
    public String getRiskColor() { throw new NoClassDefFoundError("netscape.security.Target"); }
    public String getDescription() { throw new NoClassDefFoundError("netscape.security.Target"); }
    public static Target getTargetFromDescription(String s) { throw new NoClassDefFoundError("netscape.security.Target"); }
    public String getHelpUrl() { throw new NoClassDefFoundError("netscape.security.Target"); }
    public String getDetailedInfo(Object o) { throw new NoClassDefFoundError("netscape.security.Target"); }
/* don't need these
    public void describeClassInfo(netscape.util.ClassInfo) { throw new NoClassDefFoundError("netscape.security.Target"); }
    public void encode(netscape.util.Encoder) { throw new NoClassDefFoundError("netscape.security.Target"); }
    public void decode(netscape.util.Decoder) { throw new NoClassDefFoundError("netscape.security.Target"); }
    public void finishDecoding() { throw new NoClassDefFoundError("netscape.security.Target"); }
*/
}
