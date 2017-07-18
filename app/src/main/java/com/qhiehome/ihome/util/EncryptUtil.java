package com.qhiehome.ihome.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.security.NoSuchAlgorithmException;

/**
 * This is an encryption class to encrypt clear text
 */

public class EncryptUtil {

    private static final String TAG = "EncryptUtil";

    public enum ALGO {
        SHA_256, SHA_512
    }

    /**
     *
     * @param text 需要加密的原始明文
     * @param algo 加密算法类型
     * @return 加密后的密文
     * @throws NoSuchAlgorithmException 当加密算法不可用时抛出此异常
     */
    public static String encrypt(String text, ALGO algo) throws NoSuchAlgorithmException {
        String encrypted = null;
        if (text == null || text.isEmpty()) {
            return "";
        } else {
            switch (algo) {
                case SHA_256:
                    encrypted = DigestUtils.sha256Hex(text);
                    break;
                case SHA_512:
                    encrypted = DigestUtils.sha512Hex(text);
                    break;
                default:
                    break;
            }
        }
        return encrypted;
    }

}
