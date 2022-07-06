package com.tumo.audioprotector;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.Arrays;

public class DirectoryManger {
    public File uploadForEncryptionDirectory;
    public File downloadEncryptedDirectory;
    public File uploadForDecryptionDirectory;
    public File downloadDecryptedDirectory;
    private Context context;
    public DirectoryManger(Context context){
        uploadForEncryptionDirectory = new File(context.getExternalFilesDir(null).getAbsolutePath(), "For Encryption");
        uploadForEncryptionDirectory.mkdirs();
        downloadEncryptedDirectory = new File(context.getExternalFilesDir(null).getAbsolutePath(), "Encrypted");
        downloadEncryptedDirectory.mkdirs();
        uploadForDecryptionDirectory = new File(context.getExternalFilesDir(null).getAbsolutePath(), "For Decryption");
        uploadForDecryptionDirectory.mkdirs();
        downloadDecryptedDirectory = new File(context.getExternalFilesDir(null).getAbsolutePath(), "Decrypted");
        downloadDecryptedDirectory.mkdirs();
        this.context = context;
    }

    public File[] getFilesForEncryption(){ return uploadForEncryptionDirectory.listFiles(); }
    public File[] getFilesForDecryption(){ return uploadForDecryptionDirectory.listFiles(); }
    public Context getContext(){ return context; }

}
