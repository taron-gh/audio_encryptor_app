package com.example.audioencryptor;

import android.app.Presentation;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class WavRecorder {
    private AudioRecord recorder = null;
    private boolean isRecording = false;
    private Thread recordingThread = null;
    private static final int RECORDER_SAMPLE_RATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_8BIT;
    private static final short BITS_PER_SAMPLE = 8;
    private static final short NUMBER_CHANNELS = 1;
    private static final int BYTE_RATE = RECORDER_SAMPLE_RATE * NUMBER_CHANNELS * 8 / 8;
    private static int BufferElements2Rec = 1024;
    private Context context;
    private byte[] returnValues = null;
    public WavRecorder(Context context) {
        this.context = context;
    }

    public void startRecording() {


        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLE_RATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, 512);

        recorder.startRecording();
        isRecording = true;
        returnValues = null;
        recordingThread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                try {
                    returnValues = getAudioData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        recordingThread.start();
    }

    public byte[] stopRecording() {
        isRecording = false;
        recorder.stop();
        recorder.release();
        recordingThread = null;
        recorder = null;
        return returnValues;
    }

    private byte[] short2byte(short[] sData) {
        int arrSize = sData.length;
        byte[] bytes = new byte[arrSize * 2];
        for (int i = 0; i < arrSize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[i * 2 + 1] = (byte) ((int) sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }
    private byte[] getAudioData() throws IOException {
        byte[] bData = new byte[BufferElements2Rec];

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        for (byte b : wavFileHeader()) {
            bos.write(b);
        }

        while (isRecording) {
            // gets the voice output from microphone to byte format
            recorder.read(bData, 0, BufferElements2Rec);
            Log.i("111", "R byte array = " + Arrays.toString(bData));
            bos.write(bData);
        }

        //Log.i("AudioAdapter", "R byte array = " + data.toString());
        byte[] result = bos.toByteArray();
        updateHeaderInformation(result);
        Log.i("AudioAdapter", "R byte array = " + Arrays.toString(result));
        return result;
    }

    private static int flowingByte(int i){
        int j = 0;
        if(i < -128){
            j = -i - 256;
        }else if(i > 127){
            j = i - 256;
        }
        return i;
    }

    public byte[] wavFileHeader() {
        int headerSize = 44;
        byte[] header = new byte[headerSize];

        header[0] = (byte) 'R'; // RIFF/WAVE header
        header[1] = (byte) 'I';
        header[2] = (byte) 'F';
        header[3] = (byte) 'F';

        header[4] = (byte) (0 & 0xff); // Size of the overall file, 0 because unknown
        header[5] = (byte) (0 >> 8 & 0xff);
        header[6] = (byte) (0 >> 16 & 0xff);
        header[7] = (byte) (0 >> 24 & 0xff);

        header[8] = (byte) 'W';
        header[9] = (byte) 'A';
        header[10] = (byte) 'V';
        header[11] = (byte) 'E';

        header[12] = (byte) 'f'; // 'fmt ' chunk
        header[13] = (byte) 'm';
        header[14] = (byte) 't';
        header[15] = (byte) ' ';

        header[16] = 16; // Length of format data
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;

        header[20] = 1; // Type of format (1 is PCM)
        header[21] = 0;

        header[22] = (byte) NUMBER_CHANNELS;
        header[23] = 0;

        header[24] = (byte) (RECORDER_SAMPLE_RATE & 0xff); // Sampling rate
        header[25] = (byte) (RECORDER_SAMPLE_RATE >> 8 & 0xff);
        header[26] = (byte) (RECORDER_SAMPLE_RATE >> 16 & 0xff);
        header[27] = (byte) (RECORDER_SAMPLE_RATE >> 24 & 0xff);

        header[28] = (byte) (BYTE_RATE & 0xff); // Byte rate = (Sample Rate * BitsPerSample * Channels) / 8
        header[29] = (byte) (BYTE_RATE >> 8 & 0xff);
        header[30] = (byte) (BYTE_RATE >> 16 & 0xff);
        header[31] = (byte) (BYTE_RATE >> 24 & 0xff);

        header[32] = (byte) (NUMBER_CHANNELS * BITS_PER_SAMPLE / 8); //  16 Bits stereo
        header[33] = 0;

        header[34] = (byte) BITS_PER_SAMPLE; // Bits per sample
        header[35] = 0;

        header[36] = (byte) 'd';
        header[37] = (byte) 'a';
        header[38] = (byte) 't';
        header[39] = (byte) 'a';

        header[40] = (byte) (0 & 0xff); // Size of the data section.
        header[41] = (byte) (0 >> 8 & 0xff);
        header[42] = (byte) (0 >> 16 & 0xff);
        header[43] = (byte) (0 >> 24 & 0xff);

        return header;
    }
    public void updateHeaderInformation(byte[] data) {
        int fileSize = data.length;
        int contentSize = fileSize - 44;

        data[4] = (byte) (fileSize & 0xff); // Size of the overall file
        data[5] = (byte) (fileSize >> 8 & 0xff);
        data[6] = (byte) (fileSize >> 16 & 0xff);
        data[7] = (byte) (fileSize >> 24 & 0xff);

        data[40] = (byte) (contentSize & 0xff); // Size of the data section.
        data[41] = (byte) (contentSize >> 8 & 0xff);
        data[42] = (byte) (contentSize >> 16 & 0xff);
        data[43] = (byte) (contentSize >> 24 & 0xff);
    }

}



