package com.example.recorderapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;

import static com.example.recorderapp.MyAudioAdapter.*;

public class MainActivity extends AppCompatActivity {
    Button startButton;
    Button stopButton;
    byte[] recordingResult;
    File outputFile;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = findViewById(R.id.btnStart);
        stopButton = findViewById(R.id.btnStop);
        outputFile = new File(getFilesDir().getAbsolutePath() + "/sample.wav");
        stopButton.setEnabled(false);
        init();
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startRecording(v.getContext());
                    startButton.setEnabled(false);
                    stopButton.setEnabled(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                try {
                    recordingResult = stopRecording();
                    if(recordingResult != null){
                        byteArrayToWav(recordingResult, outputFile);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

}