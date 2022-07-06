package com.example.audioencryptor;

import android.widget.TextView;
import android.app.*;
import kotlinx.coroutines.*;

public class TimerForRecording {
    private TextView timerTextView;
    private long time = 0;
    private boolean timerRunning = false;
    private TimerThread timerThread;
    private Activity activity;
    //private Thread timerThread = new Thread();
    public TimerForRecording(TextView timerTextView, Activity activity) {
        refreshTimer(time);
        this.activity = activity;
        this.timerTextView = timerTextView;
    }

    public void startTimer() {
        time = 0;
        refreshTimer(time);
        timerThread = new TimerThread();
        new Thread(timerThread, "TimerThread").start();
    }

    public String stopTimer() {
        timerThread.stop();
        return refreshTimer(time);
    }

    private class TimerThread implements Runnable {
        private boolean isActive;

        TimerThread() {
            isActive = true;
        }

        void stop() {
            isActive = false;
        }

        @Override
        public void run() {
            while (isActive) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                time++;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshTimer(time);
                    }
                });

            }
        }
    }

    private String refreshTimer(long currentTime) {
        int seconds;
        int minutes;
        long hours;
        String label = "";
        if(currentTime > 0 && currentTime < 60){
            seconds = (int) currentTime;
            minutes = 0;
            hours = 0;
        }else if(currentTime == 0){
            seconds = 0;
            minutes = 0;
            hours = 0;
            return "00:00:00";
        }else{
            seconds = (int) currentTime % 60;
            minutes = (int) currentTime / 60;
            if(minutes > 60){
                hours = minutes / 60;
                minutes = minutes % 60;
            }
            hours = 0;
        }
        label = timeToString(hours, minutes, seconds);

        timerTextView.setText(label);
        return label;
    }
    private String timeToString(long hours, int minutes, int seconds){
        String label = null;
        if (hours < 10 && minutes < 10 && seconds < 10) {
            label = "0" + hours + ":0" + minutes + ":0" + seconds;
        }
        else if(hours < 10 && minutes >= 10 && seconds < 10) {
            label = "0" + hours + ":" + minutes + ":0" + seconds;
        }
        else if(hours < 10 && minutes >= 10 && seconds >= 10){
            label = "0" + hours + ":" + minutes + ":" + seconds;
        }
        else if(hours < 10 && minutes < 10 && seconds >= 10){
            label = "0" + hours + ":0" + minutes + ":" + seconds;
        }
        else if(hours >= 10 && minutes < 10 && seconds < 10){
            label = hours + ":0" + minutes + ":0" + seconds;
        }
        else if(hours >= 10 && minutes >= 10 && seconds < 10){
            label = hours + ":" + minutes + ":0" + seconds;
        }
        else if(hours >= 10 && minutes >= 10 && seconds >= 10){
            label = hours + ":" + minutes + ":" + seconds;
        }
        else if(hours >= 10 && minutes < 10 && seconds >= 10){
            label = hours + ":0" + minutes + ":" + seconds;
        }
        return label;
    }
}
