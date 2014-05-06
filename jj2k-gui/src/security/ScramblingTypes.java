package security;

public interface ScramblingTypes {

    /** No scrambling */
    public static final int NO_SCRAMBLING = 0;

    /** Scrambling in the wavelet domain (i.e. code-block's sign bit-plane) */
    public static final int WAV_DOMAIN_SCRAMBLING = 1;

    /** Scrambling in the compressed domain (i.e. code-block's codewords) */
    public static final int COMP_DOMAIN_SCRAMBLING = 2;
}
