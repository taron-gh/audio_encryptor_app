package com.example.audioencryptor;

import android.util.Log;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

public class Encryptor {
    private static Random rand = new Random();
    private static BigInteger p = new BigInteger(1024, 100,  rand);
    private static BigInteger q = new BigInteger(1024, 100,  rand);
    private static BigInteger n = p.multiply(q);
    private static BigInteger fi = (p.subtract(new BigInteger("1"))).multiply(q.subtract(new BigInteger("1")));
    private static BigInteger e = new BigInteger("17");
    private static BigInteger d = e.modInverse(fi);
    private static BigInteger valueToEncrypt = new BigInteger("123456");
    public Encryptor(){
        Log.i("Encryptor", " p value is: " + p.toString());
        Log.i("Encryptor", " q value is: " + q.toString());
        Log.i("Encryptor", " n value is: " + n.toString());
        Log.i("Encryptor", " f value is: " + fi.toString());
        Log.i("Encryptor", " e value is: " + e);
        Log.i("Encryptor", " d value is: " + d.toString());
        Log.i("Encryptor", " Value for encryption is: " + valueToEncrypt.toString());
        decrypt(encrypt(valueToEncrypt));
    }

    public BigInteger encrypt(BigInteger m){
        BigInteger c = m.modPow(e, n);
        Log.i("Encryptor", " encrypted value is: " + c.toString());
        return  c;
    }

    public BigInteger decrypt(BigInteger c){
        BigInteger m = c.modPow(d, n);
        Log.i("Encryptor", " decrypted value is: " + m.toString());
        return  m;
    }

}
