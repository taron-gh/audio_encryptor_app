package com.tumo.audioprotector;

import android.content.SharedPreferences;
import android.util.Log;

import java.math.BigInteger;
import java.util.Random;

public class RSAEncryptor {
    private Random rand = new Random();
    private BigInteger p;
    private BigInteger q;
    public BigInteger n;
    private BigInteger fi;
    private BigInteger e;
    private BigInteger d;
    //private BigInteger valueToEncrypt = new BigInteger("123456");
    private String TAG = "MainActivity";
    public RSAEncryptor(){
        e = new BigInteger("17");
    }
    public BigInteger encrypt(BigInteger m, BigInteger N){
        BigInteger c = m.modPow(e, N);
        Log.i("Encryptor", " encrypted value is: " + c.toString());
        return  c;
    }
    private void init(BigInteger p, BigInteger q){
        this.p = p;
        this.q = q;
        n = p.multiply(q);
        fi  = (p.subtract(new BigInteger("1"))).multiply(q.subtract(new BigInteger("1")));
        d = e.modInverse(fi);
    }
    public BigInteger decrypt(BigInteger c){
        BigInteger m = c.modPow(d, n);
        Log.i("Encryptor", " decrypted value is: " + m.toString());
        return  m;
    }
    public void generatePQ(){
        q = new BigInteger(1024, 100,  rand);
        p = new BigInteger(1024, 100,  rand);
        init(p, q);
//        Log.i(TAG, p.toString());
//        Log.i(TAG, q.toString());
    }
    public void setPQ(BigInteger p, BigInteger q){
        init(p, q);
    }
    public void saveForExit(SharedPreferences sp){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("p", p.toString());
        editor.putString("q", q.toString());
        editor.commit();
    }
}
