/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://dspace.org/license/
 */

package org.dspace.curate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utils contains a few commonly occurring methods.
 *
 * @author richardrodgers
 */
public class Utils {

    private static final int BUF_SIZE = 4096;
    
    public static String checksum(File file, String algorithm) throws IOException {
        FileInputStream in = new FileInputStream(file);
        String csum = checksum(in, algorithm);
        in.close();
        return csum;
    }

    public static String checksum(InputStream in, String algorithm) throws IOException {
        try {
            DigestInputStream din = new DigestInputStream(in,
                                        MessageDigest.getInstance(algorithm));
            byte[] buf = new byte[BUF_SIZE];
            while (din.read(buf) != -1) {
                // no-op
            }
            return toHex(din.getMessageDigest().digest());
        } catch (NoSuchAlgorithmException nsaE) {
            throw new IOException(nsaE.getMessage());
        }
    }

    static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
    public static String toHex(byte[] data) {
         if ((data == null) || (data.length == 0)) {
            return null;
        }
        char[] chars = new char[2 * data.length];
        for (int i = 0; i < data.length; ++i) {
            chars[2 * i] = HEX_CHARS[(data[i] & 0xF0) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[data[i] & 0x0F];
        }
        return new String(chars);
    }
    
    public static void copy(File inFile, File outFile) throws IOException {
        FileInputStream in = new FileInputStream(inFile);
        FileOutputStream out = new FileOutputStream(outFile);
        copy(in, out);
        in.close();
        out.close();
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        final byte[] buffer = new byte[BUF_SIZE];
        BufferedInputStream bin = new BufferedInputStream(in);
        BufferedOutputStream bout = new BufferedOutputStream(out);
        while (true) {
            int count = bin.read(buffer, 0, BUF_SIZE);
            if (-1 == count) {
                break;
            }
            // write out those same bytes
            bout.write(buffer, 0, count);
        }
        // needed to flush cache
        bout.flush();
    }
}
