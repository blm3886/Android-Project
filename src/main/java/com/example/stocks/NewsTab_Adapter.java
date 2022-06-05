package com.example.stocks;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NewsTab_Adapter extends RecyclerView.Adapter<NewsTab_Adapter.MyViewHolder> {
    Context context;
    ArrayList<NewsModel> newsModel;
    private OnNoteListener onNoteListener;

    public NewsTab_Adapter(Context context, ArrayList<NewsModel>  newsModel, OnNoteListener onNoteListener) {
         this.context = context;
         this.newsModel = newsModel;
         this.onNoteListener = onNoteListener;

    }

    @NonNull
    @Override
    public NewsTab_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflator = LayoutInflater.from(context);
        View view = inflator.inflate(R.layout.reclycler_view_row,parent,false);
        return new NewsTab_Adapter.MyViewHolder(view, onNoteListener);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsTab_Adapter.MyViewHolder holder, int position) {
          holder.title.setText(newsModel.get(position).getNewsSource());
          holder.description.setText(newsModel.get(position).getNewsTitle());
          holder.hoursElapsed.setText(newsModel.get(position).getHoursElapsed());
        Log.e(null, "onBindViewHolder: "+"REC"+newsModel.get(position).getImageSrc());
          Picasso.get()
                .load(newsModel.get(position).getImageSrc())
                .into(holder.img);

    }

    @Override
    public int getItemCount(){
        return newsModel.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{
        TextView title;
        TextView hoursElapsed;
        TextView description;
        ImageView img;
        OnNoteListener onNoteListener;

        public MyViewHolder(@NonNull View itemView, OnNoteListener onNoteListener) {
            super(itemView);

            img = itemView.findViewById(R.id.NEWS_LOGO);
            title = itemView.findViewById(R.id.NEWS_SOURCE);
            hoursElapsed = itemView.findViewById(R.id.TIME_ELAPSED);
            description = itemView.findViewById(R.id.NEWS_TITLE);
            img = itemView.findViewById(R.id.NEWS_LOGO);
            this.onNoteListener = onNoteListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.e(null, "onClick: "+"CLICK DETECTED");
              onNoteListener.onNoteClick(getAdapterPosition());
        }
    }

    public interface OnNoteListener{
        void onNoteClick(int position);
    }
}
