package com.neowine.fmanager;

public class JniBrige {
    public native static int OPENSSLENC(String src_file, String dest_file, String key, String device);
    public native static int OPENSSLDEC(String src_file, String dest_file, String key, String device);
    public native static void AES_CIPHER(byte[] input,byte[] output,byte[] key);
    private JniBrige() {};
    static public JniBrige brige = null;
    public static JniBrige getInstance()
    {
        if(brige == null) {
            brige = new JniBrige();
        }
        return brige;

    }
    static {
        System.loadLibrary("NeowineNative");
    }

}
