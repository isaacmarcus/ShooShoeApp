package com.example.samsungshoeshoo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.itemViewHolder> {

    private Context mCtx;
    private List<ListItem> itemList; // list to contain items in adapter
    private OnItemClickListener mListener;

    // interface to communicate with main activity
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // called in main activity
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class itemViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView textViewType, textViewColour, textViewShelfId;
        Button deployBut;

        itemViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textViewType = itemView.findViewById(R.id.textViewType);
            textViewColour = itemView.findViewById(R.id.textViewColour);
            textViewShelfId = itemView.findViewById(R.id.textViewShelfId);
            deployBut = itemView.findViewById(R.id.deployButton);

            // WORKING
            deployBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        int position = getAdapterPosition();
                        // check if position is valid
                        if(position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
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
        return new itemViewHolder(view, mListener);
    }

    // binds item to view holder
    @Override
    public void onBindViewHolder(@NonNull itemViewHolder itemViewHolder, int position) {
        ListItem item = itemList.get(position);

        itemViewHolder.textViewType.setText(item.getType());
        itemViewHolder.textViewColour.setText(item.getColour());
        itemViewHolder.textViewShelfId.setText(String.valueOf(item.getShelfId()));

        // For local testing of img
        Context context = itemViewHolder.imageView.getContext();
        int id = context.getResources().getIdentifier(item.getImage(), "drawable", context.getPackageName());
        itemViewHolder.imageView.setImageDrawable(mCtx.getResources().getDrawable(id));

        // For receiving img in Bytes
//        byte[] decodedString = Base64.decode(item.getImage(), Base64.DEFAULT);
//        Bitmap decodedImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//        itemViewHolder.imageView.setImageBitmap(decodedImg);

    }



}
