package com.example.eduhelper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ViewHolder> {

    Context context;
    List<ClassItem> classList;

    public ClassAdapter(Context context, List<ClassItem> classList) {
        this.context = context;
        this.classList = classList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.class_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassItem item = classList.get(position);
        holder.className.setText(item.getClassName());

        holder.viewNotes.setOnClickListener(v -> handleLink(item.getNotesLink(), "Notes not available"));
        holder.viewQP.setOnClickListener(v -> handleLink(item.getQuestionPaperLink(), "Question paper not available"));
        holder.viewVideo.setOnClickListener(v -> handleLink(item.getVideoLink(), "Video not available"));
    }

    private void handleLink(String url, String errorMessage) {
        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri uri = Uri.parse(url.trim());

            if (uri.getScheme() == null || (!uri.getScheme().equals("http") && !uri.getScheme().equals("https"))) {
                Toast.makeText(context, "Invalid URL scheme", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Could not open the link", Toast.LENGTH_SHORT).show();
            Log.e("LINK_ERROR", "Failed to open link: " + url, e);
        }
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView className;
        Button viewNotes, viewQP, viewVideo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            className = itemView.findViewById(R.id.className);
            viewNotes = itemView.findViewById(R.id.viewNotes);
            viewQP = itemView.findViewById(R.id.viewQP);
            viewVideo = itemView.findViewById(R.id.viewVideo);
        }
    }
}
