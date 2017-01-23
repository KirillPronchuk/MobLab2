package com.example.kirill.lab2;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.pm.ActivityInfo;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.kirill.lab2.model.Meeting;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NewMeetingActivity extends BaseActivity {

    TextInputEditText edtEventDateTimeStart;
    DateTimeDialog dlg1;
    TextInputEditText edtEventDateTimeEnd;
    DateTimeDialog dlg2;
    String[] data = {"low", "medium", "high"};
    Spinner spinner;
    EditText nameEdit;
    EditText descrEdit;
    Button submitButton;
    CheckBox checkbox;

    private DatabaseReference mDatabase;

    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";
    private static final String DATE_ERROR = "Must be greater than start date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_meeting);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        checkbox = (CheckBox)findViewById(R.id.checkBox);
        nameEdit = (EditText)findViewById(R.id.nameEdit);
        descrEdit = (EditText)findViewById(R.id.descrEdit);
        edtEventDateTimeStart = ((TextInputEditText) findViewById(R.id.edtEventDateTimeStart));
        dlg1 = DateTimeDialog.getInstance(new DateTimeDialog.ICustomDateTimeListener() {

            @Override
            public void onSet(Calendar calendarSelected,
                              Date dateSelected,
                              String AM_PM) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy 'at' kk:mm", Locale.ENGLISH);
                edtEventDateTimeStart.setText(sdf.format(dateSelected));
            }

            @Override
            public void onCancel() {

            }
        });
        dlg1.set24HourFormat(true);
        dlg1.setDate(Calendar.getInstance());

        edtEventDateTimeStart.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dlg1.show(getSupportFragmentManager(),"dlg1");
                    }
                });


        edtEventDateTimeEnd = ((TextInputEditText) findViewById(R.id.edtEventDateTimeEnd));
        dlg2 = DateTimeDialog.getInstance(new DateTimeDialog.ICustomDateTimeListener() {

            @Override
            public void onSet(Calendar calendarSelected,
                              Date dateSelected,
                              String AM_PM) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy 'at' kk:mm", Locale.ENGLISH);

                edtEventDateTimeEnd.setText(sdf.format(dateSelected));
            }

            @Override
            public void onCancel() {

            }
        });
        dlg2.set24HourFormat(true);
        dlg2.setDate(Calendar.getInstance());

        edtEventDateTimeEnd.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dlg2.show(getSupportFragmentManager(),"dlg2");
                    }
                });




        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        // выделяем элемент
        spinner.setSelection(0);
        // устанавливаем обработчик нажатия
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        submitButton = (Button)findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });

    }


    private void submitPost() {
        final String title = nameEdit.getText().toString();
        final String body = descrEdit.getText().toString();
        final String startTime = edtEventDateTimeStart.getText().toString();
        final String endTime = edtEventDateTimeEnd.getText().toString();
        final String priority = spinner.getSelectedItem().toString();
        final boolean present = checkbox.isChecked();

        // Title is required
        if (TextUtils.isEmpty(title)) {
            nameEdit.setError(REQUIRED);
            return;
        }

        // Body is required
        if (TextUtils.isEmpty(body)) {
            descrEdit.setError(REQUIRED);
            return;
        }

        if (TextUtils.isEmpty(title)) {
            edtEventDateTimeStart.setError(REQUIRED);
            return;
        }

        if (TextUtils.isEmpty(body)) {
            edtEventDateTimeEnd.setError(REQUIRED);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy 'at' kk:mm", Locale.ENGLISH);
        try {
            if(sdf.parse(startTime).getTime()>sdf.parse(endTime).getTime()){
                edtEventDateTimeEnd.setError(DATE_ERROR);
                return;
            }
        }catch (ParseException e){
            Log.e("ERROR", "Date parse exception");
        }

        try {
        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();
        createNewMeeting(title,body,sdf.parse(startTime),sdf.parse(endTime),priority, present);
        }catch (ParseException e){
            Log.e("ERROR", "Date parse exception");
        }


    }

    private void setEditingEnabled(boolean enabled) {
        nameEdit.setEnabled(enabled);
        descrEdit.setEnabled(enabled);
        if (enabled) {
            submitButton.setVisibility(View.VISIBLE);
        } else {
            submitButton.setVisibility(View.GONE);
        }
    }

    // [START write_fan_out]
    private void createNewMeeting(String name, String desription, Date startDate, Date endDate, String priority, boolean present) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("meetings").push().getKey();
        Meeting post = new Meeting(name, desription, startDate, endDate, priority);
        if(present){
            post.pres.put(getUid(),true);
        }
        Map<String, Object> postValues = post.toMap();

        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/meetings/" + key, postValues);


        mDatabase.child("meetings").child(key).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mDatabase.updateChildren(childUpdates);
                        setEditingEnabled(true);
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "onCancelled", databaseError.toException());
                        // [START_EXCLUDE]
                        setEditingEnabled(true);
                        // [END_EXCLUDE]
                    }
                });

    }
    // [END write_fan_out]

}
