package de.di.xml.importers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author A. Sopicki
 */
public class MD5Digest {
    public MD5Digest() {
        
    }
    
    public String digest(File input) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        FileInputStream fileInput = new FileInputStream(input);
        DigestInputStream digestInput = new DigestInputStream(fileInput, md);
        byte[] buffer = new byte[1024];
        
        try {
            while(digestInput.read(buffer) >= 0) {
            }
        } catch (IOException ioex) {
            try { digestInput.close(); } catch(Exception ex) {}
            throw ioex;
        }
        
        buffer = md.digest();
        md.reset();
        digestInput.close();
        
        String hexDigest = new String(Hex.encodeHex(buffer));
        
        return hexDigest.toUpperCase();
    }

    
}
