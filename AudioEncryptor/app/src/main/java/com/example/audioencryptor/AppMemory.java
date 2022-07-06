package com.example.audioencryptor;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class AppMemory {
    private Context context;
    public static   File serviceFilesDirectory;
    private File recordingListFile;
    private File internalRecordingDirectory;
    private File externalRecordingDirectory;
    private Gson gson;
    private String TAG = "Main1";
    public AppMemory(Context context){
        this.context = context;
        //this.audioAdapter = audioAdapter;
        internalRecordingDirectory = new File(context.getFilesDir().getAbsolutePath() + "/Recording directory");
        externalRecordingDirectory = new File(context.getExternalFilesDir(null).getAbsolutePath()  + "/Recording directory");
        gson = new Gson();
        serviceFilesDirectory = new File(context.getFilesDir().getAbsolutePath(), "Service Files");
        serviceFilesDirectory.mkdirs();
        recordingListFile = new File(serviceFilesDirectory.getAbsolutePath(), "uploadedFiles.txt");
    }
    public void saveForExit(ArrayList recordings) throws IOException {
        for(int i = 0; i < recordings.size(); i++){
            Recording recording = (Recording) recordings.get(i);
            if(!recording.recordedState){
                recordings.remove(recording);
            }

        }
        String finalString = gson.toJson(recordings);
        FileOutputStream fos1 = new FileOutputStream(recordingListFile);
        fos1.write(finalString.getBytes());
        Log.i("AppMemory", "Saved for exit");
    }
    public <T> ArrayList<T> getRecordingList() {
        String inputString;

        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(recordingListFile));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            inputString = text.toString();
            if(inputString.length() > 0){
                Type forReturn = new TypeToken<ArrayList<Recording>>(){}.getType();
                return gson.fromJson(inputString, forReturn);
            }else{
                return new ArrayList<T>();
            }
        }catch(IOException e){
            return new ArrayList<T>();
        }

    }
    public void externalStorageCheck(ArrayList<Recording> recordings, File uploadingDirectory) {
        Log.i(TAG, "Check started in folder: " + uploadingDirectory.getAbsolutePath());
        ArrayList<Recording> innerRecordings = new ArrayList<>();
        if (uploadingDirectory.listFiles() != null) {
            Log.i(TAG, "File found");
            for (File f : uploadingDirectory.listFiles()) {
                if (f.getName().contains(".wav")) {
                    Log.i(TAG, "getting duration");
                    MediaPlayer mp = MediaPlayer.create(context, Uri.fromFile(f));
                    String durationString = null;
                    durationString = MainActivity.durationToString(mp.getDuration() / 1000);
                    Recording recording = new Recording(f, durationString, Recording.UNDEFINED_ENCRYPTION_STATE, false);
//                    boolean containsRecording = false;
//                    for (Recording r : innerRecordings) {
//                        if (r.equals(recording)) {
//                            containsRecording = true;
//                            break;
//                        }
//                    }
                    if (!innerRecordings.contains(recording) && !recordings.contains(recording)) {
                        innerRecordings.add(recording);
                    }
                }
            }
            recordings.addAll(innerRecordings);
        }

    }
}
