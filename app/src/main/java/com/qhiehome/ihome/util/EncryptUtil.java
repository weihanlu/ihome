package com.qhiehome.ihome.util;

import android.text.TextUtils;

import org.apache.commons.codec.binary.Hex;
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
            default:
                break;
        }
        return encrypted;
    }

}
