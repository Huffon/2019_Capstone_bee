package com.example.huffon.bee_and;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Huffon on 6/1/2019.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
    List<Chat> mChat;
    String stEmail;

    final static int right = 1;
    final static int left = 2;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public TextView userID;

        public MyViewHolder(View ItemView) {
            super(ItemView);
            mTextView = (TextView)ItemView.findViewById(R.id.mTextView);
            userID = (TextView) ItemView.findViewById(R.id.tvID);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ChatAdapter(List<Chat> mChat, String email) {
        this.mChat = mChat;
        this.stEmail = email;
    }

    @Override
    public int getItemViewType(int position) {
        if (mChat.get(position).getEmail().equals(stEmail)) {
            return right;
        } else {
            return left;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChatAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        View v;
        if (viewType == 1){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.right_text_view, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.left_text_view, parent, false);
        }

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mTextView.setText(mChat.get(position).getText());
        holder.userID.setText(mChat.get(position).getEmail());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mChat.size();
    }
}