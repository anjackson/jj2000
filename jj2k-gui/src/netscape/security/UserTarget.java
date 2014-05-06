// $Log: UserTarget.java,v $
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
public class UserTarget extends Target {
    public UserTarget() { /* do nothing */ }
    public UserTarget(String s1, Principal p, int i, String s2, String s3, String s4) { /* do nothing */ }
    public UserTarget(String s1, Principal p, int i, String s2, String s3, String s4, Target[] ta) { /* do nothing */ }
    public UserTarget(String s1, Principal p, int i, String s2, String s3, String s4, String s5) { /* do nothing */ }
    public UserTarget(String s1, Principal p, int i, String s2, String s3, String s4, String s5, Target[] ta) { /* do nothing */ }
    public Privilege enablePrivilege(Principal p, Object o) { throw new NoClassDefFoundError("netscape.security.UserTarget"); }
}
