package com.example.kirill.lab2.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.kirill.lab2.PostDetailActivity;
import com.example.kirill.lab2.R;
import com.example.kirill.lab2.model.Meeting;
import com.example.kirill.lab2.model.User;
import com.example.kirill.lab2.viewholder.PostViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;


public abstract class PostListFragment extends Fragment {

    private static final String TAG = "PostListFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<Meeting, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    protected boolean marker = false;

    public PostListFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mRecycler = (RecyclerView) rootView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(mDatabase);
        mAdapter = new FirebaseRecyclerAdapter<Meeting, PostViewHolder>(Meeting.class, R.layout.item_post,
                PostViewHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final PostViewHolder viewHolder, final Meeting model, final int position) {
                final DatabaseReference postRef = getRef(position);

                // Set click listener for the whole post view
                final String postKey = postRef.getKey();
                if(marker) {
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        new AlertDialog.Builder(getContext())
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Delete meeting")
                                .setMessage("Are you sure you want to delete this meeting?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(getContext(), "Removing", Toast.LENGTH_SHORT).show();
                                        postRef.removeValue();
                                        mDatabase.child("user-meetings").child(getUid()).child(postKey).removeValue();
                                        for (final Map.Entry<String,Boolean> entry: model.pres.entrySet()) {
                                            mDatabase.child("users").child(entry.getKey()).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    mDatabase.child("user-meetings").child(entry.getKey()).child(postKey).removeValue();
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }

                                })
                                .setNegativeButton("No", null)
                                .show();
                        return true;
                    }
                });
//                if(marker) {
                    // Determine if the current user has liked this post and set UI accordingly
                    if (model.pres.containsKey(getUid())) {
                        viewHolder.starView.setImageResource(R.drawable.ic_add_box);
                    } else {
                        viewHolder.starView.setImageResource(R.drawable.ic_indeterminate_check_box);
                    }

                }
                    viewHolder.bindToPost(model, new View.OnClickListener() {
                        @Override
                        public void onClick(View starView) {
                            // Need to write to both places the post is stored
                            DatabaseReference globalPostRef = mDatabase.child("meetings").child(postRef.getKey());
                            // Run two transactions
                            onStarClicked(globalPostRef, postKey, model);
                        }
                    });

            }
        };
        mRecycler.setAdapter(mAdapter);
    }




    private void onStarClicked(DatabaseReference postRef, final String postKey, final Meeting model) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Meeting p = mutableData.getValue(Meeting.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.pres.containsKey(getUid())) {
                    // Unstar the post and remove self from stars
                    mDatabase.child("user-meetings").child(getUid()).child(postKey).removeValue();
                    p.pres.remove(getUid());
                } else {
                    // Star the post and add self to stars
                    mDatabase.child("user-meetings").child(getUid()).child(postKey).setValue(model);
                    p.pres.put(getUid(), true);

                }

                // Set value and report transaction success
                mutableData.setValue(p);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);

}
