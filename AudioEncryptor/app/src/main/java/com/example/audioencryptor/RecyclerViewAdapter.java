package com.example.audioencryptor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<Recording> data;
    private Context context;
    private RecyclerViewCallback recyclerViewCallback;
    public RecyclerViewAdapter(List<Recording> data, Context context, RecyclerViewCallback recyclerViewCallback){
        this.recyclerViewCallback = recyclerViewCallback;
        this.data = data;
        this.context = context;
    }
    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list_item, parent, false));
    }
    public void setRecordingList(ArrayList<Recording> recordingList){ data = recordingList; }
    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        final Recording recording = data.get(position);
        holder.fileNameTextView.setText(recording.getName());
        holder.fileDurationTextView.setText(recording.getTimeString());
        if(recording.getEncryptionState() == 0){
            holder.fileStateTextView.setText("Undefined");
        }else if(recording.getEncryptionState() == 1){
            holder.fileStateTextView.setText("Decrypted");
        }
        else if(recording.getEncryptionState() == 2){
            holder.fileStateTextView.setText("Encrypted");
        }
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog whereToSaveDialog = new AlertDialog.Builder(context)
                        .setMessage("Are yo sure you want to delete this item?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                recyclerViewCallback.refreshRecyclerView(recording);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        })
                        .create();
                whereToSaveDialog.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() { return data.size(); }
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView fileNameTextView;
        public TextView fileStateTextView;
        public TextView fileDurationTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.fileNameTextView);
            fileStateTextView = itemView.findViewById(R.id.fileStateTextView);
            fileDurationTextView = itemView.findViewById(R.id.fileDurationTextView);
        }
    }
    public interface RecyclerViewCallback{
        void refreshRecyclerView(Recording r);
    }
}
