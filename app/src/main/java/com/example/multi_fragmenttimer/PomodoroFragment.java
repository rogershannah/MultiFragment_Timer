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

public class PomodoroFragment extends Fragment {

    private static final String POM_PREFS = "pomPrefs";
    private static final String START_MILLIS = "startTimeInMillis";
    private static final String MILLIS_LEFT = "millisLeft";
    private static final String TIMER_RUNNING = "timerRunning";
    private static final String END_TIME = "endTime";

    private TextView textViewPomodoro;
    private TextView textViewCycles;
    private EditText editTextInput1;
    private Button buttonSet;
    private EditText editTextInput2;
    private EditText editCyclesInput;
    private Button buttonStartPause;
    private Button buttonReset;

    private TextView textViewCycleTotal;
    private TextView textViewTime1;
    private TextView textViewTime2;

    private CountDownTimer countdownTimer;

    private boolean isTimerRunning;
    private boolean isSecondCycle;

    private long startTimeInMillis;
    private long timeLeftInMillis = startTimeInMillis;
    private long endTime;
    private long startCycles;
    private long cyclesLeft = startCycles;
    private long otherCycleInMillis;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pomodoro, container, false);

        editCyclesInput = v.findViewById(R.id.cycles_edit_text_input);
        textViewCycles = v.findViewById(R.id.text_view_cycles);
        textViewCycleTotal = v.findViewById(R.id.cycles_total);
        textViewTime1 = v.findViewById(R.id.time1_total);
        textViewTime2 = v.findViewById(R.id.time2_total);

        editTextInput1 = v.findViewById(R.id.edit_text1_input);
        textViewPomodoro = v.findViewById(R.id.text_view_pomodoro);

        buttonStartPause = v.findViewById(R.id.button_start_pause);
        buttonReset = v.findViewById(R.id.button_reset);

        buttonSet = v.findViewById(R.id.button1_set);

        editTextInput2 = v.findViewById(R.id.edit_text2_input);

        buttonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input1 = editTextInput1.getText().toString();
                String input2 = editTextInput2.getText().toString();
                String cyclesIn = editCyclesInput.getText().toString();
                if(input1.length() == 0 || input2.length() == 0) {
                    Toast.makeText(getContext(), "Field cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(cyclesIn.length() == 0) {
                    cyclesIn = "1";
                }

                long millisInput1 = Long.parseLong(input1) * 60000;

                if(millisInput1 == 0) {
                    Toast.makeText(getContext(), "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }


                setTime(millisInput1, Long.parseLong(cyclesIn));
                otherCycleInMillis = Long.parseLong(input2) * 60000;
                editTextInput1.setText("");
                editTextInput2.setText("");
                editCyclesInput.setText("");

                textViewCycleTotal.setText(cyclesIn + " Cycles");
                textViewTime1.setText(input1 + " Minutes");
                textViewTime2.setText(input2 + " Minutes");
            }
        });

        buttonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        return v;
    }

    private void setTime(Long millisecs, Long c) {
        startTimeInMillis = millisecs;
        startCycles = c;
        resetTimer();
        closeKeyboard();
    }

    private void startTimer() {
        endTime = System.currentTimeMillis() + timeLeftInMillis;

        countdownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updatePomodoroText();
            }

            @Override
            public void onFinish() {
                if (cyclesLeft > 0) {
                    long t = startTimeInMillis;
                    if (isSecondCycle) {
                        cyclesLeft -= 1;
                    }
                    isSecondCycle = !isSecondCycle;
                    setTime(otherCycleInMillis, cyclesLeft);
                    otherCycleInMillis = t;
                    textViewCycles.setText("Cycles Left: " + cyclesLeft);
                    if (cyclesLeft > 0) {
                        startTimer();
                    } else {
                        onFinish();
                    }
                } else {
                    isTimerRunning = false;
                    buttonStartPause.setText("Start");
                    updateInterface();
                }
            }
        }.start();

        isTimerRunning = true;
        updateInterface();
    }

    private void pauseTimer(){
        countdownTimer.cancel();
        isTimerRunning = false;
        updateInterface();
    }

    private void resetTimer(){
        timeLeftInMillis = startTimeInMillis;
        cyclesLeft = startCycles;
        updatePomodoroText();
        updateInterface();
    }

    private void updatePomodoroText() {
        int hours = (int) (startTimeInMillis / 1000) / 3600;
        int mins = (int) ((timeLeftInMillis / 1000) % 3600) / 60;
        int secs = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, mins, secs);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", mins, secs);
        }

        textViewPomodoro.setText(timeLeftFormatted);
        textViewCycles.setText("Cycles Left: " + cyclesLeft);
    }

    private void updateInterface(){
        if(isTimerRunning){
            editCyclesInput.setVisibility(View.INVISIBLE);
            editTextInput1.setVisibility(View.INVISIBLE);
            editTextInput2.setVisibility(View.INVISIBLE);
            buttonSet.setVisibility(View.INVISIBLE);
            buttonReset.setVisibility(View.INVISIBLE);
            buttonStartPause.setText("Pause");

            textViewCycleTotal.setVisibility(View.VISIBLE);
            textViewTime2.setVisibility(View.VISIBLE);
            textViewTime1.setVisibility(View.VISIBLE);
        } else {
            editTextInput1.setVisibility(View.VISIBLE);
            editTextInput2.setVisibility(View.VISIBLE);
            editCyclesInput.setVisibility(View.VISIBLE);
            buttonSet.setVisibility(View.VISIBLE);
            buttonStartPause.setText("Start");
            textViewCycleTotal.setVisibility(View.INVISIBLE);
            textViewTime2.setVisibility(View.INVISIBLE);
            textViewTime1.setVisibility(View.INVISIBLE);
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

        SharedPreferences prefs = getActivity().getSharedPreferences(POM_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong(START_MILLIS, startTimeInMillis);
        editor.putLong(MILLIS_LEFT, timeLeftInMillis);
        editor.putBoolean(TIMER_RUNNING, isTimerRunning);
        editor.putLong(END_TIME, endTime);

        editor.apply();

        if(countdownTimer != null) {
            countdownTimer.cancel();
        }
    }



    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = getActivity().getSharedPreferences(POM_PREFS, Context.MODE_PRIVATE);

        startTimeInMillis = prefs.getLong(START_MILLIS, 600000);
        timeLeftInMillis = prefs.getLong(MILLIS_LEFT, startTimeInMillis);
        isTimerRunning = prefs.getBoolean(TIMER_RUNNING, false);
        updatePomodoroText();
        updateInterface();

        if(isTimerRunning) {
            endTime = prefs.getLong(END_TIME, 0);
            timeLeftInMillis = endTime - System.currentTimeMillis();

            updatePomodoroText();
            updateInterface();

            if (cyclesLeft < 0 && timeLeftInMillis < 0 ) {
                timeLeftInMillis = 0;
                isTimerRunning = false;
                updatePomodoroText();
                updateInterface();
            } else {
                startTimer();
            }
        }
    }
}

