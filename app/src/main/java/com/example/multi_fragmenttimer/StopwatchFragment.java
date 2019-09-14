package com.example.multi_fragmenttimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;

public class StopwatchFragment extends Fragment {

    private static final String SW_PREFS = "sWPrefs";
    private static final String CUR_TIME = "curTime";

    private Button buttonStartPause;
    private Button buttonReset;
    private Chronometer stopwatch;
    private long pauseOffset;
    private boolean running;
    private long curTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stopwatch, null);

        stopwatch = v.findViewById(R.id.stopwatch);
        stopwatch.setFormat("%s");
        stopwatch.setBase(SystemClock.elapsedRealtime());
        buttonStartPause = v.findViewById(R.id.button_start_pause);
        buttonReset = v.findViewById(R.id.button_reset);

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetStopwatch();
            }
        });
        buttonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(running) {
                    pauseStopwatch();
                } else {
                    startStopwatch();
                }
            }
        });


        return v;
    }
    private void startStopwatch() {
        if (!running) {
            curTime = SystemClock.elapsedRealtime() - pauseOffset;
            stopwatch.setBase(curTime);
            stopwatch.start();
            running = true;
            buttonStartPause.setText("Pause");
        }
    }

    private void pauseStopwatch() {
        if (running) {
            stopwatch.stop();
            pauseOffset = SystemClock.elapsedRealtime() - stopwatch.getBase();
            running = false;
            buttonStartPause.setText("Start");
        }
    }

    private void resetStopwatch() {
        stopwatch.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
    }

    @Override
    public void onStop() {
        super.onStop();

        SharedPreferences prefs = getActivity().getSharedPreferences(SW_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong(CUR_TIME, curTime);

        editor.apply();

        if(stopwatch != null) {

        }
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = getActivity().getSharedPreferences(SW_PREFS, Context.MODE_PRIVATE);

        curTime = prefs.getLong(CUR_TIME, 0);

        if(running) {
            curTime =  - SystemClock.elapsedRealtime() - stopwatch.getBase();
        }
    }
}