package com.example.samsungshoeshoo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.itemViewHolder> {

    private Context mCtx;
    private List<ListItem> itemList; // list to contain items in adapter
    private OnItemClickListener mListener;
    private OnDeleteClickListener nListener;

    // interface to communicate with main activity
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    // called in main activity
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        nListener = listener;
    }

    public static class itemViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView textViewType, textViewColour, textViewDate;
        Button deployBut;
        ImageButton deleteBut;

        itemViewHolder(@NonNull View itemView, OnItemClickListener listener, OnDeleteClickListener delListener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textViewType = itemView.findViewById(R.id.textViewType);
            textViewColour = itemView.findViewById(R.id.textViewColour);
//            textViewShelfId = itemView.findViewById(R.id.textViewShelfId);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            deployBut = itemView.findViewById(R.id.deployButton);
            deleteBut = itemView.findViewById(R.id.deleteButton);

            // listener for deploy button
            deployBut.setOnClickListener(v -> {
                if(listener != null) {
                    int position = getAdapterPosition();

                    // check if position is valid
                    if(position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });

            // listener for delete button
            deleteBut.setOnClickListener(v -> {
                if(delListener != null) {
                    int position = getAdapterPosition();
                    // check if position is valid
                    if(position != RecyclerView.NO_POSITION) {
                        delListener.onDeleteClick(position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    MyAdapter(Context mCtx, List<ListItem> itemList) {
        this.mCtx = mCtx;
        this.itemList = itemList;
    }

    // returns an instance of the class itemViewHolder
    @NonNull
    @Override
    public itemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.list_item, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new itemViewHolder(view, mListener, nListener);
    }

    public String Capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    // binds item to view holder
    @Override
    public void onBindViewHolder(@NonNull itemViewHolder itemViewHolder, int position) {
        ListItem item = itemList.get(position);
        itemViewHolder.textViewType.setText(Capitalize(item.getType()));
        itemViewHolder.textViewColour.setText(Capitalize(item.getColour()));
        itemViewHolder.textViewDate.setText("Stored " + item.getDate() + " days ago");
        //        itemViewHolder.textViewShelfId.setText("Shelf ID: " + String.valueOf(item.getShelfId()));

        // For local testing of img
//        Context context = itemViewHolder.imageView.getContext();
//        int id = context.getResources().getIdentifier(item.getImage(), "drawable", context.getPackageName());
//        itemViewHolder.imageView.setImageDrawable(mCtx.getResources().getDrawable(id));

        // For receiving img in Bytes
        try {
            byte[] decodedString = Base64.decode(item.getImage(), Base64.DEFAULT);
            Bitmap decodedImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            itemViewHolder.imageView.setImageBitmap(decodedImg);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.e("Image received in wrong format", item.getImage());
        }


    }



}
