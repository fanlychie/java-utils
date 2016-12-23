package org.fanlychie.util;

import javax.crypto.Cipher;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 加密算法工具类
 *
 * @author fanlychie
 */
public final class RSAUtils {

    /**
     * 生成密钥
     *
     * @return KeyPair
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            java.security.KeyPair pair = keyGen.generateKeyPair();
            KeyPair keyPair = new KeyPair();
            keyPair.publicKey = new String(base64Encode(pair.getPublic().getEncoded()), "UTF-8");
            keyPair.privateKey = new String(base64Encode(pair.getPrivate().getEncoded()), "UTF-8");
            return keyPair;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 私钥签名
     *
     * @param privateKey 私钥
     * @param ciphertext 密文内容
     * @return
     */
    public static String sign(PrivateKey privateKey, String ciphertext) {
        try {
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initSign(privateKey.source);
            signature.update(ciphertext.getBytes("UTF-8"));
            return new String(base64Encode(signature.sign()), "UTF-8");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 公钥验证签名
     *
     * @param publicKey  公钥
     * @param ciphertext 密文内容
     * @param signature  数字签名
     * @return
     */
    public static boolean verify(PublicKey publicKey, String ciphertext, String signature) {
        try {
            Signature sign = Signature.getInstance("SHA1withRSA");
            sign.initVerify(publicKey.source);
            sign.update(ciphertext.getBytes("UTF-8"));
            return sign.verify(base64Decode(signature.getBytes("UTF-8")));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 公钥加密内容
     *
     * @param plaintext 明文内容
     * @param publicKey 公钥
     * @return
     */
    public static String encrypt(String plaintext, PublicKey publicKey) {
        return encrypt(plaintext, publicKey.source);
    }

    /**
     * 私钥加密内容
     *
     * @param plaintext  明文内容
     * @param privateKey 私钥
     * @return
     */
    public static String encrypt(String plaintext, PrivateKey privateKey) {
        return encrypt(plaintext, privateKey.source);
    }

    /**
     * 公钥解密内容
     *
     * @param ciphertext 密文内容
     * @param publicKey  公钥
     * @return
     */
    public static String decrypt(String ciphertext, PublicKey publicKey) {
        return decrypt(ciphertext, publicKey.source);
    }

    /**
     * 私钥解密内容
     *
     * @param ciphertext 密文内容
     * @param privateKey 私钥
     * @return
     */
    public static String decrypt(String ciphertext, PrivateKey privateKey) {
        return decrypt(ciphertext, privateKey.source);
    }

    /**
     * 密钥对
     *
     * @author fanlychie
     */
    public static final class KeyPair {

        // 公钥字符串
        private String publicKey;

        // 私钥字符串
        private String privateKey;

        public String getPublicKey() {
            return publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        @Override
        public String toString() {
            return "KeyPair:\npublicKey = " + publicKey + "\nprivateKey = " + privateKey;
        }

    }

    /**
     * 公钥
     *
     * @author fanlychie
     */
    public static final class PublicKey {

        private RSAPublicKey source;

        /**
         * 通过公钥字符串构造公钥对象
         *
         * @param publicKey 公钥字符串
         * @return
         */
        public static PublicKey valueOf(String publicKey) {
            try {
                KeyFactory factory = KeyFactory.getInstance("RSA");
                PublicKey pubKey = new PublicKey();
                pubKey.source = (RSAPublicKey) factory.generatePublic(new X509EncodedKeySpec(base64Decode(format(publicKey))));
                return pubKey;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * 私钥
     *
     * @author fanlychie
     */
    public static final class PrivateKey {

        private RSAPrivateKey source;

        /**
         * 通过私钥字符串构造私钥对象
         *
         * @param privateKey 私钥
         * @return
         */
        public static PrivateKey valueOf(String privateKey) {
            try {
                KeyFactory factory = KeyFactory.getInstance("RSA");
                PrivateKey priKey = new PrivateKey();
                priKey.source = (RSAPrivateKey) factory.generatePrivate(new PKCS8EncodedKeySpec(base64Decode(format(privateKey))));
                return priKey;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
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

    /**
     * Base64 解码
     *
     * @param src 源字符串
     * @return
     */
    private static byte[] base64Decode(String src) {
        return base64Decode(src.getBytes(Charset.forName("ISO-8859-1")));
    }

    /**
     * 格式化密钥字符串
     *
     * @param key 密钥字符串
     * @return
     */
    private static String format(String key) {
        key = key.replace("-----BEGIN PUBLIC KEY-----\n", "");
        key = key.replace("-----END PUBLIC KEY-----", "");
        return key;
    }

    /**
     * RSA加密内容
     *
     * @param plaintext 明文内容
     * @param key       密钥
     * @return
     */
    private static String encrypt(String plaintext, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return new String(base64Encode(cipher.doFinal(plaintext.getBytes("UTF-8"))));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * RSA解密内容
     *
     * @param ciphertext 密文内容
     * @param key        密钥
     * @return
     */
    private static String decrypt(String ciphertext, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(base64Decode(ciphertext)), "UTF-8");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}