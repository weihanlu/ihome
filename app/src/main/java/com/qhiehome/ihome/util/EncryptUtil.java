package com.qhiehome.ihome.util;

import android.text.TextUtils;
import android.util.Base64;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * This is an encryption class to encrypt clear text
 */

public class EncryptUtil {

    private static final String TAG = "EncryptUtil";

    private static final String RSA_KEY = "RSA";

    private static final String ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding";//加密填充方式

    private static final String PUBLIC_KEY
            = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCE8iu5bbgVX8hy62V1QF5NSBaxELNdkGu1RfiL\n" +
            "K5nnNoZnr/OWahw/UeH5IB20zSTobOmgFKUV+4q2wvI32MVCphV3tM/EW59HZCGOuQSIWzvkk1B3\n" +
            "kahA1WPYIbyR6417Rxukwktf72WiQKGOp2WIbXch6fhdp3HRmiqQqAYHCQIDAQAB";

    public enum ALGO {
        SHA_256, SHA_512, MD5, RSA
    }

    /**
     *
     * @param text 需要加密的原始明文
     * @param algo 加密算法类型
     * @return 加密后的密文
     */
    public static String encrypt(String text, ALGO algo) {
        String encrypted = null;
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        switch (algo) {
            case SHA_256:
                encrypted = new String(Hex.encodeHex(DigestUtils.sha256(text)));
                break;
            case SHA_512:
                encrypted = new String(Hex.encodeHex(DigestUtils.sha512(text)));
                break;
            case MD5:
                encrypted = new String(Hex.encodeHex(DigestUtils.md5(text)));
                break;
            case RSA:
                encrypted = rsa(text);
            default:
                break;
        }
        return encrypted;
    }

    private static String rsa(String plainText) {
        try {
            byte[] cipherText = encrypt(EncryptUtil.loadPublicKeyByStr(PUBLIC_KEY), plainText.getBytes());
            if (cipherText != null) {
                return new String(Base64.encode(cipherText, Base64.NO_WRAP));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 公钥加密过程
     *
     * @param publicKey
     *            公钥
     * @param plainTextData
     *            明文数据
     * @return
     * @throws Exception
     *             加密过程中的异常信息
     */
    private static byte[] encrypt(RSAPublicKey publicKey, byte[] plainTextData)
            throws Exception {
        if (publicKey == null) {
            throw new Exception("加密公钥为空, 请设置");
        }
        Cipher cipher = null;
        try {
            // 使用Android的RSA，不使用默认的RSA
            cipher = Cipher.getInstance(ECB_PKCS1_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(plainTextData);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此加密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("加密公钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("明文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("明文数据已损坏");
        }
    }

    /**
     * 从字符串中加载公钥
     *
     * @param publicKeyStr
     *            公钥数据字符串
     * @throws Exception
     *             加载公钥时产生的异常
     */
    private static RSAPublicKey loadPublicKeyByStr(String publicKeyStr)
            throws Exception {
        try {
            // 对公钥解密
            byte[] keyBytes = Base64.decode(publicKeyStr, Base64.DEFAULT);
            // 取得公钥
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("公钥非法");
        } catch (NullPointerException e) {
            throw new Exception("公钥数据为空");
        }
    }

}
