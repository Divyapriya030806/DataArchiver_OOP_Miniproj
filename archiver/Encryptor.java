package com.example.archiver;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

public class Encryptor {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public enum Algorithm {
        AES_GCM, AES_CBC
    }

    public static byte[] randomBytes(int len) {
        byte[] b = new byte[len];
        SECURE_RANDOM.nextBytes(b);
        return b;
    }

    public static byte[] deriveKey(char[] password, byte[] salt, int keyLen) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, 100_000, keyLen * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return skf.generateSecret(spec).getEncoded();
    }

    public static byte[] encrypt(Algorithm algorithm, byte[] plaintextZip, char[] password, byte[] salt) throws Exception {
        byte[] key = deriveKey(password, salt, 32);
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        if (algorithm == Algorithm.AES_GCM) {
            byte[] iv = randomBytes(12);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            byte[] ct = cipher.doFinal(plaintextZip);
            byte[] out = new byte[1 + salt.length + iv.length + ct.length];
            out[0] = 1; // version 1, gcm
            System.arraycopy(salt, 0, out, 1, salt.length);
            System.arraycopy(iv, 0, out, 1 + salt.length, iv.length);
            System.arraycopy(ct, 0, out, 1 + salt.length + iv.length, ct.length);
            Arrays.fill(key, (byte)0);
            return out;
        } else {
            byte[] iv = randomBytes(16);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] ct = cipher.doFinal(plaintextZip);
            byte[] out = new byte[1 + salt.length + iv.length + ct.length];
            out[0] = 2; // version 2, cbc
            System.arraycopy(salt, 0, out, 1, salt.length);
            System.arraycopy(iv, 0, out, 1 + salt.length, iv.length);
            System.arraycopy(ct, 0, out, 1 + salt.length + iv.length, ct.length);
            Arrays.fill(key, (byte)0);
            return out;
        }
    }

    public static byte[] decrypt(byte[] payload, char[] password) throws Exception {
        if (payload == null || payload.length < 1 + 16 + 12) {
            throw new IllegalArgumentException("Invalid payload");
        }
        int version = payload[0];
        int offset = 1;
        byte[] salt = Arrays.copyOfRange(payload, offset, offset + 16);
        offset += 16;

        byte[] key = deriveKey(password, salt, 32);
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

        if (version == 1) { // GCM
            byte[] iv = Arrays.copyOfRange(payload, offset, offset + 12);
            offset += 12;
            byte[] ct = Arrays.copyOfRange(payload, offset, payload.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            byte[] pt = cipher.doFinal(ct);
            Arrays.fill(key, (byte)0);
            return pt;
        } else if (version == 2) { // CBC
            byte[] iv = Arrays.copyOfRange(payload, offset, offset + 16);
            offset += 16;
            byte[] ct = Arrays.copyOfRange(payload, offset, payload.length);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] pt = cipher.doFinal(ct);
            Arrays.fill(key, (byte)0);
            return pt;
        } else {
            Arrays.fill(key, (byte)0);
            throw new IllegalArgumentException("Unsupported version: " + version);
        }
    }
}


