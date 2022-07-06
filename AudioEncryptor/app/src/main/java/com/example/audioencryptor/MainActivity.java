package com.example.audioencryptor;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import static com.example.audioencryptor.MyAudioAdapter.*;

public class MainActivity extends AppCompatActivity {
    private Button startButton;
    private Button stopButton;
    private Button clearButton;
    private TextView timerTextView;
    //private TextView temporaryTextView;
    private byte[] recordingResult;
    private File outputFile;
    private AppMemory appMemory;
    private ArrayList<Recording> recordingList;
    private EditText fileNameEditTextInDialog;
    private MyAudioAdapter audioAdapter;
    private TimerForRecording timer;
    private RecyclerView filesRecyclerView;
    private RecyclerViewAdapter adapter;
    private String TAG = "Main1";
    private File uploadingDirectory;
    private File encryptionDirectory;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = findViewById(R.id.btnStart);
        stopButton = findViewById(R.id.btnStop);
        clearButton = findViewById(R.id.clearButton);
        timerTextView = findViewById(R.id.timerTextView);
        //temporaryTextView = findViewById(R.id.temporaryTextView);
        filesRecyclerView = findViewById(R.id.recyclerView);
        uploadingDirectory = new File(getExternalFilesDir(null).getAbsolutePath() + "/" + "Uploading directory");
        uploadingDirectory.mkdirs();
        encryptionDirectory = new File(getExternalFilesDir(null).getAbsolutePath() + "/Encrypted recordings directory");
        encryptionDirectory.mkdirs();
        startButton.setOnClickListener(startButtonClickListener);
        stopButton.setOnClickListener(stopButtonClickListener);
        clearButton.setOnClickListener(clearButtonClickListener);
        stopButton.setEnabled(false);
        appMemory = new AppMemory(this);
        Encryptor encryptor = new Encryptor();
        audioAdapter = new MyAudioAdapter(getFilesDir().getAbsolutePath(), getExternalFilesDir(null).getAbsolutePath(), encryptionCallback, this, encryptionDirectory);
        audioAdapter.init();
        recordingList = appMemory.getRecordingList();
        appMemory.externalStorageCheck(recordingList, uploadingDirectory);
        timer = new TimerForRecording(timerTextView, this);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        filesRecyclerView.setLayoutManager(manager);
        adapter = new RecyclerViewAdapter(recordingList, MainActivity.this, recyclerViewCallback);
        filesRecyclerView.setAdapter(adapter);
        if(!recordingList.isEmpty()){
            audioAdapter.encryptAudioFile(recordingList.get(0).getRecordingFile());
        }

    }


    //***********Button Listeners***********
    private View.OnClickListener startButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                audioAdapter.startRecording(v.getContext());
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                timer.startTimer();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private View.OnClickListener stopButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String currentFileTime = timer.stopTimer();
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            try {
                recordingResult = audioAdapter.stopRecording();
                if (recordingResult != null) {
                    Log.i(TAG, "Starting Dialog");
                    fileNameEditTextInDialog = new EditText(MainActivity.this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    fileNameEditTextInDialog.setLayoutParams(lp);
                    fileNameEditTextInDialog.setHint("File name");
                    final AlertDialog whereToSaveDialog = new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Where to save recording?")
                            .setCancelable(false)
                            .setView(fileNameEditTextInDialog)
                            .setPositiveButton("Accessible storage", null)
                            .setNegativeButton("Inaccessible storage", null)
                            .setNeutralButton("Delete recording", null)
                            .create();
                    whereToSaveDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            Button positiveButton = whereToSaveDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            Button negativeButton = whereToSaveDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                            Button neutralButton = whereToSaveDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                            positiveButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.i(TAG, "Accessible storage button pressed");
                                    boolean nameIsNotUsed = true;
                                    if (fileNameEditTextInDialog.getText().toString().length() > 0) {
                                        for (File f : new File(EXTERNAL_DIR).listFiles()) {
                                            if (f.getName().replace(".wav", "").equals(fileNameEditTextInDialog.getText().toString())) {
                                                Log.i(TAG, f.getName() + " :is equal to: ");
                                                nameIsNotUsed = false;
                                            }
                                            Log.i(TAG, "Checked: " + f.getName() + " :and: " + fileNameEditTextInDialog.getText().toString());
                                        }
                                        for (File f : new File(INTERNAL_DIR).listFiles()) {
                                            if (f.getName().replace(".wav", "").equals(fileNameEditTextInDialog.getText().toString())) {
                                                Log.i(TAG, f.getName() + " :is equal to: ");
                                                nameIsNotUsed = false;
                                            }
                                            Log.i(TAG, "Checked: " + f.getName() + " :and: " + fileNameEditTextInDialog.getText().toString());
                                        }
                                        if (nameIsNotUsed) {
                                            outputFile = new File(EXTERNAL_DIR, fileNameEditTextInDialog.getText().toString() + ".wav");
                                            try {
                                                recordingList.add(new Recording(audioAdapter.byteArrayToWav(recordingResult, outputFile), currentFileTime, Recording.DECRYPTED, true));
                                                Log.i(TAG, "file written");
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                Log.i(TAG, "File Not Found");
                                            }
                                            adapter.setRecordingList(recordingList);
                                            filesRecyclerView.setAdapter(adapter);
                                            Log.i(TAG, "recycler view updated");
                                            whereToSaveDialog.dismiss();
                                        } else {
                                            Toast.makeText(MainActivity.this, "File with this name already exists", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(MainActivity.this, "Please enter file name", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            negativeButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.i(TAG, "Inaccessible storage");
                                    boolean nameIsNotUsed = true;
                                    if (fileNameEditTextInDialog.getText().toString().length() > 0) {
                                        for (File f : new File(EXTERNAL_DIR).listFiles()) {
                                            if (f.getName().replace(".wav", "").equals(fileNameEditTextInDialog.getText().toString())) {
                                                Log.i(TAG, f.getName() + " :is equal to: ");
                                                nameIsNotUsed = false;
                                            }
                                            Log.i(TAG, "Checked: " + f.getName() + " :and: " + fileNameEditTextInDialog.getText().toString());
                                        }
                                        for (File f : new File(INTERNAL_DIR).listFiles()) {
                                            if (f.getName().replace(".wav", "").equals(fileNameEditTextInDialog.getText().toString())) {
                                                Log.i(TAG, f.getName() + " :is equal to: ");
                                                nameIsNotUsed = false;
                                            }
                                            Log.i(TAG, "Checked: " + f.getName() + " :and: " + fileNameEditTextInDialog.getText().toString());
                                        }
                                        if (nameIsNotUsed) {
                                            outputFile = new File(INTERNAL_DIR, fileNameEditTextInDialog.getText().toString() + ".wav");
                                            try {
                                                recordingList.add(new Recording(audioAdapter.byteArrayToWav(recordingResult, outputFile), currentFileTime, Recording.DECRYPTED, true));
                                                Log.i(TAG, "file written");
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            adapter.setRecordingList(recordingList);
                                            filesRecyclerView.setAdapter(adapter);
                                            Log.i(TAG, "recycler view updated");
                                            whereToSaveDialog.dismiss();
                                        } else {
                                            Toast.makeText(MainActivity.this, "File with this name already exists", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(MainActivity.this, "Please enter file name", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            neutralButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.i(TAG, "file deleted");
                                    whereToSaveDialog.dismiss();
                                }
                            });
                        }
                    });
                    whereToSaveDialog.show();
                }
                else{
                    Log.i(TAG, "null returned");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    private View.OnClickListener clearButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog clearAllVerificationDialog = new AlertDialog.Builder(MainActivity.this)
                    .setMessage("Are you sure want to delete all recordings?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int counter = 0;
                            File internalDirectory = new File(audioAdapter.INTERNAL_DIR);
                            File externalDirectory = new File(audioAdapter.EXTERNAL_DIR);
                            if (internalDirectory.isDirectory()) {
                                for (File child : internalDirectory.listFiles()) {
                                    child.delete();
                                }
                                Log.i(TAG, "internal storage cleared");
                                counter++;
                            } else {
                                Log.i(TAG, "error while clearing internal storage");
                            }

                            if (externalDirectory.isDirectory()) {
                                for (File child : externalDirectory.listFiles()) {
                                    child.delete();
                                }
                                Log.i(TAG, "external storage cleared");
                                counter++;
                            } else {
                                Log.i(TAG, "error while clearing external storage");
                            }
                            if (AppMemory.serviceFilesDirectory.isDirectory()) {
                                for (File child : AppMemory.serviceFilesDirectory.listFiles()) {
                                    child.delete();
                                }
                                Log.i(TAG, "Service files cleared");
                                counter++;
                            } else {
                                Log.i(TAG, "error while clearing external storage");
                            }
                            if (counter == 3) {
                                Toast.makeText(MainActivity.this, "Cleared all recordings", Toast.LENGTH_SHORT).show();
                                recordingList.clear();
                                adapter.setRecordingList(recordingList);
                                filesRecyclerView.setAdapter(adapter);
                                Log.i(TAG, "recycler view updated");
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
            clearAllVerificationDialog.show();
        }
    };


    @Override
    protected void onStop() {
        super.onStop();

        try {
            appMemory.saveForExit(recordingList);
            Log.i(TAG, "saved to exit");
        } catch (IOException e) {
            Log.i(TAG, "Error while saving app memory");
            e.printStackTrace();
        }
    }

    private RecyclerViewAdapter.RecyclerViewCallback recyclerViewCallback = new RecyclerViewAdapter.RecyclerViewCallback() {
        @Override
        public void refreshRecyclerView(Recording r) {
            recordingList.remove(r);
            r.getRecordingFile().delete();
            adapter.setRecordingList(recordingList);
            filesRecyclerView.setAdapter(adapter);
            Log.i(TAG, "recycler view updated");
        }
    };
    private MyAudioAdapter.EncryptionCallback encryptionCallback = new MyAudioAdapter.EncryptionCallback(){

        @Override
        public void addRecording(Recording rec) {
            recordingList.add(rec);
            adapter.setRecordingList(recordingList);
            filesRecyclerView.setAdapter(adapter);
        }
    };
    //**********Other methods****************
    public static String durationToString(long fileDuration) {
        int seconds;
        int minutes;
        long hours;
        String label = "";
        if (fileDuration > 0 && fileDuration < 60) {
            seconds = (int) fileDuration;
            minutes = 0;
            hours = 0;
        } else if (fileDuration == 0) {
            seconds = 0;
            minutes = 0;
            hours = 0;
        } else {
            seconds = (int) fileDuration % 60;
            minutes = (int) fileDuration / 60;
            if (minutes > 60) {
                hours = minutes / 60;
                minutes = minutes % 60;
            }
            hours = 0;
        }
        if (hours < 10 && minutes < 10 && seconds < 10) {
            label = "0" + hours + ":0" + minutes + ":0" + seconds;
        } else if (hours < 10 && minutes >= 10 && seconds < 10) {
            label = "0" + hours + ":" + minutes + ":0" + seconds;
        } else if (hours < 10 && minutes >= 10 && seconds >= 10) {
            label = "0" + hours + ":" + minutes + ":" + seconds;
        } else if (hours < 10 && minutes < 10 && seconds >= 10) {
            label = "0" + hours + ":0" + minutes + ":" + seconds;
        } else if (hours >= 10 && minutes < 10 && seconds < 10) {
            label = hours + ":0" + minutes + ":0" + seconds;
        } else if (hours >= 10 && minutes >= 10 && seconds < 10) {
            label = hours + ":" + minutes + ":0" + seconds;
        } else if (hours >= 10 && minutes >= 10 && seconds >= 10) {
            label = hours + ":" + minutes + ":" + seconds;
        } else if (hours >= 10 && minutes < 10 && seconds >= 10) {
            label = hours + ":0" + minutes + ":" + seconds;
        }
        return label;
    }
}