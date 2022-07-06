package com.tumo.audioprotector;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class AudioEncoder {
    private DirectoryManger directoryManger;
    private Object copyService;
    private RSAEncryptor rsaEncryptor;

    public AudioEncoder(DirectoryManger directoryManger, Object copyS, RSAEncryptor rsa) {
        rsaEncryptor = rsa;
        copyService = copyS;
        this.directoryManger = directoryManger;
    }

    private byte[] wavToByteArray(File WAV_FILE) throws IOException {
        byte[] result;
        FileInputStream inputStream = new FileInputStream(WAV_FILE);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int read;
        byte[] data = new byte[1024];
        while ((read = inputStream.read(data, 0, data.length)) != -1) {
            byteArrayOutputStream.write(data, 0, read);
        }
        byteArrayOutputStream.flush();
        result = byteArrayOutputStream.toByteArray();
        return result;
    }


    private File byteArrayToWav(byte[] source, File outputFile) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(source);
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        int read;
        byte[] buff = new byte[1024];
        while ((read = inputStream.read(buff)) > 0) {
            outputStream.write(buff, 0, read);
        }
        inputStream.close();
        outputStream.flush();
        outputStream.close();
        return outputFile;
    }

    private static int applyEncryptFunction(int pos, byte n, int a, int b) {
        if (pos == 1) {
            return (n + a + b);
        } else if (pos == 2) {
            return (n + a - b);
        } else if (pos == 3) {
            return (n - a - b);
        } else if (pos == 4) {
            return (n + 2 * a + 2 * b);
        } else if (pos == 5) {
            return (n + 2 * a - 2 * b);
        } else if (pos == 6) {
            return (n - 2 * a - 2 * b);
        } else if (pos == 7) {
            return (2 * n + a + b);
        } else if (pos == 8) {
            return (2 * n + a - b);
        } else if (pos == 9) {
            return (2 * n - a - b);
        } else if (pos == 10) {
            return (2 * n + 2 * a + 2 * b);
        } else if (pos == 11) {
            return (2 * n + 2 * a - 2 * b);
        } else if (pos == 12) {
            return (2 * n - 2 * a - 2 * b);
        } else if (pos == 13) {
            return (3 * n + a + b);
        } else if (pos == 14) {
            return (3 * n + a - b);
        } else if (pos == 15) {
            return (3 * n - a - b);
        } else if (pos == 16) {
            return (3 * n + 2 * a + 2 * b);
        } else if (pos == 17) {
            return (3 * n + 2 * a - 2 * b);
        } else if (pos == 18) {
            return (3 * n - 2 * a - 2 * b);
        } else {
            return 0;
        }
    }

    private static int applyDecryptFunction(int pos, int c, int a, int b) {
        if (pos == 1) {
            return (c - a - b);
        } else if (pos == 2) {
            return (c - a + b);
        } else if (pos == 3) {
            return (c + a + b);
        } else if (pos == 4) {
            return (c - 2 * a - 2 * b);
        } else if (pos == 5) {
            return (c - 2 * a + 2 * b);
        } else if (pos == 6) {
            return (c + 2 * a + 2 * b);
        } else if (pos == 7) {
            return ((c - a - b) / 2);
        } else if (pos == 8) {
            return ((c - a + b) / 2);
        } else if (pos == 9) {
            return ((c + a + b) / 2);
        } else if (pos == 10) {
            return ((c - 2 * a - 2 * b) / 2);
        } else if (pos == 11) {
            return ((c - 2 * a + 2 * b) / 2);
        } else if (pos == 12) {
            return ((c + 2 * a + 2 * b) / 2);
        } else if (pos == 13) {
            return ((c - a - b) / 3);
        } else if (pos == 14) {
            return ((c - a + b) / 3);
        } else if (pos == 15) {
            return ((c + a + b) / 3);
        } else if (pos == 16) {
            return ((c - 2 * a - 2 * b) / 3);
        } else if (pos == 17) {
            return ((c - 2 * a + 2 * b) / 3);
        } else if (pos == 18) {
            return ((c + 2 * a + 2 * b) / 3);
        } else {
            return 0;
        }
    }

    public void encrypt(File file, BigInteger publicKey) throws IOException {
        int X, Y, Z, a, b, c, d, e, F;
        Random rand = new Random();
        Gson gson = new Gson();
        String outputFileName = file.getName().replace(".wav", "") + "_encrypted.txt";
        String outputString;
        final String key;
        final String encryptedKey;
        final BigInteger[] audioKeyForEncryption = new BigInteger[1];
        if (directoryManger.downloadEncryptedDirectory.listFiles() != null) {
            for (File f : directoryManger.downloadEncryptedDirectory.listFiles()) {
                if (f.getName().equals(outputFileName)) {
                    f.delete();
                }
            }
        }
        File outputFile = new File(directoryManger.downloadEncryptedDirectory, outputFileName);
        try {
            outputFile.createNewFile();
        } catch (IOException E) {
            E.printStackTrace();
        }
        FileOutputStream fos = new FileOutputStream(outputFile);
        byte[] inputArray = wavToByteArray(file);
        Log.i("MainActivity", "InE = " + Arrays.toString(inputArray));
        ArrayList<Integer> outputArray = new ArrayList<>(inputArray.length);
        X = (rand.nextInt(6) + 1);
        Y = (rand.nextInt(6) + 1);
        Z = (rand.nextInt(6) + 1);
        a = (rand.nextInt(9) + 1);
        b = (rand.nextInt(9) + 1);
        c = (rand.nextInt(9) + 1);
        d = (rand.nextInt(9) + 1);
        e = (rand.nextInt(9) + 1);
        F = (rand.nextInt(9) + 1);
        Log.i("MainActivity", "X = " + X + "Y = " + Y + "Z = " + Z + "a = " + a + "b = " + b + "c = " + c + "d = " + d + "e = " + e + "F = " + F);
        key = X + Integer.toString(Y) + Z + a + b + c + d + e + F;
        audioKeyForEncryption[0] = new BigInteger(key);
        encryptedKey = rsaEncryptor.encrypt(audioKeyForEncryption[0], publicKey).toString();
        for (int i = 0; i < 45; i++) {
            outputArray.add((int) inputArray[i]);
        }
        int j = 0;
        for (int i = 45; i < inputArray.length; i++) {
            if (j == 0) {
                outputArray.add(applyEncryptFunction(X, inputArray[i], a, b));
            } else if (j == 1) {
                outputArray.add(applyEncryptFunction(Y, inputArray[i], c, d));
            } else {
                outputArray.add(applyEncryptFunction(Z, inputArray[i], e, F));
            }
            j++;
            if (j == 3) {
                j = 0;
            }
        }
        outputString = gson.toJson(new ArrayForJson(outputArray));
        Log.i("MainActivity", "OuE = " + outputString);
        fos.write(outputString.getBytes());
        fos.flush();
        fos.close();
        AlertDialog alert = new AlertDialog.Builder(directoryManger.getContext())
                .setTitle("ATTENTION!")
                .setMessage("Copy this key. If your key is lost, you will not be able to decrypt this file anymore! Key is: " + encryptedKey)
                .setCancelable(false)
                .setPositiveButton("Copy to clipboard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ClipboardManager clipboard = (ClipboardManager) copyService;
                        ClipData clip = ClipData.newPlainText("public key", encryptedKey);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(directoryManger.getContext(), "Copied", Toast.LENGTH_SHORT).show();
                    }
                })
                .create();
        alert.show();
        file.delete();
    }

    public void decrypt(File file, String stringKey) throws  IOException{
        final String decryptedKey = rsaEncryptor.decrypt(new BigInteger(stringKey)).toString();
        char[] keyChars = decryptedKey.toCharArray();
        int X, Y, Z, a, b, c, d, e, F;
        Gson gson = new Gson();
        String outputFileName = file.getName().replace("_encrypted.txt", "") + "_decrypted.wav";
        String outputString;
        String inputString;
        StringBuilder text = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
            }
            br.close();
        }catch (IOException E){
            E.printStackTrace();
        }

        inputString = text.toString();
        ArrayForJson arrayForJson = gson.fromJson(inputString, ArrayForJson.class);
        int[] inputArray = new int[arrayForJson.arr.size()];
        for (int i = 0; i < arrayForJson.arr.size(); i++) {
            inputArray[i] = arrayForJson.arr.get(i);
        }
        Log.i("MainActivity", "InD = " + inputString);
        if (directoryManger.downloadDecryptedDirectory.listFiles() != null) {
            for (File f : directoryManger.downloadDecryptedDirectory.listFiles()) {
                if (f.getName().equals(outputFileName)) {
                    f.delete();
                }
            }
        }
        File outputFile = new File(directoryManger.downloadDecryptedDirectory.getAbsolutePath(), outputFileName);
        FileOutputStream fos = new FileOutputStream(outputFile);
        ArrayList<Byte> outputArray = new ArrayList<>();
        X = Character.getNumericValue(keyChars[0]);
        Y = Character.getNumericValue(keyChars[1]);
        Z = Character.getNumericValue(keyChars[2]);
        a = Character.getNumericValue(keyChars[3]);
        b = Character.getNumericValue(keyChars[4]);
        c = Character.getNumericValue(keyChars[5]);
        d = Character.getNumericValue(keyChars[6]);
        e = Character.getNumericValue(keyChars[7]);
        F = Character.getNumericValue(keyChars[8]);
        Log.i("MainActivity", "X = " + X + "Y = " + Y + "Z = " + Z + "a = " + a + "b = " + b + "c = " + c + "d = " + d + "e = " + e + "F = " + F);
        for (int i = 0; i < 45; i++) {
            outputArray.add((byte) inputArray[i]);
        }
        int j = 0;
        for (int i = 45; i < inputArray.length; i++) {
            if (j == 0) {
                outputArray.add((byte) applyDecryptFunction(X, inputArray[i], a, b));
            } else if (j == 1) {
                outputArray.add((byte) applyDecryptFunction(Y, inputArray[i], c, d));
            } else {
                outputArray.add((byte) applyDecryptFunction(Z, inputArray[i], e, F));
            }
            j++;
            if (j == 3) {
                j = 0;
            }
        }
        Log.i("MainActivity", "OuD = " + Arrays.toString(outputArray.toArray()));
        byte[] outputByteArray = new byte[outputArray.size()];
        for (int i = 0; i < outputArray.size(); i++) {
            outputByteArray[i] = outputArray.get(i);
        }
        fos.write(outputByteArray);
        fos.flush();
        fos.close();
        Toast.makeText(directoryManger.getContext(), "Decrypted", Toast.LENGTH_SHORT).show();
        file.delete();
    }
}


