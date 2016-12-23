package org.fanlychie.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES 对称加密算法工具类
 *
 * @author fanlychie
 */
public final class AESUtils {

    /**
     * AES 密钥长度 128(16)/192(24)/256(32)
     */
    public static int length = 128;

    /**
     * 加密内容
     *
     * @param plaintext 明文内容
     * @param key       加密所使用的密钥串
     * @return
     */
    public static String encrypt(String plaintext, String key) {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(length, new SecureRandom(key.getBytes()));
            SecretKeySpec spec = new SecretKeySpec(generator.generateKey().getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, spec);
            return new String(base64Encode(cipher.doFinal(plaintext.getBytes("UTF-8"))));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解密内容
     *
     * @param ciphertext 密文内容
     * @param key        加密所使用的密钥串
     * @return
     */
    public static String decrypt(String ciphertext, String key) {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(length, new SecureRandom(key.getBytes()));
            SecretKeySpec spec = new SecretKeySpec(generator.generateKey().getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, spec);
            return new String(cipher.doFinal(base64Decode(ciphertext.getBytes("UTF-8"))));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Java 1.8 开始提供 java.util.Base64, 低于 Java 1.8 的可使用 Apache 的 Base64 算法替换：
     * <p>
     * org.apache.commons.codec.binary.Base64.encodeBase64(byte[] src)
     *
     * @param src 源字节数组
     * @return
     */
    private static byte[] base64Encode(byte[] src) {
        return Base64.getEncoder().encode(src);
    }

    /**
     * Java 1.8 开始提供 java.util.Base64, 低于 Java 1.8 的可使用 Apache 的 Base64 算法替换：
     * <p>
     * org.apache.commons.codec.binary.Base64.decodeBase64(byte[] src)
     *
     * @param src 源字节数组
     * @return
     */
    private static byte[] base64Decode(byte[] src) {
        return Base64.getDecoder().decode(src);
    }

}