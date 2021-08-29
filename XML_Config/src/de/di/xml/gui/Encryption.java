package de.di.xml.gui;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.util.StringTokenizer;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 *
 * @author samir.lebaal
 */
public class Encryption {
    
    private static final String ENCR_ALGORITHM = "DES";
    private static final String TRANSFORMATION = "DES/ECB/PKCS5Padding";
    private static final String ENC_CHARSET = "UTF-8";
    private Key key;
    
    public Encryption() throws Exception {
        this.key = keyStringToKey(initString());
    }       
    
    public String encrypt(String source) throws Exception {
        String encString;
        try {
            Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            desCipher.init(1, this.key);
            byte[] cleartext = source.getBytes("UTF-8");
            byte[] ciphertext = desCipher.doFinal(cleartext);
            encString = bytesToString(ciphertext);
        } catch (Exception e) {                       
            throw new Exception("could not encrypt: " + e.getMessage());
        }       
        return encString;
    }
    
    public String decrypt(String source) throws Exception {
        String decrString;
        try {
            Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            byte[] ciphertext = stringToBytes(source);
            desCipher.init(2, this.key);
            byte[] cleartext = desCipher.doFinal(ciphertext);
            decrString = new String(cleartext, "UTF-8");
        } catch (Exception e) {                        
            throw new Exception("could not decrypt (" + e.getMessage() + ")");
        }        
        return decrString;
    }
    
    private static String bytesToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            sb.append(0xFF & b);
            if (i + 1 < bytes.length) {
                sb.append("-");
            }
        }
        return sb.toString();
    }
    
     private String initString() {
        int[] keyBytes = {14, 132, 11, 71, 30, 212, 107, 17};
        StringBuilder sb = new StringBuilder(26);
        sb.append(keyBytes[0] + 11);
        for (int i = 1; i < keyBytes.length; i++) {
            sb.append("-");
            sb.append(keyBytes[i] + 11);
        }
        return sb.toString();
    }
    
    private Key keyStringToKey(String keyString) throws Exception {
        SecretKey secretKey;
        try {
            byte[] bytes = stringToBytes(keyString);
            DESKeySpec keySpec = new DESKeySpec(bytes);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
            secretKey = skf.generateSecret(keySpec);
        } catch (Exception e) {                        
            throw new Exception("error while creating key: " + e.getMessage());
        }        
        return secretKey;
    }
    
    private byte[] stringToBytes(String str) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StringTokenizer st = new StringTokenizer(str, "-", false);
        while (st.hasMoreTokens()) {
            int i = Integer.parseInt(st.nextToken());
            bos.write((byte) i);
        }
        return bos.toByteArray();
    }
    
}
