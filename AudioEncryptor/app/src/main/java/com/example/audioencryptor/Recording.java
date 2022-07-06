package com.example.audioencryptor;

import java.io.File;

public class Recording {
    public static int UNDEFINED_ENCRYPTION_STATE = 0;
    public static int DECRYPTED = 1;
    public static int ENCRYPTED = 2;
    private int isEncrypted = 0; // 0 - undefined, 1 - decrypted, 2 - encrypted
    private File recordingFile;
    private String durationString;
    public boolean recordedState = false; // true - recorded, false uploaded
    public Recording(File file, String durationString, int isEncrypted, boolean recordedState){
        recordingFile = file;
        this.recordedState = recordedState;
        this.durationString = durationString;
        this.isEncrypted = isEncrypted;
    }

    public int getEncryptionState() {
        return isEncrypted;
    }

    public File getRecordingFile() {
        return recordingFile;
    }
    public String getName(){return recordingFile.getName(); }
    public String getTimeString(){return durationString; }
    public String getPath(){ return recordingFile.getPath(); }
}
