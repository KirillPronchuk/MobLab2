package com.example.kirill.lab2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kirill.lab2.model.Meeting;
import com.example.kirill.lab2.model.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PostDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "PostDetailActivity";

    public static final String EXTRA_POST_KEY = "post_key";

    private DatabaseReference mPostReference;
    private ValueEventListener mPostListener;
    private String mPostKey;

    private TextView mTitleView;
    private TextView mBodyView;
    private TextView mStartDate;
    private TextView mEndDate;
    private TextView mPriority;
    private TextView mParticipants;
    Button smsButton;
    Intent sendIntent = new Intent(Intent.ACTION_VIEW);

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Get post key from intent
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        // Initialize Database
        mPostReference = FirebaseDatabase.getInstance().getReference()
                .child("meetings").child(mPostKey);


        // Initialize Views
        mTitleView = (TextView) findViewById(R.id.post_title);
        mBodyView = (TextView) findViewById(R.id.post_body);
        mStartDate = (TextView) findViewById(R.id.startDate);
        mEndDate = (TextView) findViewById(R.id.endDate);
        mPriority = (TextView) findViewById(R.id.priority);
        mParticipants = (TextView) findViewById(R.id.participants);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");


        smsButton = (Button)findViewById(R.id.smsButton);
        sendIntent.setData(Uri.parse("sms:"));
        smsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendIntent.putExtra("sms_body", mTitleView.getText() + "\n" + mBodyView.getText() + "\n" + mStartDate.getText() + "\n" + mEndDate.getText() + "\n" + mPriority.getText() + "\n" + mParticipants.getText());
                startActivity(sendIntent);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Meeting post = dataSnapshot.getValue(Meeting.class);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy 'at' kk:mm", Locale.ENGLISH);
                    // [START_EXCLUDE]
                    mTitleView.setText(post.name);
                    mBodyView.setText(post.description);
                    mStartDate.setText("Start date: " + sdf.format(post.startDate));
                    mEndDate.setText("End date: " + sdf.format(post.endDate));
                    mPriority.setText("Priority: " + post.priority);
                mParticipants.setText("Participants: ");
                    if(post.pres!=null){
                        for (Map.Entry<String,Boolean> entry: post.pres.entrySet()) {
                            mDatabase.child(entry.getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User user = dataSnapshot.getValue(User.class);
                                    mParticipants.append(user.firstname);
                                    mParticipants.append(" ");
                                    mParticipants.append(user.lastname);
                                    mParticipants.append("; ");
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(PostDetailActivity.this, "Failed to load user.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(PostDetailActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };


        mPostReference.addValueEventListener(postListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;

        // Listen for comments
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }

    }

    @Override
    public void onClick(View v) {

    }





}
