package com.example.recorderapp;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MyAudioAdapter {
    static MediaRecorder recorder;
    static boolean recordingStarted = false;
    static File temporaryFile;
    static public byte[] wavToByteArray(File WAV_FILE) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(WAV_FILE));

        int read;
        byte[] buff = new byte[1024];
        while ((read = in.read(buff)) > 0)
        {
            out.write(buff, 0, read);
        }
        in.close();
        out.flush();
        out.close();
        byte[] audioBytes = out.toByteArray();
        return audioBytes;
    }
    static public File byteArrayToWav(byte[] source, File outputFile) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(source);
        FileOutputStream outputStream = new FileOutputStream(outputFile);

        int read;
        byte[] buff = new byte[1024];
        while ((read = inputStream.read(buff)) > 0)
        {
            outputStream.write(buff, 0, read);
        }
        inputStream.close();
        outputStream.flush();
        outputStream.close();
        return outputFile;
    }
    static public void init(){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    static public void startRecording(Context context) throws IOException {
        temporaryFile = new File(context.getFilesDir().getAbsolutePath() + "/temporaryFile.amr");
        recorder.setOutputFile(temporaryFile);
        recorder.prepare();
        recorder.start();
        recordingStarted = true;
    }
    static public byte[] stopRecording() throws IOException {
        if(recordingStarted){
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
        }else{
            return null;
        }
    }
}
