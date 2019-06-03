package com.example.huffon.bee_and;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Huffon on 6/1/2019.
 */

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.MyViewHolder> {
    List<Friend> mFriend;
    Context context;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView tvEmail;
        public ImageView ivUser;
        public Button btnChat;

        public MyViewHolder(View ItemView) {
            super(ItemView);
            ivUser = (ImageView) ItemView.findViewById(R.id.ivUser);
            tvEmail = (TextView)ItemView.findViewById(R.id.tvEmail);
            btnChat = (Button) ItemView.findViewById(R.id.btnChat);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FriendAdapter(List<Friend> mFriend,  Context context) {
        this.mFriend = mFriend;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FriendAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_friend, parent, false);
        MyViewHolder vh = new MyViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.tvEmail.setText(mFriend.get(position).getEmail());
        String stPhoto = mFriend.get(position).getPhoto();

        if (TextUtils.isEmpty(stPhoto)){
            Picasso.with(context)
                    .load(R.mipmap.ic_nophoto)
                    .fit()
                    .centerInside()
                    .into(holder.ivUser);
        } else {
            Picasso.with(context)
                    .load(stPhoto)
                    .fit()
                    .centerInside()
                    .into(holder.ivUser);
        }

        holder.btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stFriendId = mFriend.get(position).getEmail();
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("friendID", stFriendId);
                context.startActivity(intent);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mFriend.size();
    }
}