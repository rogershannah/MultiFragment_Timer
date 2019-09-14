package com.example.multi_fragmenttimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class CountdownFragment extends Fragment {

    private static final String PREFS = "prefs";
    private static final String START_MILLIS = "startTimeInMillis";
    private static final String MILLIS_LEFT = "millisLeft";
    private static final String TIMER_RUNNING = "timerRunning";
    private static final String END_TIME = "endTime";

    private EditText editTextInput;
    private TextView textViewCountdown;
    private Button buttonSet;
    private Button buttonStartPause;
    private Button buttonReset;

    private CountDownTimer countDownTimer;

    private boolean isTimerRunning;

    private long startTimeInMillis;
    private long timeLeftInMillis = startTimeInMillis;
    private long endTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_countdown, container, false);

        editTextInput = v.findViewById(R.id.edit_text_input);
        textViewCountdown = v.findViewById(R.id.text_view_countdown);

        buttonStartPause = v.findViewById(R.id.button_start_pause);
        buttonReset = v.findViewById(R.id.button_reset);
        buttonSet = v.findViewById(R.id.button_set);

        buttonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = editTextInput.getText().toString();
                if(input.length() == 0) {
                    Toast.makeText(getContext(), "Field cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                long millisInput = Long.parseLong(input) * 60000;
                if(millisInput == 0) {
                    Toast.makeText(getContext(), "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }

                setTime(millisInput);
                editTextInput.setText("");
            }
        });

        buttonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTimerRunning) {
                    pauseCountDown();
                } else {
                    startCountDown();
                }
            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetCountDown();
            }
        });

        return v;
    }

    private void setTime(Long millisecs) {
        startTimeInMillis = millisecs;
        resetCountDown();
        closeKeyboard();
    }

    private void startCountDown() {
        endTime = System.currentTimeMillis() + timeLeftInMillis;

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDown();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                buttonStartPause.setText("Start");
                updateInterface();
            }
        }.start();

        isTimerRunning = true;
        updateInterface();
    }

    private void pauseCountDown(){
        countDownTimer.cancel();
        isTimerRunning = false;
        updateInterface();
    }

    private void resetCountDown(){
        timeLeftInMillis = startTimeInMillis;
        updateCountDown();
        updateInterface();
    }

    private void updateCountDown() {
        int hours = (int) (startTimeInMillis / 1000) / 3600;
        int mins = (int) ((timeLeftInMillis / 1000) % 3600) / 60;
        int secs = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, mins, secs);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", mins, secs);
        }

        textViewCountdown.setText(timeLeftFormatted);
    }

    private void updateInterface(){
        if(isTimerRunning){
            editTextInput.setVisibility(View.INVISIBLE);
            buttonSet.setVisibility(View.INVISIBLE);
            buttonReset.setVisibility(View.INVISIBLE);
            buttonStartPause.setText("Pause");
        } else {
            editTextInput.setVisibility(View.VISIBLE);
            buttonSet.setVisibility(View.VISIBLE);
            buttonStartPause.setText("Start");
            if(timeLeftInMillis < 1000) {
                buttonStartPause.setVisibility(View.INVISIBLE);
            } else {
                buttonStartPause.setVisibility(View.VISIBLE);
            }
            if(timeLeftInMillis < startTimeInMillis) {
                buttonReset.setVisibility(View.VISIBLE);
            } else {
                buttonReset.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if(view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong(START_MILLIS, startTimeInMillis);
        editor.putLong(MILLIS_LEFT, timeLeftInMillis);
        editor.putBoolean(TIMER_RUNNING, isTimerRunning);
        editor.putLong(END_TIME, endTime);

        editor.apply();

        if(countDownTimer != null) {
            countDownTimer.cancel();
        }
    }



    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        startTimeInMillis = prefs.getLong(START_MILLIS, 600000);
        timeLeftInMillis = prefs.getLong(MILLIS_LEFT, startTimeInMillis);
        isTimerRunning = prefs.getBoolean(TIMER_RUNNING, false);
        updateCountDown();
        updateInterface();

        if(isTimerRunning) {
            endTime = prefs.getLong(END_TIME, 0);
            timeLeftInMillis = endTime - System.currentTimeMillis();

            updateCountDown();
            updateInterface();
            
            if (timeLeftInMillis < 0) {
                timeLeftInMillis = 0;
                isTimerRunning = false;
                updateCountDown();
                updateInterface();
            } else {
                startCountDown();
            }
        }
    }
}
