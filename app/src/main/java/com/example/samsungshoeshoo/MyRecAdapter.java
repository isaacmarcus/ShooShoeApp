package com.example.samsungshoeshoo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class MyRecAdapter extends RecyclerView.Adapter<MyRecAdapter.itemViewHolder> {

    private Context mCtx;
    private List<ListItem> itemList; // list to contain items in adapter
    private OnItemClickListener mListener;

    private static final float CARD_SIZE_RATIO = 0.75f; // ratio for resizing card
    private static final float IMAGE_SIZE_RATIO = 0.27f;

    // interface to communicate with main activity
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // called in main activity
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class itemViewHolder extends RecyclerView.ViewHolder{

        ImageView recImageView;
        TextView recDeployBut;
        CardView recListItemCardView;

        itemViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            recImageView = itemView.findViewById(R.id.recImageView);
            recDeployBut = itemView.findViewById(R.id.recDeployButton);
            recListItemCardView = itemView.findViewById(R.id.recListItemCardView);

            // WORKING
            recDeployBut.setOnClickListener(v -> {
                if(listener != null) {
                    int position = getAdapterPosition();
                    // check if position is valid
                    if(position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    MyRecAdapter(Context mCtx, List<ListItem> itemList) {
        this.mCtx = mCtx;
        this.itemList = itemList;
    }

    // returns an instance of the class itemViewHolder
    @NonNull
    @Override
    public itemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.rec_list_item, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new MyRecAdapter.itemViewHolder(view, mListener);
    }

    public String Capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    // binds item to view holder
    @Override
    public void onBindViewHolder(@NonNull itemViewHolder itemViewHolder, int position) {
        ListItem item = itemList.get(position);

        // For local testing of img
//        Context context = itemViewHolder.imageView.getContext();
//        int id = context.getResources().getIdentifier(item.getImage(), "drawable", context.getPackageName());
//        itemViewHolder.imageView.setImageDrawable(mCtx.getResources().getDrawable(id));

        // For receiving img in Bytes
        try {
            byte[] decodedString = Base64.decode(item.getImage(), Base64.DEFAULT);
            Bitmap decodedImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            itemViewHolder.recImageView.setImageBitmap(decodedImg);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.e("Image received in wrong format", item.getImage());
        }

        // Get the actual screen width
        int screenWidth = MainHomePage.screenWidth;
        // Calculate the new image size
        int imageViewSize = (int) ((float) screenWidth * IMAGE_SIZE_RATIO);

        // Set the Image size
        RelativeLayout.LayoutParams recImgViewLayoutParams = (RelativeLayout.LayoutParams) itemViewHolder.recImageView.getLayoutParams();
        // Setting the same size for Width and height
        recImgViewLayoutParams.height = imageViewSize;
        recImgViewLayoutParams.width = imageViewSize;


    }

}
