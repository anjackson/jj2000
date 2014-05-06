// $Log: PrivilegeTable.java,v $
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
public class PrivilegeTable implements Cloneable /*, netscape.util.Codable */ {
    public PrivilegeTable() { throw new NoClassDefFoundError("netscape.security.PrivilegeTable"); }
    public int size() { throw new NoClassDefFoundError("netscape.security.PrivilegeTable"); }
    public boolean isEmpty() { throw new NoClassDefFoundError("netscape.security.PrivilegeTable"); }
/*
    public netscape.util.Enumeration keys() { throw new NoClassDefFoundError("netscape.security.PrivilegeTable"); }
    public netscape.util.Enumeration elements() { throw new NoClassDefFoundError("netscape.security.PrivilegeTable"); }
*/
    public String toString() { throw new NoClassDefFoundError("netscape.security.PrivilegeTable"); }
    public Privilege get(Object o) { throw new NoClassDefFoundError("netscape.security.PrivilegeTable"); }
    public Privilege get(Target t) { throw new NoClassDefFoundError("netscape.security.PrivilegeTable"); }
    public Privilege put(Object o, Privilege p) { throw new NoClassDefFoundError("netscape.security.PrivilegeTable"); }
    public Privilege put(Target t, Privilege p) { throw new NoClassDefFoundError("netscape.security.PrivilegeTable"); }
    public Privilege remove(Object o) { throw new NoClassDefFoundError("netscape.security.PrivilegeTable"); }
    public Privilege remove(Target t) { throw new NoClassDefFoundError("netscape.security.PrivilegeTable"); }
    public void clear() { throw new NoClassDefFoundError("netscape.security.PrivilegeTable"); }
    public Object clone() { throw new NoClassDefFoundError("netscape.security.PrivilegeTable"); }
/* don't need these
    public void describeClassInfo(netscape.util.ClassInfo) { throw new NoClassDefFoundError("netscape.security.PrivilegeTable"); }
    public void encode(netscape.util.Encoder) { throw new NoClassDefFoundError("netscape.security.PrivilegeTable"); }
    public void decode(netscape.util.Decoder) { throw new NoClassDefFoundError("netscape.security.PrivilegeTable"); }
    public void finishDecoding() { throw new NoClassDefFoundError("netscape.security.PrivilegeTable"); }
*/
}
