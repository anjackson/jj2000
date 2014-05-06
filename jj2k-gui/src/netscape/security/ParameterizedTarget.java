// $Log: ParameterizedTarget.java,v $
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
public class ParameterizedTarget extends UserTarget {
    public ParameterizedTarget() { /* do nothing */ }
    public ParameterizedTarget(String s1, Principal p, int i, String s2, String s3, String s4) { /* do nothing */ }
    public ParameterizedTarget(String s1, Principal p, int i, String s2, String s3, String s4, String s5) { /* do nothing */ }
    public String getDetailedInfo(Object o) { throw new NoClassDefFoundError("netscape.security.ParameterizedTarget"); }
    public Privilege enablePrivilege(Principal p, Object o) { throw new NoClassDefFoundError("netscape.security.ParameterizedTarget"); }
    public Privilege checkPrivilegeEnabled(Principal[] pa, Object o) { throw new NoClassDefFoundError("netscape.security.ParameterizedTarget"); }
}
