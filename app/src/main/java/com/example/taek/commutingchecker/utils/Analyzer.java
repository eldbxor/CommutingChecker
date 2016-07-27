package com.example.taek.commutingchecker.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by Awesometic on 2016-07-27.
 */
public class Analyzer {

    /** 2016. 7. 21
     * Awesometic
     * If the serversPublicKey continuously has value "null", then any socket data will not be send to server with RSA encryption
     */
    // A RSA cipher manager that has RSA key pair itself and to do such as encrypt, decrypt using RSA
    private RSACipher rsaCipher;

    // RSA public key from server, it will be updated once a day at midnight
    public PublicKey serversPublicKey;

    public Analyzer() {
        try {
            // Initialize the RSA manager
            rsaCipher = new RSACipher();
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException |
                InvalidKeyException |
                IllegalBlockSizeException |
                BadPaddingException e) {

            e.printStackTrace();
        }
    }

    private String bytesToHex(byte[] bytes) {
        char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v/16];
            hexChars[j*2 + 1] = hexArray[v%16];
        }

        return new String(hexChars);
    }

    private byte[] hexToBytes(String hex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(hex.length() / 2);

        for (int i = 0; i < hex.length(); i += 2) {
            String output = hex.substring(i, i + 2);
            int decimal = Integer.parseInt(output, 16);
            baos.write(decimal);
        }

        return baos.toByteArray();
    }

    public JSONObject encryptSendJson(JSONObject content) {
        try {
            JSONObject sendJson = new JSONObject();
            JSONObject aesKey = new JSONObject();

            byte[] aesCryptKey = AES256Cipher.getRandomAesCryptKey();
            byte[] aesCryptIv = AES256Cipher.getRandomAesCryptIv();

            aesKey.put("aesCryptKey", bytesToHex(aesCryptKey));
            aesKey.put("aesCryptIv", bytesToHex(aesCryptIv));

            sendJson.put("rsaPublicKey", rsaCipher.getPublicKey("pkcs8-pem"));
            sendJson.put("aesKeyIv", rsaCipher.encrypt(aesKey.toString(), serversPublicKey));
            sendJson.put("content", AES256Cipher.encrypt(aesCryptKey, aesCryptIv, content.toString()));

            return sendJson;

        } catch (JSONException |
                NoSuchAlgorithmException |
                NoSuchPaddingException |
                InvalidKeyException |
                IllegalBlockSizeException |
                BadPaddingException |
                UnsupportedEncodingException |
                InvalidAlgorithmParameterException e) {

            e.printStackTrace();
            Log.d("Awesometic", "Analyzer - exception caught (JSON envelopment)");

            return null;
        }
    }

    public String extractContentFromReceivedJson(JSONObject receivedJson) {
        try {
            JSONObject keyIvJson = new JSONObject(rsaCipher.decrypt(receivedJson.getString("aesKeyIv")));

            byte[] serversAesCryptKey = hexToBytes(keyIvJson.getString("aesCryptKey"));
            byte[] serversAesCryptIv = hexToBytes(keyIvJson.getString("aesCryptIv"));

            String contentJsonString = AES256Cipher.decrypt(serversAesCryptKey, serversAesCryptIv, receivedJson.getString("content"));
            Log.d("Awesometic", contentJsonString);

            return contentJsonString;

        } catch (JSONException |
                NoSuchAlgorithmException |
                NoSuchPaddingException |
                InvalidKeyException |
                IllegalBlockSizeException |
                BadPaddingException |
                UnsupportedEncodingException |
                InvalidAlgorithmParameterException e) {

            e.printStackTrace();
            Log.d("Awesometic", "Analyzer - exception caught (result analyze)");

            return null;
        }
    }
}
