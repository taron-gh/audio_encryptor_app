package com.tumo.audioprotector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private SharedPreferences sharedPreferences;
    private BigInteger q;
    private BigInteger p;
    private RSAEncryptor rsaEncryptor;
    private String TAG = "MainActivity";
    private DirectoryManger directoryManger;
    private Button copyToClipBoardButton;
    private TextView publicKeyTextView;
    private RecyclerView encryptionRecyclerView;
    private RecyclerViewForEncyptionAdapter encryptionRecyclerViewAdapter;
    private RecyclerView decryptionRecyclerView;
    private RecyclerViewForDecryptionAdapter decryptionRecyclerViewAdapter;
    private DividerItemDecoration divider;
    private AudioEncoder audioEncoder;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //*************Permissions*************************
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_DOCUMENTS},
                MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        //*************App initialization*******************
        sharedPreferences = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        copyToClipBoardButton = findViewById(R.id.copyToClipboardButton);
        encryptionRecyclerView = findViewById(R.id.forEncryptionRecyclerView);
        decryptionRecyclerView = findViewById(R.id.forDecryptionRecyclerView);
        publicKeyTextView = findViewById(R.id.appPublicKeyTextView);
        divider = new DividerItemDecoration(this, RecyclerView.VERTICAL);
        rsaEncryptor = new RSAEncryptor();
        directoryManger = new DirectoryManger(this);
        encryptionRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        encryptionRecyclerViewAdapter = new RecyclerViewForEncyptionAdapter(directoryManger.getFilesForEncryption(), MainActivity.this, encryptionRecyclerViewAdapterCallback);
        encryptionRecyclerView.setAdapter(encryptionRecyclerViewAdapter);
        encryptionRecyclerView.addItemDecoration(divider);
        decryptionRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        decryptionRecyclerViewAdapter = new RecyclerViewForDecryptionAdapter(directoryManger.getFilesForDecryption(), MainActivity.this, decryptionRecyclerViewAdapterCallback);
        decryptionRecyclerView.setAdapter(decryptionRecyclerViewAdapter);
        decryptionRecyclerView.addItemDecoration(divider);
        if (sharedPreferences.getString("p", "0").equals("0")
                || sharedPreferences.getString("q", "0").equals("0")) {
            rsaEncryptor.generatePQ();
        } else {
            p = new BigInteger(sharedPreferences.getString("p", "0"));
            q = new BigInteger(sharedPreferences.getString("q", "0"));
            rsaEncryptor.setPQ(p, q);
        }
        publicKeyTextView.setText(rsaEncryptor.n.toString());
        copyToClipBoardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("public key", rsaEncryptor.n.toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "Copied", Toast.LENGTH_SHORT).show();
            }
        });
        audioEncoder = new AudioEncoder(directoryManger, getSystemService(Context.CLIPBOARD_SERVICE), rsaEncryptor);

    }


    //**************Overrided methods****************
    @Override
    protected void onStop() {
        super.onStop();
        rsaEncryptor.saveForExit(sharedPreferences);
    }

    //*************Interfaces**********************
    private RecyclerViewForEncyptionAdapter.EncryptionRecyclerViewAdapterCallback encryptionRecyclerViewAdapterCallback = new RecyclerViewForEncyptionAdapter.EncryptionRecyclerViewAdapterCallback() {
        @Override
        public void encrypt(final File f) {
            final BigInteger[] publicKeyForEncryption = new BigInteger[1];
            final EditText keyInputEditText = new EditText(directoryManger.getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            keyInputEditText.setLayoutParams(lp);
            keyInputEditText.setHint("Public key");
            final AlertDialog dialog = new AlertDialog.Builder(directoryManger.getContext())
                    .setMessage("Please enter public key for encryption")
                    .setCancelable(false)
                    .setView(keyInputEditText)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.i(TAG, "ok clicked");
                            if (!keyInputEditText.getText().toString().isEmpty()) {
                                publicKeyForEncryption[0] = new BigInteger(keyInputEditText.getText().toString());
                                try {
                                    audioEncoder.encrypt(f, publicKeyForEncryption[0]);
                                } catch (IOException e) {
                                    Log.i(TAG, "Error while encrypting file");
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(directoryManger.getContext(), "Please enter key", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            dialog.show();

        }
    };

    private RecyclerViewForDecryptionAdapter.DecryptionRecyclerViewAdapterCallback decryptionRecyclerViewAdapterCallback = new RecyclerViewForDecryptionAdapter.DecryptionRecyclerViewAdapterCallback() {
        @Override
        public void decrypt(final File f) {
            final EditText keyEditText = new EditText(MainActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            keyEditText.setLayoutParams(lp);
            keyEditText.setHint("Decryption key");
            final AlertDialog alert = new AlertDialog.Builder(directoryManger.getContext())
                    .setMessage("Please enter key for decryption.")
                    .setCancelable(false)
                    .setView(keyEditText)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.i(TAG, "OK pressed");
                            if(!keyEditText.getText().toString().isEmpty()){
                                try{
                                    audioEncoder.decrypt(f, keyEditText.getText().toString());
                                }catch(IOException ignored){
                                    Log.i(TAG, "Error while decrypting");
                                }
                            }else{
                                Toast.makeText(directoryManger.getContext(), "Please enter valid key", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            alert.show();

        }
    };
}