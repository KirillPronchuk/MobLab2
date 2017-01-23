package com.example.kirill.lab2;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeDialog extends DialogFragment implements OnClickListener {

    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button applyButton;
    private Button cancelButton;
    private Calendar calendar_date = null;
    private int selectedHour;
    private int selectedMinute;
    private ICustomDateTimeListener iCustomDateTimeListener = null;
    private final static String DATE_TIME_LISTENER_KEY = "datetimekey";
    private final static String HOUR_KEY = "hour";
    private final static String MINUTE_KEY = "minute";
    private final static String CUSTOM_DATE_TIME_PICKER = "interface";

    private boolean is24HourView = true;


    final String LOG_TAG = "myLogs";

    public static DateTimeDialog getInstance(DateTimeDialog.ICustomDateTimeListener iCustomDateTimeListener){
        DateTimeDialog result = new DateTimeDialog();
        Bundle args = new Bundle();
        args.putSerializable(DATE_TIME_LISTENER_KEY, iCustomDateTimeListener);
        result.setArguments(args);
        return result;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.d(LOG_TAG, "activity create: hour " + selectedHour + "; minute " + selectedMinute);
        setRetainInstance(true);
        this.setCancelable(true);

    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(savedInstanceState!=null){
            Log.d(LOG_TAG,"----------------------------");
            Log.d(LOG_TAG, "values restored!!!!!!!");
            Log.d(LOG_TAG,"----------------------------");
            selectedHour=savedInstanceState.getInt(HOUR_KEY);
            selectedMinute=savedInstanceState.getInt(MINUTE_KEY);
            iCustomDateTimeListener = (DateTimeDialog.ICustomDateTimeListener)savedInstanceState.getSerializable(CUSTOM_DATE_TIME_PICKER);
        } else {
            iCustomDateTimeListener= (DateTimeDialog.ICustomDateTimeListener)getArguments().getSerializable(DATE_TIME_LISTENER_KEY);
        }


        getDialog().setTitle("Title!");
        View v = inflater.inflate(R.layout.dialog_layout, null);

        TabHost tabHost = (TabHost) v.findViewById(R.id.tabhost);
        // инициализация
        tabHost.setup();

        TabHost.TabSpec tabSpec;

        // создаем вкладку и указываем тег
        tabSpec = tabHost.newTabSpec("tag1");
        // название вкладки
        tabSpec.setIndicator("Time");
        // указываем id компонента из FrameLayout, он и станет содержимым
        tabSpec.setContent(R.id.tab1);
        // добавляем в корневой элемент
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        // указываем название и картинку
        // в нашем случае вместо картинки идет xml-файл,
        // который определяет картинку по состоянию вкладки
        tabSpec.setIndicator("Date");
        tabSpec.setContent(R.id.tab2);
        tabHost.addTab(tabSpec);


        // вторая вкладка будет выбрана по умолчанию
//        tabHost.setCurrentTabByTag("tag2");

        RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rllp.addRule(RelativeLayout.CENTER_IN_PARENT);

        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title).setLayoutParams(rllp);
        }

        timePicker = (TimePicker) v.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(is24HourView);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                selectedHour = hourOfDay;
                selectedMinute = minute;
                Log.d(LOG_TAG,"----------------------------");
                Log.d(LOG_TAG, "time changed: hour " + selectedHour + "; minute " + selectedMinute);
                Log.d(LOG_TAG,"----------------------------");
            }
        });

//        timePicker.setHour(selectedHour);
//        timePicker.setMinute(selectedMinute);

        datePicker = (DatePicker) v.findViewById(R.id.datePicker);

        applyButton = (Button) v.findViewById(R.id.applyButton);
        cancelButton = (Button) v.findViewById(R.id.cancelButton);

        // обработчик переключения вкладок
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(String tabId) {
            }
        });

        applyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (iCustomDateTimeListener != null) {
                    int month = datePicker.getMonth();
                    int year = datePicker.getYear();
                    int day = datePicker.getDayOfMonth();

                    calendar_date.set(year, month, day, selectedHour,
                            selectedMinute);

                    iCustomDateTimeListener.onSet(calendar_date,
                            calendar_date.getTime(), getAMPM(calendar_date));
                }
                dismiss();
            }
        });

        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return v;
    }

    public void onClick(View v) {
        Log.d(LOG_TAG, "Dialog 1: " + ((Button) v).getText());
        dismiss();
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(LOG_TAG, "Date/time dialog: onDismiss");
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d(LOG_TAG, "Date/time dialog: onCancel");
    }

    @Override
    public void onResume() {
        selectedHour = timePicker.getHour();
        selectedMinute = timePicker.getMinute();
        Log.d(LOG_TAG,"----------------------------");
        Log.d(LOG_TAG,"Resume: Hour (timepicker) " + String.valueOf(timePicker.getHour()) + ";Minute (timepicker) " + String.valueOf(timePicker.getMinute()));
        Log.d(LOG_TAG,"Resume: Hour " + String.valueOf(selectedHour) + ";Minute " + String.valueOf(selectedMinute));
        Log.d(LOG_TAG,"----------------------------");
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        if(outState == null)
            outState = new Bundle();
        outState.putInt(HOUR_KEY,selectedHour);
        outState.putInt(MINUTE_KEY,selectedMinute);
        outState.putSerializable(DATE_TIME_LISTENER_KEY, iCustomDateTimeListener);
        super.onSaveInstanceState(outState);
    }



    private String getAMPM(Calendar calendar) {
        String ampm = (calendar.get(Calendar.AM_PM) == (Calendar.AM)) ? "AM"
                : "PM";
        return ampm;
    }

    public void set24HourFormat(boolean is24HourFormat) {
        is24HourView = is24HourFormat;
    }

    public void setDate(Calendar calendar) {
        if (calendar != null)
            calendar_date = calendar;
    }

    public abstract static class ICustomDateTimeListener implements Serializable
    {
        public abstract void onSet(Calendar calendarSelected,
                          Date dateSelected, String AM_PM);

        public abstract void onCancel();
    }

}
