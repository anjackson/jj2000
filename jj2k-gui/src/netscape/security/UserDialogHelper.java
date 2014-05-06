// $Log: UserDialogHelper.java,v $
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
public class UserDialogHelper {
    private UserDialogHelper() {} // static methods only

    private static final int LOW_RISK = 0;
    private static final int MEDIUM_RISK = 1;
    private static final int HIGH_RISK = 2;

    public static String targetRiskStr(int i) {
        switch (i) {
          case LOW_RISK: return "low";
          case MEDIUM_RISK: return "medium";
          case HIGH_RISK: return "high";
          default: throw new IllegalArgumentException("unrecognized risk code: " + i);
        }
    }
    public static int targetRiskLow() { return LOW_RISK; }
    public static String targetRiskColorLow() { return "?"; }
    public static int targetRiskMedium() { return MEDIUM_RISK; }
    public static String targetRiskColorMedium() { return "?"; }
    public static int targetRiskHigh() { return HIGH_RISK; }
    public static String targetRiskColorHigh() { return "?"; }
}
