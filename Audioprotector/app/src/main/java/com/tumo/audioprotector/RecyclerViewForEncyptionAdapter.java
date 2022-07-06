package com.tumo.audioprotector;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class RecyclerViewForEncyptionAdapter extends RecyclerView.Adapter<RecyclerViewForEncyptionAdapter.ViewHolder>{
    private ArrayList<File> files;
    private Context context;
    private EncryptionRecyclerViewAdapterCallback callback;
    public RecyclerViewForEncyptionAdapter(File[] filesArray, Context context, EncryptionRecyclerViewAdapterCallback callback){
        this.context = context;
        this.callback = callback;
        files = new ArrayList<>();
        files.addAll(Arrays.asList(filesArray));
    }

    @NonNull
    @Override
    public RecyclerViewForEncyptionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.encryption_recycler_view_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewForEncyptionAdapter.ViewHolder holder, int position) {
        final File f = files.get(position);
        holder.fileNameTextView.setText(f.getName());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View view) {
                final AlertDialog alert = new AlertDialog.Builder(context)
                        .setMessage("Do you want to encrypt this file?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                callback.encrypt(f);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                alert.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() { return files.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView fileNameTextView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.fileNameTextViewInEncryptionRecyclerView);
        }
    }
    public interface EncryptionRecyclerViewAdapterCallback{
        void encrypt(File f);
    }
}

