package com.example.kirill.lab2.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kirill.lab2.R;
import com.example.kirill.lab2.model.Meeting;

public class PostViewHolder extends RecyclerView.ViewHolder {

    public TextView titleView;
    public TextView bodyView;
    public ImageView starView;

    public PostViewHolder(View itemView) {
        super(itemView);

        titleView = (TextView) itemView.findViewById(R.id.post_title);
        bodyView = (TextView) itemView.findViewById(R.id.post_body);
        starView = (ImageView) itemView.findViewById(R.id.star);
    }

    public void bindToPost(Meeting post, View.OnClickListener starClickListener) {
        titleView.setText(post.name);
        bodyView.setText(post.description);
        starView.setOnClickListener(starClickListener);
    }
}
