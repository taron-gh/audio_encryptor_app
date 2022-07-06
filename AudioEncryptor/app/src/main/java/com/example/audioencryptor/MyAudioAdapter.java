package com.example.audioencryptor;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Random;

public class MyAudioAdapter {
    private File EXTERNAL_DIR_FILE;
    public static String EXTERNAL_DIR;
    public static String INTERNAL_DIR;
    private MediaRecorder recorder;
    private boolean recordingStarted = false;
    private File temporaryFile;
    private Context context;
    private EncryptionCallback encryptionCallback;
    private File encryptionDirectory;
    private WavRecorder wavRecorder;
    public MyAudioAdapter(String internalPath, String externalPath, EncryptionCallback encryptionCallback, Context context, File encriptionDirectory) {
        this.encryptionDirectory = encriptionDirectory;
        this.context = context;
        this.encryptionCallback = encryptionCallback;
        wavRecorder = new WavRecorder(context);
        INTERNAL_DIR = internalPath + "/Recording directory";
        EXTERNAL_DIR = externalPath + "/Recording directory";
        EXTERNAL_DIR_FILE = new File(externalPath + "/Recording directory");
        EXTERNAL_DIR_FILE.mkdirs();
    }

    //************Conversion methods**************
    public byte[] wavToByteArray(File WAV_FILE) throws IOException {
        byte[] result;
        FileInputStream inputStream = new FileInputStream(WAV_FILE);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int read;
        byte[] data = new byte[1024];
        //byteArrayOutputStream.write(wavRecorder.wavFileHeader());
        while ((read = inputStream.read(data, 0, data.length)) != -1) {
                byteArrayOutputStream.write(data, 0, read);


        }

        byteArrayOutputStream.flush();
        result = byteArrayOutputStream.toByteArray();
        //wavRecorder.updateHeaderInformation(result);
        return result;
    }


    public File byteArrayToWav(byte[] source, File outputFile) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(source);
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        int read;
        byte[] buff = new byte[1024];
        while ((read = inputStream.read(buff)) > 0) {
            outputStream.write(buff, 0, read);
            Log.i("AudioAdapter", "file writing");
        }
        inputStream.close();
        outputStream.flush();
        outputStream.close();
        return outputFile;
    }

    //*************Initialization method*************
    public void init() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    }

    //*************Recording methods******************
    public void startRecording(Context context) throws IOException, InterruptedException {
        recorder = new MediaRecorder();
        //externalDirecory.mkdirs();
        //internalDirectory.mkdirs();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        temporaryFile = new File(context.getFilesDir().getAbsolutePath() + "/temporaryFile.wav");
        //wavRecorder.startRecording();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            recorder.setOutputFile(temporaryFile);
        } else {
            recorder.setOutputFile(temporaryFile.getAbsolutePath());
        }

        recorder.prepare();
        recorder.start();
        recordingStarted = true;

    }

    public byte[] stopRecording() throws  IOException {
        if (recordingStarted) {
            recorder.stop();
            recorder.reset();
            recorder.release();
            byte[] result;
            FileInputStream inputStream = new FileInputStream(temporaryFile);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int read;
            byte[] data = new byte[1024];

            while ((read = inputStream.read(data, 0, data.length)) != -1) {
                byteArrayOutputStream.write(data, 0, read);
            }

            byteArrayOutputStream.flush();
            result = byteArrayOutputStream.toByteArray();
            recordingStarted = false;
            temporaryFile.delete();
            return result;
        } else {
            return null;
        }
            //return wavRecorder.stopRecording();

    }

    //*************Encryption methods******************
    private static int applyEncryptFunction(byte pos, int n, byte a, byte b) {
        if (pos == 1) {
            return  (n + a + b);
        } else if (pos == 2) {
            return  (n + a - b);
        } else if (pos == 3) {
            return  (n - a - b);
        } else if (pos == 4) {
            return  (n + 2 * a + 2 * b);
        } else if (pos == 5) {
            return  (n + 2 * a - 2 * b);
        } else if (pos == 6) {
            return  (n - 2 * a - 2 * b);
        } else if (pos == 7) {
            return  (2 * n + a + b);
        } else if (pos == 8) {
            return  (2 * n + a - b);
        } else if (pos == 9) {
            return  (2 * n - a - b);
        } else if (pos == 10) {
            return  (2 * n + 2 * a + 2 * b);
        } else if (pos == 11) {
            return  (2 * n + 2 * a - 2 * b);
        } else if (pos == 12) {
            return  (2 * n - 2 * a - 2 * b);
        } else if (pos == 13) {
            return  (3 * n + a + b);
        } else if (pos == 14) {
            return  (3 * n + a - b);
        } else if (pos == 15) {
            return  (3 * n - a - b);
        } else if (pos == 16) {
            return  (3 * n + 2 * a + 2 * b);
        } else if (pos == 17) {
            return  (3 * n + 2 * a - 2 * b);
        } else if (pos == 18) {
            return  (3 * n - 2 * a - 2 * b);
        } else {
            return 0;
        }
    }

    private static int applyDecryptFunction(byte pos, int c, byte a, byte b) {
        if (pos == 1) {
            return  (c - a - b);
        } else if (pos == 2) {
            return  (c - a + b);
        } else if (pos == 3) {
            return  (c + a + b);
        } else if (pos == 4) {
            return  (c - 2 * a - 2 * b);
        } else if (pos == 5) {
            return  (c - 2 * a + 2 * b);
        } else if (pos == 6) {
            return  (c + 2 * a + 2 * b);
        } else if (pos == 7) {
            return  ((c - a - b) / 2);
        } else if (pos == 8) {
            return  ((c - a + b) / 2);
        } else if (pos == 9) {
            return  ((c + a + b) / 2);
        } else if (pos == 10) {
            return  ((c - 2 * a - 2 * b) / 2);
        } else if (pos == 11) {
            return  ((c - 2 * a + 2 * b) / 2);
        } else if (pos == 12) {
            return  ((c + 2 * a + 2 * b) / 2);
        } else if (pos == 13) {
            return  ((c - a - b) / 3);
        } else if (pos == 14) {
            return  ((c - a + b) / 3);
        } else if (pos == 15) {
            return  ((c + a + b) / 3);
        } else if (pos == 16) {
            return  ((c - 2 * a - 2 * b) / 3);
        } else if (pos == 17) {
            return  ((c - 2 * a + 2 * b) / 3);
        } else if (pos == 18) {
            return  ((c + 2 * a + 2 * b) / 3);
        } else {
            return 0;
        }
    }

    private static int flowingByte(int i){
//        int j = 0;
//        if(i < -128){
//            j = -i - 256;
//        }else if(i > 127){
//            j = i - 256;
//        }
        return i;
    }

    public String encryptAudioFile(File f){
        byte X, Y, Z, a, b, c, d, e, F, A;
        Random rand = new Random();
        int[] inputIntArray = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 100, 120};
        Log.i("AudioAdapter", "F byte array = " + Arrays.toString(inputIntArray));
        int[] encryptedIntArray = new int[inputIntArray.length];
        X = (byte) (rand.nextInt(6) + 1);
        Y = (byte) (rand.nextInt(6) + 7);
        Z = (byte) (rand.nextInt(6) + 13);
        a = (byte) (rand.nextInt(70) + 1);
        b = (byte) (rand.nextInt(70) + 1);
        c = (byte) (rand.nextInt(70) + 1);
        d = (byte) (rand.nextInt(70) + 1);
        e = (byte) (rand.nextInt(70) + 1);
        F = (byte) (rand.nextInt(70) + 1);
        Log.i("AudioAdapter", "x = " + X + " y = " + Y + " z = " + Z + " a = " + a + " b = " + b + " c = " + c + " d = " + d + " e = " + e + " f = " + F);
        int j = 0;
        for (int i = 0; i < encryptedIntArray.length; i++) {
            if (j == 0) {
                encryptedIntArray[i] = applyEncryptFunction(X, inputIntArray[i], a, b);
            } else if (j == 1) {
                encryptedIntArray[i] = applyEncryptFunction(Y, inputIntArray[i], c, d);
            } else {
                encryptedIntArray[i] = applyEncryptFunction(Z, inputIntArray[i], e, F);
            }
            j++;
            if (j == 3) {
                j = 0;
            }
            //outputByteArray[i] = (byte)(fileByteArray[i] - 1);
        }
        Log.i("AudioAdapter", "E byte array = " + Arrays.toString(encryptedIntArray));

        int[] decryptedIntArray = new int[encryptedIntArray.length];
        j = 0;
        for (int i = 0; i < decryptedIntArray.length; i++) {
            if (j == 0) {
                decryptedIntArray[i] = applyDecryptFunction(X, encryptedIntArray[i], a, b);
            } else if (j == 1) {
                decryptedIntArray[i] = applyDecryptFunction(Y, encryptedIntArray[i], c, d);
            } else {
                decryptedIntArray[i] = applyDecryptFunction(Z, encryptedIntArray[i], e, F);
            }
            j++;
            if (j == 3) {
                j = 0;
            }
            //decryptedByteArray[i] = (byte) (fromEncryptedFile[i] + 1);
        }
        Log.i("AudioAdapter", "D byte array = " + Arrays.toString(decryptedIntArray));
        return null;
    }


//    public String encryptAudioFile(File f) {
//        try {
//            byte X, Y, Z, a, b, c, d, e, F, A;
//            String finalKey = "";
//            Random rand = new Random();
//            byte[] fileByteArray = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 100, 120};//wavToByteArray(new File(context.getExternalFilesDir(null).getAbsolutePath() + "/file_example_WAV_1MG.wav"));
//            //Log.i("AudioAdapter", "F byte array = " + Arrays.toString(fileByteArray));
//            Log.i("AudioAdapter", "F byte array = " + Arrays.toString(fileByteArray));
//            byte[] outputByteArray = new byte[fileByteArray.length + 44];
//            X = (byte) (rand.nextInt(6) + 1);
//            Y = (byte) (rand.nextInt(6) + 7);
//            Z = (byte) (rand.nextInt(6) + 13);
//            a = (byte) (rand.nextInt(70) + 1);
//            b = (byte) (rand.nextInt(70) + 1);
//            c = (byte) (rand.nextInt(70) + 1);
//            d = (byte) (rand.nextInt(70) + 1);
//            e = (byte) (rand.nextInt(70) + 1);
//            F = (byte) (rand.nextInt(70) + 1);
//            A = (byte) rand.nextInt(2);
//            Log.i("AudioAdapter", "x = " + X + " y = " + Y + " z = " + Z + " a = " + a + " b = " + b + " c = " + c + " d = " + d + " e = " + e + " f = " + F + " a = " + A);
////            for(int i = 0; i < 44; i++){
////                outputByteArray[i] = wavRecorder.wavFileHeader()[i];
////            }
//            int j = 0;
//            for (int i = 0; i < outputByteArray.length - 44; i++) {
//                if (j == 0) {
//                    outputByteArray[i] = applyEncryptFunction(X, fileByteArray[i], a, b);
//                } else if (j == 1) {
//                    outputByteArray[i] = applyEncryptFunction(Y, fileByteArray[i], c, d);
//                } else {
//                    outputByteArray[i] = applyEncryptFunction(Z, fileByteArray[i], e, F);
//                }
//                j++;
//                if (j == 3) {
//                    j = 0;
//                }
//                //outputByteArray[i] = (byte)(fileByteArray[i] - 1);
//            }
//            wavRecorder.updateHeaderInformation(outputByteArray);
//            Log.i("AudioAdapter", "E byte array = " + Arrays.toString(outputByteArray));
//            //Log.i("AudioAdapter", "File byte array = " + Arrays.toString(fileByteArray));
//            File encryptedOutputFile = new File(encryptionDirectory, f.getName().replace(".wav", "") + "_encrypted.wav");
//            byteArrayToWav(outputByteArray, encryptedOutputFile);
//            File outputDecryptedFile = new File(encryptionDirectory, f.getName().replace(".wav", "") + "_decrypted.wav");
//            byte[] fromEncryptedFile = outputByteArray;//wavToByteArray(encryptedOutputFile);
//            //Log.i("AudioAdapter", "D byte array = " + Arrays.toString(fromEncryptedFile));
//            byte[] decryptedByteArray = new byte[fromEncryptedFile.length];
//
////            for(int i = 0; i < 44; i++){
////                decryptedByteArray[i] = fromEncryptedFile[i];
////            }
//
////            if (A == 1) {
////                for (int i = 50; i < fromEncryptedFile.length; i++) {
////                    decryptedByteArray[i] = (byte) -fromEncryptedFile[i];
////                }
////            }
//            j = 0;
//            for (int i = 0; i < fromEncryptedFile.length; i++) {
//                if (j == 0) {
//                    decryptedByteArray[i] = applyDecryptFunction(X, fromEncryptedFile[i], a, b);
//                } else if (j == 1) {
//                    decryptedByteArray[i] = applyDecryptFunction(Y, fromEncryptedFile[i], c, d);
//                } else {
//                    decryptedByteArray[i] = applyDecryptFunction(Z, fromEncryptedFile[i], e, F);
//                }
//                j++;
//                if (j == 3) {
//                    j = 0;
//                }
//                //decryptedByteArray[i] = (byte) (fromEncryptedFile[i] + 1);
//            }
//            Log.i("AudioAdapter", "D byte array = " + Arrays.toString(decryptedByteArray));
//            byteArrayToWav(decryptedByteArray, outputDecryptedFile);
//
//
////            MediaPlayer mp = MediaPlayer.create(context, Uri.fromFile(outputFile));
////            encryptionCallback.addRecording(new Recording(outputFile, MainActivity.durationToString(mp.getDuration() / 1000), Recording.ENCRYPTED, false));
////            finalKey = Integer.toString(X)
////                    + Integer.toString(Y)
////                    + Integer.toString(a)
////                    + Integer.toString(b)
////                    + Integer.toString(c)
////                    + Integer.toString(d)
////                    + Integer.toString(e)
////                    + Integer.toString(F)
////                    + Integer.toString(A);
//            return finalKey;
//        } catch (IOException e) {
//            Log.i("Main2", "Error while converting file to byte array");
//            e.printStackTrace();
//            return null;
//        }
//    }

    public interface EncryptionCallback {
        public void addRecording(Recording rec);
    }

}
