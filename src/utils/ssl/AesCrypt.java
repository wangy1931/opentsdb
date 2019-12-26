package net.opentsdb.utils.ssl;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Author: Keqing Zhang
 * Date: 2019-11-25 11:48
 */
public class AesCrypt {
    private static AesCrypt instance = new AesCrypt();
    private static final int RANDOM_SIZE = 16;
    private static final int AES_KEY_SIZE = 32;

    private Base64 base64 = new Base64();
    private SecretKey aesKey;

    public void init(String keyString) throws AesException {
        byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != AES_KEY_SIZE) {
            throw new AesException("Illegal secret key");
        }
        this.aesKey = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * 加密
     * @param text
     * @return
     */
    public String encrypt(String text) throws AesException {
        String randomStr = getRandomStr();
        ByteGroup byteCollector = new ByteGroup();
        byte[] randomStrBytes = randomStr.getBytes(StandardCharsets.UTF_8);
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        byteCollector.addBytes(randomStrBytes);
        byteCollector.addBytes(textBytes);
        //使用自定义的填充方式对明文进行补位填充
        byte[] padBytes = PKCS7Encoder.encode(byteCollector.size());
        byteCollector.addBytes(padBytes);
        // 获得最终的字节流, 未加密
        byte[] unencrypted = byteCollector.toBytes();

        try {
            // 设置加密模式为AES的GCM模式
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            // 加密
            byte[] iv = cipher.getIV();
            assert iv.length == 12;
            byte[] encrypted = cipher.doFinal(unencrypted);
            assert encrypted.length == unencrypted.length + 16;
            byte[] message = new byte[12 + unencrypted.length + 16];
            System.arraycopy(iv, 0, message, 0, 12);
            System.arraycopy(encrypted, 0, message, 12, encrypted.length);

            // 使用BASE64对加密后的字符串进行编码
            return base64.encodeToString(message);
        } catch (Exception e) {
            throw new AesException("aes encrypt fail");
        }
    }

    /**
     * 解密
     * @param text
     * @return
     */
    public String decrypt(String text) throws AesException {
        try {
            // 设置解密模式为AES的CBC模式
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            // 使用BASE64对密文进行解码
            byte[] decrypted = Base64.decodeBase64(text);
            GCMParameterSpec params = new GCMParameterSpec(128, decrypted, 0, 12);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, params);
            // 解密
            byte[] original = cipher.doFinal(decrypted, 12, decrypted.length - 12);
            // 去除补位
            byte[] bytes = PKCS7Encoder.decode(original);
            String content = new String(Arrays.copyOfRange(bytes, 16, bytes.length), StandardCharsets.UTF_8);
//            String content = new String(Arrays.copyOfRange(original, 16, original.length), StandardCharsets.UTF_8);
            return content;
        } catch (Exception e) {
            throw new AesException("aes decrypt fail");
        }
    }

    public static AesCrypt getInstance() {
        return instance;
    }


    /**
     * 随机生成16位字符串
     */
    public String getRandomStr() {
        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < RANDOM_SIZE; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static void main(String[] args) throws AesException {
        AesCrypt.getInstance().init(args[0]);
        if ("encrypt".equals(args[1])) {
            String encryptMsg = AesCrypt.getInstance().encrypt(args[2]);
            System.out.println(encryptMsg);
        } else if ("decrypt".equals(args[1])) {
            String decryptMsg = AesCrypt.getInstance().decrypt(args[2]);
            System.out.println(decryptMsg);
        } else {
            System.out.println("crypt type failed");
        }

    }
}

